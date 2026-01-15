package sockets.tcp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClienteSocketStream {
    public static void main(String[] args) {
        try (Socket clientSocket = new Socket()) {
            // EJERCICIO 1: Conectar a la IP real de la VM
            String ipVM = "192.168.165.6"; 
            int puerto = 5555;
            
            System.out.println("[e1] Conectando a " + ipVM + ":" + puerto);
            
            InetSocketAddress addr = new InetSocketAddress(ipVM, puerto);
            clientSocket.connect(addr);

            OutputStream os = clientSocket.getOutputStream();
            String mensaje = "Hola mundo"; // Mensaje corto para ver la basura en el server
            
            os.write(mensaje.getBytes());
            os.flush();
            System.out.println("Mensaje enviado");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}