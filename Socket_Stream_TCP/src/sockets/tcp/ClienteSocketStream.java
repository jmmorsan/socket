package sockets.tcp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClienteSocketStream {
    public static void main(String[] args) {
        try (Socket clientSocket = new Socket()) {
            // Tarea 1.2: IP real de la VM (NO localhost)
            String ipVM = "192.168.165.6"; // Mi IP de la MV
            int puerto = 5555;
            
            System.out.println("[DEBUG] Cliente: destino=" + ipVM + ":" + puerto);
            
            InetSocketAddress addr = new InetSocketAddress(ipVM, puerto);
            clientSocket.connect(addr);

            OutputStream os = clientSocket.getOutputStream();
            String mensaje = "Hola desde el host";
            
            os.write(mensaje.getBytes());
            // Uso de flush() según la tabla para asegurar el envío
            os.flush(); 
            
            System.out.println("Mensaje enviado con éxito");

        } catch (IOException e) {
            System.err.println("Error en el cliente: " + e.getMessage());
        }
    }
}