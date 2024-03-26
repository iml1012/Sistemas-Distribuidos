package es.ubu.lsi.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.ubu.lsi.common.ChatMessage;
import es.ubu.lsi.common.MessageType;

public class ChatServerImpl implements ChatServer {

	/** Constante, puerto por defecto. */
	private static final int DEFAULT_PORT = 1500;

	/** ID del cliente, también sirve como contador de clientes conectados. */
	private static int clientId = 0;

	/** Formato de fecha. */
	private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	/** Puerto. */
	private int port;

	/** Booleano para saber si el hilo sigue vivo. */
	private boolean alive;

	/** Mapa con los usuarios de clientes. */
	Map<String, ServerThreadForClient> clientsMap = new HashMap<>();

	/** Mapa con los ids de clientes. */
	Map<Integer, String> clientsIdMap = new HashMap<>();

	/** Mapa con los usuarios baneados. */
	Map<String, Boolean> bannedUsers = new HashMap<>();

	/** Servidor socket. */
	ServerSocket server;

	/**
	 * Constructor con el puerto 1500 por defecto.
	 */

	public ChatServerImpl() {
		this(DEFAULT_PORT);
	}

	/**
	 * Constructor con el puerto como parámetro de argumento.
	 *
	 * @param port the port
	 */
	public ChatServerImpl(int port) {
		this.alive = true;
		this.port = port;
	}

	/**
	 * Incrementa y devuelve el siguiente ID del cliente. El metodo es synchronized
	 * para evitar problemas de acceso concurrente y tener IDs unicos.
	 *
	 * @return id del cliente, unico.
	 */
	private synchronized int getNextId() {
		return clientId++;
	}

	/**
	 * Método que muestra los mapas por pantalla, ha sido utlizado para pruebas.
	 */
	public void mostrarMapas() {
		System.out.println("Contenido de clientsMap (Usuario -> Thread):");
		for (Map.Entry<String, ServerThreadForClient> entry : clientsMap.entrySet()) {
			System.out.println("Usuario: " + entry.getKey() + ", Thread: " + entry.getValue().toString());
		}

		System.out.println("\nContenido de clientsIdMap (ID -> Username):");
		for (Map.Entry<Integer, String> entry : clientsIdMap.entrySet()) {
			System.out.println("ID: " + entry.getKey() + ", Usuario: " + entry.getValue());
		}
	}

	/**
	 * Inicia la conexion a traves del puerto indicado y comienza a escuchar las
	 * peticiones.
	 */
	@Override
	public void startup() {
	    try {
	        this.server = new ServerSocket(this.port);
	        System.out.println("[" + getDateString() + "] Servidor iniciado en el puerto: " + this.port);
	    } catch (IOException e) {
	        System.err.println("ERROR: No se puede conectar al servidor");
	        System.exit(1);
	    }

	    while (alive) {
	        try {
	            System.out.println("Escuchando conexiones en " + server.getInetAddress() + ":" + server.getLocalPort());
	            // Al aceptar una conexión, inicia el hilo del servidor para ese cliente.
	            Socket client = server.accept();
	            ServerThreadForClient clientThread = new ServerThreadForClient(client);
	            clientThread.start();
	        } catch (IOException e) {
	            if (!alive) {
	                System.out.println("[" + getDateString() + "] Servidor deteniéndose...");
	            } else {
	                System.err.println("ERROR: No se pudo aceptar la conexión. Apagando el servidor...");
	                // Considera si quieres realmente detener el servidor aquí o solo lograr el error.
	            }
	            break; // Salir del bucle si el servidor se detiene o si ocurre un error
	        }
	    }
	}

