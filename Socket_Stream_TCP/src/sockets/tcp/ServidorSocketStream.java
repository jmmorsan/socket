package sockets.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorSocketStream {
    public static void main(String[] args) {
        // Uso de try-with-resources para asegurar el close() automático
        try (ServerSocket serverSocket = new ServerSocket()) {
            
            // Tarea 1.1: Bind a IP 0.0.0.0 y puerto elegido (Ejercicio 1)
            InetSocketAddress addr = new InetSocketAddress("0.0.0.0", 5555);
            serverSocket.bind(addr);
            System.out.println("[DEBUG] Servidor: escuchando en puerto 5555");

            // Tarea 1.2: Aceptar conexión
            try (Socket newSocket = serverSocket.accept()) {
                // Tarea 1.3: Log de IP remota usando getRemoteSocketAddress()
                System.out.println("[DEBUG] Servidor: conectado desde=" + newSocket.getRemoteSocketAddress());

                InputStream is = newSocket.getInputStream();
                byte[] buffer = new byte[25];

                // Tarea 2.1: Leer bytes y capturar la cantidad real (Ejercicio 2)
                int leidos = is.read(buffer); 

                if (leidos != -1) {
                    // Tarea 2.2: Crear el String evitando la basura (usando el offset y length)
                    String mensajeLimpio = new String(buffer, 0, leidos);
                    System.out.println("[DEBUG] Recibido: '" + mensajeLimpio + "'");
                    System.out.println("[DEBUG] Respuesta bytes=" + leidos);
                }
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }
}