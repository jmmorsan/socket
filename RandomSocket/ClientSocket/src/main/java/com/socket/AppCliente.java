package com.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.UnknownHostException;

/**
 * Hello world!
 *
 */
public class AppCliente {
	static final int  PORT = 7777;
	
    public static void main( String[] args ) {
    	
        try {
        	//conectamos con el servidor
        	Socket socket = new Socket("172.26.159.119",PORT);
        	
        	//Para enviar datos al server
        	PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
        	
        	//Para recibir respuestas del servidor
        	BufferedReader entradaSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        	//Leer los datos introducidos por consola
        	BufferedReader entradaConsola = new BufferedReader(new InputStreamReader(System.in));
        	
        	System.out.println("<Cliente>Inserte un número: ");
        	System.out.println("<Cliente>Si quiere terminar el juego escriba SALIR: ");
        	//Leemos de consola y enviamos al server
        	String texto;
        	
        	while((texto = entradaConsola.readLine()) != null) {
        		
        		//Mejora 3: Permitir salir del juego
        		if (texto.equalsIgnoreCase("SALIR")) {
        			salida.println("SALIR"); // Avisamos al servidor
        			System.out.println("Has abandonado la partida.");
        			break; // Rompemos el bucle para cerrar
        		}
        	
        	
        		//Mejora 1: Validar que el dato introducido es un número
        		try {
        			//Intentamos convertir a número      		
        			Integer.parseInt(texto);
        		
        			//Si es correcto enviamos al server
        			salida.println(texto);
        		
        			//Leemos respuesta del servidor
        			String respuesta = entradaSocket.readLine();
        			
					if (respuesta != null) {
						System.out.println(respuesta);
						//Insertar otro número
						System.out.println("<Cliente>Inserte otro número: ");
					}else {
						break;
					}
        		
        		} catch(NumberFormatException ex) {
        			System.out.println("<Cliente>Debe insertar un número válido");

                    System.out.println("<Cliente>Inserte un número: "); 
        		}        		
        	}
        	
        //Cerramos flujos y socket
        socket.close();
        	
        }catch(UnknownHostException ex) {
        	
        } catch (IOException e) {
			
			e.printStackTrace();
		}
    }
}