	/**
	 * Método que finaliza y cierra el servidor, incluidas todas las conexiones con
	 * los clientes.
	 */
	@Override
	public void shutdown() {
	    alive = false; // Marca el servidor como no activo para prevenir nuevas conexiones
	    try {
	        // Cierra todas las conexiones de clientes activos
	        for (ServerThreadForClient client : clientsMap.values()) {
	            client.shutdownClient(); // Intenta cerrar la conexión de cada cliente de manera segura
	        }
	        clientsMap.clear(); // Limpia el mapa de clientes para liberar recursos
	        
	        // Cierra el socket del servidor si está abierto
	        if (server != null && !server.isClosed()) {
	            server.close();
	        }
	    } catch (IOException e) {
	        System.err.println("[" + getDateString() + "] Error durante el apagado del servidor.");
	    } finally {
	        // Intenta liberar otros recursos aquí si es necesario
	        onServerShutdown(); // Método adicional para manejar lógica de limpieza
	    }
	}

	/**
	 * Este método puede contener cualquier lógica de limpieza adicional necesaria
	 * cuando el servidor se está apagando, como guardar estados, liberar recursos
	 * adicionales, notificaciones, etc.
	 */
	private void onServerShutdown() {
	    // Implementar lógica de limpieza adicional aquí
	    System.out.println("[" + getDateString() + "] El servidor se ha apagado correctamente.");
	}

	/**
	 * Recibe un mensaje de un cliente y lo reenvia al resto de clientes conectados,
	 * se utilzia el id del mensaje para saber el emisor del mensaje.
	 *
	 * @param message mensaje a enviar
	 */
	@Override
	public void broadcast(ChatMessage message) {
	    String senderUsername = getUsernameById(message.getId());
	    // Comprobar si el usuario está baneado antes de proceder con el broadcast
	    if (bannedUsers.getOrDefault(senderUsername, false)) {
	        // Si el usuario está baneado, no hacer broadcast de su mensaje.
	        return;
	    }

	    String time = "[" + getDateString() + "]";
	    String formattedMessage = String.format("%s %s: %s", time, senderUsername, message.getMessage());

	    for (ServerThreadForClient handler : clientsMap.values()) {
	        // Verificar si el destinatario no está baneado antes de enviar (opcional, dependiendo de la lógica de negocio)
	        if (!bannedUsers.getOrDefault(handler.getUsername(), false)) {
	            try {
	                // Creamos un nuevo mensaje para mantener el formato y lo enviamos.
	                ChatMessage newMsg = new ChatMessage(message.getId(), message.getType(), formattedMessage);
	                handler.output.writeObject(newMsg);
	            } catch (IOException e) {
	                System.err.println("ERROR: No se pudo enviar mensaje al cliente " + handler.getUsername());
	                remove(handler.id);
	            }
	        }
	    }
	}


	/**
	 * Devuelve el username del cliente, utilizando como parametro de argumento el
	 * id del cliente. Para ello se ha implementado un mapa adiccional llamado
	 * 'clientsIdMap'.
	 *
	 * @param id del cliente
	 * @return username del cliente
	 */
	public String getUsernameById(int id) {
		return clientsIdMap.get(id);
	}

	/**
	 * Desconecta y elimina al cliente del mapa.
	 *
	 * @param id id del cliente
	 */
	@Override
	public void remove(int id) {
		// Si se encontró un nombre de usuario, proceder a removerlo
		String usernameToDelete = getUsernameById(id);
		if (usernameToDelete != null) {
			// Recupera y elimina el cliente del mapa
			ServerThreadForClient client = clientsMap.remove(usernameToDelete);
			if (client != null) {
				client.shutdownClient(); // cerramos la conexión correctamente
				clientsIdMap.remove(id);
				// mostramos mensajes informativos
				System.out.println("[" + getDateString() + "] Cliente  " + usernameToDelete + " eliminado.");
				System.out.println("Clientes conectados: " + clientsMap.size());
			}
		} else {
			System.out.println("[" + getDateString() + "] No se encontró el cliente con ID: " + usernameToDelete);
		}
	}

	/**
	 * Devuelve la hora exacta en formato texto, este metodo es utilizado para
	 * mostrar por pantalla los mensajes.
	 *
	 * @return fecha en formato texto
	 */
	public String getDateString() {
		return sdf.format(new Date());
	}

