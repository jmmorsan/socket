package es.iescamas.socket;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Tests de integración con JUnit 5 para el Servidor Concurrente.
 * Comprueba: Rutas dinámicas, Error 404 y Concurrencia real.
 */
class HiloPorClienteServidorTest {

    private HiloPorClienteServidor server;
    private Thread serverThread;
    private int port;

    @BeforeEach
    void startServer() throws Exception {
        // 1. Buscamos un puerto libre automático para evitar errores de "Address in use"
        try (ServerSocket tmp = new ServerSocket(0)) {
            port = tmp.getLocalPort();
        }

        // 2. Arrancamos servidor en ese puerto
        server = new HiloPorClienteServidor(port);
        serverThread = new Thread(server, "test-server");
        serverThread.start();

        // 3. Esperamos a que esté escuchando realmente
        waitUntilListening("127.0.0.1", port, 1000);
    }

    @AfterEach
    void stopServer() throws Exception {
        // 4. Paramos el servidor limpiamente tras cada test
        server.stop();
        serverThread.join(500);
    }

    // --- TEST 1 (Obligatorio C1): Ruta dinámica ---
    @Test
    @DisplayName("GET /nombre/Ana devuelve 200 OK y saludo")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    @Tag("http")
    void shouldSayHello() throws Exception {
        String response = httpGet("/nombre/Ana");
        
        // Verificamos que responde OK y con el nombre correcto
        assertTrue(response.contains("200 OK"), "Debe devolver estado 200");
        assertTrue(response.contains("Hola Ana"), "El body debe saludar a Ana");
    }

    // --- TEST 2 (Obligatorio C2): Ruta 404 ---
    @Test
    @DisplayName("GET /rutainventada devuelve 404 Not Found")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    @Tag("http")
    void shouldReturn404() throws Exception {
        String response = httpGet("/rutainventada");
        
        // Verificamos cabecera 404 y mensaje de error HTML
        assertTrue(response.contains("404 Not Found"), "Debe devolver estado 404");
        assertTrue(response.contains("ERROR 404"), "El body debe contener el mensaje de error");
    }

    // --- TEST 3 (Obligatorio C2): Concurrencia ---
    @Test
    @DisplayName("Dos clientes simultáneos reciben respuesta")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    @Tag("concurrencia")
    void shouldHandleConcurrentClients() throws Exception {
        // Hilo Cliente 1
        Thread t1 = new Thread(() -> {
            try {
                String res = httpGet("/nombre/Cliente1");
                assertTrue(res.contains("Hola Cliente1"));
            } catch (Exception e) { fail("Fallo cliente 1"); }
        });

        // Hilo Cliente 2
        Thread t2 = new Thread(() -> {
            try {
                String res = httpGet("/nombre/Cliente2");
                assertTrue(res.contains("Hola Cliente2"));
            } catch (Exception e) { fail("Fallo cliente 2"); }
        });

        // Lanzamos a la vez
        t1.start();
        t2.start();
        
        // Esperamos que terminen
        t1.join();
        t2.join();
    }

    // --- MÉTODOS AUXILIARES (No tocar) ---

    private String httpGet(String path) throws Exception {
        try (Socket s = new Socket("127.0.0.1", port);
             OutputStream out = s.getOutputStream();
             InputStream in = s.getInputStream()) {

            String req = "GET " + path + " HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n";
            out.write(req.getBytes(StandardCharsets.US_ASCII));
            out.flush();
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void waitUntilListening(String host, int port, long maxMs) throws Exception {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < maxMs) {
            try (Socket ignored = new Socket(host, port)) {
                return;
            } catch (IOException e) {
                Thread.sleep(50);
            }
        }
        fail("El servidor no arrancó a tiempo");
    }
}