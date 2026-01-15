package sockets.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorSocketStream {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket()) {
            
            // EJERCICIO 1: Escuchar en 0.0.0.0 para aceptar externos
            InetSocketAddress addr = new InetSocketAddress("0.0.0.0", 5555);
            serverSocket.bind(addr);
            System.out.println("[e1] Servidor escuchando en puerto 5555");

            try (Socket newSocket = serverSocket.accept()) {
                // EJERCICIO 1: Log de la IP del cliente conectado
                System.out.println("[e1] Conexión recibida de: " + newSocket.getRemoteSocketAddress());

                InputStream is = newSocket.getInputStream();
                byte[] buffer = new byte[25];

                // LECTURA BÁSICA (Aún sin arreglar la basura, esto es correcto para esta fase)
                is.read(buffer);
                
                // Esto imprimirá el mensaje + símbolos raros (cuadraditos) si sobra espacio
                System.out.println("Mensaje recibido: " + new String(buffer));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}