	/**
	 * Metodo principal, inicia el servidor.
	 *
	 * @param args Argumentos del main
	 */
	public static void main(String[] args) {

		new ChatServerImpl().startup();
	}

	/**
	 * Clase interna ServerThreadForClient Hilo que gestiona la comunicación entre
	 * el cliente y el servidor.
	 */
	class ServerThreadForClient extends Thread {

		/** Id del cliente. */
		private int id;

		/** Boleano, indica si el hilo esta corriendo. */
		private boolean running;

		/** Username del cliente. */
		private String username;

		/** Socket del cliente. */
		private Socket socket;

		/** Input. */
		private ObjectInputStream input;

		/** Output. */
		private ObjectOutputStream output;

		/**
		 * Constructor.
		 *
		 * @param socket Socket
		 */
		public ServerThreadForClient(Socket socket) {
		    this.socket = socket;
		    this.running = true;

		    try {
		        output = new ObjectOutputStream(socket.getOutputStream());
		        input = new ObjectInputStream(socket.getInputStream());
		    } catch (IOException e) {
		        System.err.println("ERROR: No se pudo crear el hilo manejador de la conexión!");
		        // Intenta cerrar el socket si la inicialización de los streams falla
		        try {
		            socket.close();
		        } catch (IOException ex) {
		            System.err.println("ERROR: No se pudo cerrar el socket tras fallar la inicialización.");
		        }
		        throw new RuntimeException("Fallo al inicializar los streams de entrada/salida para el socket.", e);
		    }
		}

		/**
		 * Realiza las acciones necesarias para conectar y comunicar con un cliente,
		 * depende el tipo de mensaje de, el servidor lo realizará unas acciones u
		 * otras.
		 *
		 * @see MessageType
		 */
		@Override
		public void run() {
		    try {
		        loginUser(); // Intenta iniciar sesión del usuario
		        while (running) {
		            ChatMessage message = (ChatMessage) input.readObject(); // Lee mensajes entrantes
		            switch (message.getType()) {
		                case MESSAGE:
		                    showTypeMessage(message); // Muestra y procesa el mensaje
		                    break;
		                case LOGOUT:
		                    System.out.println("[" + getDateString() + "] Usuario desconectado: " + getUsername());
		                    remove(id); // Elimina al usuario del mapa de clientes activos
		                    shutdownClient(); // Cierra la conexión de manera segura
		                    running = false; // Detiene el bucle while
		                    break;
		                case SHUTDOWN:
		                    if (this.username.equalsIgnoreCase("ADMIN")) {
		                        System.out.println("[" + getDateString() + "] Comando de APAGADO recibido del admin. Apagando el servidor...");
		                        ChatServerImpl.this.shutdown(); // Ejecuta el método de apagado del servidor
		                        return; // Sale del bucle y termina este hilo
		                    } else {
		                        System.out.println("[" + getDateString() + "] Comando de APAGADO recibido de usuario no-admin: " + getUsername() + ". Ignorando.");
		                    }
		                    break;
		                default:
		                    // Manejar posiblemente otros tipos de mensajes aquí
		                    break;
		            }
		        }
		    } catch (ClassNotFoundException | IOException e) {
		        System.err.println("ERROR: Se perdió la conexión con el cliente " + getUsername());
		    } finally {
		        // Este bloque se ejecutará tanto si la ejecución fue exitosa como si se capturó una excepción
		        remove(id); // Asegura que el usuario se elimine del mapa de clientes activos
		        if (running) { // Solo intenta cerrar la conexión si el ciclo aún estaba activo
		            shutdownClient(); // Cierra la conexión de manera segura
		        }
		    }
		}

