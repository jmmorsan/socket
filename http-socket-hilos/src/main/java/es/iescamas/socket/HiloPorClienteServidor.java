package es.iescamas.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Servidor TCP que atiende clientes mediante un hilo por conexión.
 * Sirve HTML básico y un favicon desde src/main/resources/favicon.ico
 */
public class HiloPorClienteServidor implements Runnable {

    /** Puerto donde escucha el servidor. */
    protected int serverPort = 9001;

    /** Socket servidor. */
    protected ServerSocket serversocket = null;

    /** Flag de parada. */
    protected boolean isStopped;

    /** Referencia al hilo que ejecuta run(). */
    protected Thread runningThread = null;

    public HiloPorClienteServidor(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        synchronized (this) {
            this.runningThread = Thread.currentThread();
        }

        openServerSocket();

        while (!isStopped()) {
            try {
                Socket clientSocket = this.serversocket.accept();

                new Thread(() -> {
                    try {
                        processClientRequest(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, "client-" + clientSocket.getPort()).start();

            } catch (IOException e) {
                if (isStopped()) {
                    System.out.println("Server stopped.");
                    return;
                }
                throw new RuntimeException("Error accepting client connection", e);
            }
        }

        System.out.println("Server Stopped");
    }

    /**
     * Procesa la conexión de un cliente.
     */
    private void processClientRequest(Socket clientSocket) throws IOException {
        try (clientSocket;
             InputStream in = clientSocket.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.US_ASCII));
             OutputStream out = clientSocket.getOutputStream()) {

            // 1) Leer la primera línea: "GET /ruta HTTP/1.1"
            String requestLine = br.readLine();
            if (requestLine == null || requestLine.isBlank()) return;

            String path = "/";
            // Parseo básico de la ruta
            int start = requestLine.indexOf(' ');
            int end = requestLine.indexOf(' ', start + 1);
            if (start != -1 && end != -1) {
                path = requestLine.substring(start + 1, end);
            }
            
            // Log en consola (Importante para la evidencia de hilos)
            System.out.println("[" + Thread.currentThread().getName() + "] Petición: " + path);
            
            
            //Mejora 1: Ruta dinámica /nombre/<nombre>
            if (path.startsWith("/nombre/")) {
                String nombre = path.substring(8); // Quita "/nombre/" para quedarse con el resto
                
                String responseBody = "<html><body style='font-family:sans-serif; text-align:center; background-color:#e3f2fd;'>" +
                        "<h1 style='color:#1565c0;'>Hola " + nombre + "</h1>" +
                        "<p>Atendido por el hilo: <b>" + Thread.currentThread().getName() + "</b></p>" +
                        "</body></html>";

                byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
                String headers = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/html; charset=UTF-8\r\n" +
                        "Content-Length: " + bytes.length + "\r\n" +
                        "\r\n";
                
                out.write(headers.getBytes(StandardCharsets.US_ASCII));
                out.write(bytes);
                out.flush();
                return; // Importante: salir para no enviar la pagina por defecto de abajo
            }

            // 2) Favicon: servir el fichero real desde resources y salir
            if ("/favicon.ico".equals(path)) {
                serveFavicon(out);
                return;
            }
            
            //Mejora 3: Página de Inicio / Root
            
            if (path.equals("/")) {
                String homePage = "<html><head><title>Inicio - Servidor Java</title></head>" +
                        "<body style='font-family: sans-serif; text-align: center; padding: 40px; background-color: #f0f4c3;'>" +
                        "<h1 style='color: #33691e;'>🏠 Bienvenido a mi Servidor Concurrente</h1>" +
                        "<p>Selecciona qué quieres probar:</p>" +
                        "<ul style='list-style-type: none; padding: 0;'>" +
                        
                        // Enlace a la Mejora 1
                        "<li><a href='/nombre/Juanma' style='display:inline-block; margin:10px; padding:10px 20px; background:#4caf50; color:white; text-decoration:none; border-radius:5px;'>👉 Probar Saludo (Mejora 1)</a></li>" +
                        
                        // Enlace a la Mejora 2 (Forzamos un error)
                        "<li><a href='/ruta-inventada' style='display:inline-block; margin:10px; padding:10px 20px; background:#f44336; color:white; text-decoration:none; border-radius:5px;'>👉 Probar Error 404 (Mejora 2)</a></li>" +
                        "</ul>" +
                        
                        "<hr><p><small>Atendido por: " + Thread.currentThread().getName() + "</small></p>" +
                        "</body></html>";

                byte[] homeBytes = homePage.getBytes(StandardCharsets.UTF_8);
                String homeHeaders = "HTTP/1.1 200 OK\r\n" +
                                     "Content-Type: text/html; charset=UTF-8\r\n" +
                                     "Content-Length: " + homeBytes.length + "\r\n" +
                                     "Connection: close\r\n\r\n";

                out.write(homeHeaders.getBytes(StandardCharsets.US_ASCII));
                out.write(homeBytes);
                out.flush();
                return; // Importante salir para que no salte el 404
            }
            
            //Mejora 2: Gestión del 404
            
            // Si el código llega aquí, es que la ruta no era ni /nombre/ ni /favicon.ico
            // Por tanto, es una ruta desconocida.
            
            String page404 = "<html><head><title>Error 404</title></head>" +
                             "<body style='background-color: #ffcccc; font-family: sans-serif; text-align: center;'>" +
                             "<h1 style='color: red;'>⛔ ERROR 404</h1>" +
                             "<h2>Página no encontrada</h2>" +
                             "<p>Lo sentimos, la ruta <b>" + path + "</b> no existe en este servidor.</p>" +
                             "<p>Intenta probar con: <i>/nombre/TuNombre</i></p>" +
                             "<p><small>Hilo: " + Thread.currentThread().getName() + "</small></p>" +
                             "</body></html>";

            byte[] bytes404 = page404.getBytes(StandardCharsets.UTF_8);

            // Fíjate en la primera línea: HTTP/1.1 404 Not Found (Ya no es 200 OK)
            String headers404 = "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Type: text/html; charset=UTF-8\r\n" +
                                "Content-Length: " + bytes404.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n";

            out.write(headers404.getBytes(StandardCharsets.US_ASCII));
            out.write(bytes404);
            out.flush();
            
            // Log en consola para que veas el error
            System.out.println("[" + Thread.currentThread().getName() + "] ⚠️ 404 Not Found: " + path);

            // 3) Datos del cliente
            String clientIp = clientSocket.getInetAddress().getHostAddress();
            int clientPort = clientSocket.getPort(); // puerto remoto del cliente
            String remote = clientSocket.getRemoteSocketAddress().toString(); // /IP:PUERTO

            long time = System.currentTimeMillis();
            String fecha = new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(new Date(time));

            String body = "<html>"
                    + "<head>"
                    + "<link rel='icon' href='/favicon.ico'>"
                    + "<title>Programación de Servicios y Procesos</title>"
                    + "</head>"
                    + "<body style='background-color: coral;'>"
                    + "<h3 style='color:blue;'>Servidor OK</h3>"
                    + "<p>Path: " + path + "</p>"
                    + "<p>Server: " + fecha + "</p>"
                    + "<p>Hilo: " + Thread.currentThread().getName() + "</p>"
                    + "<p>Cliente IP: " + clientIp + "</p>"
                    + "<p>Cliente puerto: " + clientPort + "</p>"
                    + "<p>Remote: " + remote + "</p>"
                    + "</body></html>";

            byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

            String headers =
                    "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html; charset=UTF-8\r\n" +
                    "Content-Length: " + bodyBytes.length + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";

            out.write(headers.getBytes(StandardCharsets.US_ASCII));
            out.write(bodyBytes);
            out.flush();

            // Log: ignorar favicon (ya se devuelve arriba) y registrar petición normal
            System.out.println("[" + Thread.currentThread().getName() + "] " + requestLine);
            System.out.println("[" + Thread.currentThread().getName() + "] Cliente: " + remote);
            System.out.println("[" + Thread.currentThread().getName() + "] Petición procesada: " + fecha);
        }
    }

    /**
     * Sirve el favicon real desde el classpath: src/main/resources/favicon.ico
     */
    private void serveFavicon(OutputStream out) throws IOException {
        try (InputStream iconStream = HiloPorClienteServidor.class.getResourceAsStream("/favicon.ico")) {

            if (iconStream == null) {
                out.write(("HTTP/1.1 404 Not Found\r\nConnection: close\r\n\r\n")
                        .getBytes(StandardCharsets.US_ASCII));
                out.flush();
                return;
            }

            byte[] iconBytes = iconStream.readAllBytes();

            String headers =
                    "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: image/x-icon\r\n" +
                    "Content-Length: " + iconBytes.length + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";

            out.write(headers.getBytes(StandardCharsets.US_ASCII));
            out.write(iconBytes);
            out.flush();
        }
    }

    private synchronized boolean isStopped() {
        return isStopped;
    }

    private void openServerSocket() {
        try {
            this.serversocket = new ServerSocket(this.serverPort);
        } catch (IOException ex) {
            throw new RuntimeException("Cannot open port " + serverPort, ex);
        }
    }

    public synchronized void stop() {
        this.isStopped = true;
        try {
            if (this.serversocket != null) {
                this.serversocket.close();
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
