package sockets.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorSocketStream {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket()) {
            
            InetSocketAddress addr = new InetSocketAddress("0.0.0.0", 5555);
            serverSocket.bind(addr);
            System.out.println("[DEBUG] Servidor: escuchando en puerto 5555");

            try (Socket newSocket = serverSocket.accept()) {
                System.out.println("[DEBUG] Servidor: conectado desde=" + newSocket.getRemoteSocketAddress());

                InputStream is = newSocket.getInputStream();
                byte[] buffer = new byte[25];

                // EJERCICIO 2: Guardamos cuántos bytes llegaron realmente
                int leidos = is.read(buffer); 

                if (leidos != -1) {
                    // EJERCICIO 2: Constructor de String con offset y length para quitar basura
                    String mensajeLimpio = new String(buffer, 0, leidos);
                    System.out.println("[DEBUG] Recibido: '" + mensajeLimpio + "'");
                    System.out.println("[DEBUG] Respuesta bytes=" + leidos);
                }
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}