		/**
		 * Comprueba el mensaje y si comienza por algun comando reconocido, se puede
		 * tirar la conexión a otro usuario, banear o desbanear, cualquier usuario puede
		 * realizar estas acciones mientras no esté baneado del servidor
		 *
		 * @param message mensaje a comprobar y enviar
		 */
		private void showTypeMessage(ChatMessage message) {
		    // Si el usuario está baneado, no se aceptan mensajes suyos ni comandos
		    if (bannedUsers.getOrDefault(this.username, false)) {
		        return;
		    }

		    List<String> commandAndUser = extractCommandAndUser(message.getMessage());
		    // si se ha reconocido un comando...
		    if (!commandAndUser.isEmpty()) { // Cambio clave aquí
		        String command = commandAndUser.get(0);
		        String username = commandAndUser.get(1);
		        switch (command) {
		        case "drop":
		            dropUser(username); // elimina al usuario username por otro cliente
		            break;
		        case "ban":
		            banUser(username, true); // banea al usuario username por otro cliente
		            break;
		        case "unban":
		            banUser(username, false); // desbanea al usuario username por otro cliente
		            break;
		        default:
		            // En teoría, no deberíamos llegar nunca aquí debido a la comprobación en extractCommandAndUser
		            break;
		        }
		    } else {
		        broadcast(message); // emitimos el mensaje
		    }
		}

		/**
		 * Extrae el comando y el usuario de un mensaje, y devuelve una lista de dos
		 * elementos.
		 *
		 * @param mensaje a analizar en modo texto
		 * @return lista de dos elementos, el primero el comando y el segundo el
		 *         username
		 */
		public List<String> extractCommandAndUser(String mensaje) {
		    List<String> commands = Arrays.asList("drop", "ban", "unban");
		    // Divide el mensaje en 2 partes, usando el espacio.
		    String[] parts = mensaje.trim().split("\\s+", 2);

		    // Comprobamos que hay dos partes en el mensaje
		    if (parts.length == 2) {
		        String command = parts[0].toLowerCase(); // la primera palabra es el comando
		        if (commands.contains(command)) {
		            return Arrays.asList(command, parts[1].trim()); // la segunda palabra el username, asegurando eliminar espacios adicionales
		        }
		    }
		    return Collections.emptyList(); // devuelve una lista vacía si no es un comando válido
		}

		/**
		 * Método login, se utilzia para iniciar la conexión con el cliente, lee el
		 * primer mensaje y establece el id y username, los almacena en los mapas.
		 *
		 * @throws IOException            Signals that an I/O exception has occurred.
		 * @throws ClassNotFoundException the class not found exception
		 */
		private void loginUser() throws IOException, ClassNotFoundException {
			// Lee el primer mensaje que contiene el nombre de usuario
			ChatMessage loginMessage = (ChatMessage) input.readObject();
			if (loginMessage.getType() != MessageType.MESSAGE) {
				System.err.println(
						"ERROR: Se esperaba un mensaje de nombre de usuario, se recibió algo diferente. Cerrando conexión.");
				shutdownClient();
				return;
			}

			this.username = loginMessage.getMessage();
			// Verificamos si el username ya existe
			if (!checkUsername(getUsername())) {
				// Si el nombre de usuario ya existe, cierra la conexión y sale.
				shutdownClient(); // cerramos correctamente la conexión aquí
				System.err.println("[" + getDateString() + "] Conexión terminada para el cliente " + getUsername()
						+ ". Este nombre de usuario ya existe.");
				return; // Salir del método run sin agregar al cliente al mapa
			}

			// Si el nombre de usuario es único, procede como de costumbre
			clientsMap.put(getUsername(), this);
			this.id = getNextId();
			clientsIdMap.put(id, getUsername());
			sendInitialConnectionMessage();
			System.out.println("Clientes conectados: " + clientsMap.size());
		}

		/**
		 * Banea o desbanea a un usuario, no tiene porqué estar conectado al servidor.
		 * Recibe como parametros el nombre del usuario a banear y true si se quiere
		 * banear o false si se quiere desbanear.
		 *
		 * @param username del cliente a banear
		 * @param ban      true si es ban o false si no
		 */
		private void banUser(String username, boolean ban) {
			bannedUsers.put(username, ban);
			String currentDate = getDateString();
			String action = ban ? "baneado" : "desbaneado";
			String logMessage = String.format("[%s] El cliente %s ha sido %s por %s", currentDate, username, action,
					getUsername());
			System.out.println(logMessage);

			broadcast(new ChatMessage(this.id, MessageType.MESSAGE, logMessage));
		}

