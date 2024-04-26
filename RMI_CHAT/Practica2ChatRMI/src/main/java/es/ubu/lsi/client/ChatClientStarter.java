package es.ubu.lsi.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

import es.ubu.lsi.common.ChatMessage;
import es.ubu.lsi.server.ChatServer;

/**
 * Chat client starter.
 * 
 * @author Ismael Manzanera López
 * @author Daniel Fernández Barrientos
 */

public class ChatClientStarter {

    /** El nickname. */
    private String nickname;
    
    /** El hostClient. */
    private String hostCliente = "localhost";

    /**
     * Constructor de ChatClientStarter.
     *
     * @param args los argumentos
     */
    public ChatClientStarter(String[] args) {
        this.nickname = args.length > 0 ? args[0] : "defaultNickname";
        start();
    }

    /**
     * Método start.
     * 
     * Se inicia el cliente, se registra en el servidor y se conecta al chat
     * en "runClientConsole", para poder escribir y recibir mensajes del chat.
     * 
     */
    public void start() {
        try {
        	// Crea el cliente
            ChatClientImpl chatClient = new ChatClientImpl(nickname);
            // Exporta el cliente
            UnicastRemoteObject.exportObject(chatClient, 0);
            Registry registry = LocateRegistry.getRegistry(hostCliente);
            ChatServer servidor = (ChatServer) registry.lookup("/servidor");
            // Registra el cliente en el servidor
            servidor.checkIn(chatClient);
            // Inicia la consola del chat.
            runClientConsole(servidor, chatClient);
        } catch (RemoteException | NotBoundException e) {
            System.out.println("ChatClientStarter-Start: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Método runClientConsole.
     * 
     * Lanza el cliente en consola para recibir y escribir los 
     * mensajes en el chat.
     *
     * @param server the server
     * @param client the client
     */
    private void runClientConsole(ChatServer server, ChatClientImpl client) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("\n¡Hola, " + nickname + "! Teclado activo, esperando mensajes...");
            System.out.println("-------------------------------------------");
            String messageText;
      
            while (true) {
                messageText = scanner.nextLine();
                try {
	                if ("logout".equals(messageText)) {
						server.logout(client);
	                    break;
	                } else if ("shutdown".equals(messageText)) {
	                    server.shutdown(client);
	                    break;
	                } else {
	                    server.publish(new ChatMessage(client.getId(), nickname, messageText));
	                }
				} catch (RemoteException e) {
					System.out.println("ChatClientStarter-runClientConsole: " + e.getMessage());
				}
            }
        } finally {
            System.exit(0);
        }
    }
}