		/**
		 * Drop user, elimina la conexión de un cliente, este metodo es llamado cuando
		 * un usuario quiere tirar la conexión de otro.
		 *
		 * @param username nombre de usuario del cliente a tirar la conexion
		 */
		private void dropUser(String username) {
			ServerThreadForClient clientToDrop = clientsMap.get(username);
			String currentDate = getDateString(); // Obtener la fecha actual una sola vez para reutilizar

			if (clientToDrop != null) {
				String dropMessage = String.format("[%s] El cliente %s ha sido eliminado por %s", currentDate, username,
						getUsername());
				System.out.println(dropMessage);
				clientToDrop.shutdownClient(); // Desconecta al cliente.
				remove(clientToDrop.id); // Elimina al cliente del mapa de clientes.

				sendDropMessage(dropMessage);
			} else {
				String notFoundMessage = String.format("[%s] Usuario %s no encontrado.", currentDate, username);

				sendDropMessage(notFoundMessage);
			}
		}

		/**
		 * Envía un mensaje sobre la acción de eliminar a un usuario, ya sea por
		 * eliminación exitosa o porque no se encontró el usuario.
		 * 
		 * @param message El mensaje a enviar.
		 */
		private void sendDropMessage(String message) {
			try {
				output.writeObject(new ChatMessage(this.id, MessageType.MESSAGE, message));
			} catch (IOException e) {
				System.err.println("ERROR: Enviando mensaje de fallo de eliminación a " + getUsername());
			}
		}

		/**
		 * Comprueba si el usuario ya existe.
		 *
		 * @param username del cliente
		 * @return true si el usuario no existe
		 */
		private boolean checkUsername(String username) {
			synchronized (clientsMap) {
				if (clientsMap.containsKey(username)) {
					try {
						output.writeObject(new ChatMessage(0, MessageType.LOGOUT, "El nombre de usuario ya existe."));
					} catch (IOException e) {
						System.err.println(
								"ERROR: No se pudo enviar el mensaje de nombre de usuario existente al cliente.");
					}
					return false;
				}
				return true;
			}
		}

		/**
		 * Devuelve el username del cliente, de este cliente.
		 *
		 * @return username del cliente actual
		 */
		public String getUsername() {
			return this.username;
		}

		/**
		 * Muestra un mensaje de bienvenida al usuario, con su id.
		 */
		private void sendInitialConnectionMessage() {
			try {
				String welcomeMessage = String.format("[%s] Bienvenido(a), %s! Tu ID es %d. Esperando un mensaje...",
						getDateString(), getUsername(), id);
				output.writeObject(new ChatMessage(id, MessageType.MESSAGE, welcomeMessage));
				System.out.println("[" + getDateString() + "] " + getUsername() + " se ha conectado al servidor");
			} catch (IOException e) {
				System.err
						.println("ERROR: No se pudo enviar el mensaje de conexión inicial al cliente " + getUsername());
			}
		}

		/**
		 * Cierra las conexiones con los clientes.
		 */
		private void shutdownClient() {
			running = false; // Detener el ciclo principal o cualquier otro proceso antes de cerrar los
								// recursos

			closeQuietly(input, "input stream");
			closeQuietly(output, "output stream");
			closeQuietly(socket, "socket");
		}

		/**
		 * Cierra un recurso de forma segura sin lanzar excepciones. Imprime un error si
		 * no puede cerrar el recurso.
		 *
		 * @param resource     el recurso a cerrar (puede ser null).
		 * @param resourceName el nombre del recurso para mensajes de error.
		 */
		private void closeQuietly(AutoCloseable resource, String resourceName) {
			if (resource != null) {
				try {
					resource.close();
				} catch (Exception e) { // IOException es un subtipo de Exception
					System.err.println("Error closing the " + resourceName + " for client " + getUsername());
				}
			}
		}
	}
}
