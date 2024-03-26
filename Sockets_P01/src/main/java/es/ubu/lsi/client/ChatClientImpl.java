package es.ubu.lsi.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

import es.ubu.lsi.common.ChatMessage;
import es.ubu.lsi.common.MessageType;

public class ChatClientImpl implements ChatClient {

	/** Servidor. */
	private String server;

	/** Nombre del usuario. */
	private String username;

	/** Puerto de conexion. */
	private int port;

	/** ID. */
	private static int id;

	/** Booleano carry on, indica el permiso para leer del canal. */
	private boolean carryOn = true;

	/** Socket del cliente. */
	private Socket socket;

	/** Salida. */
	ObjectOutputStream outputStream;

	/** Entrada */
	ObjectInputStream inputStream;

	/** Entrada por teclado. */
	private Scanner input;

	/**
	 * Constructor de la clase ChatClientImpl.
	 *
	 * @param server   IP del servidor al que se conecta el cliente.
	 * @param port     Puerto del servidor al que envia las peticiones.
	 * @param username Nombre de usuario con el que se conecta.
	 */
	public ChatClientImpl(String server, int port, String username) {
		this.server = server;
		this.port = port;
		this.username = username;

		try {

			this.socket = new Socket(this.server, this.port);
			outputStream = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.err.println("ERROR: No se puede lanzar el cliente! Saliendo...");
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Inicia el cliente y conecta este cliente con el servidor.
	 *
	 * @return true, si no ha habido error.
	 */
	@Override
	public boolean start() {
		try {
			connect(); // Conectarse al servidor y enviar el nombre de usuario
			try (Scanner input = new Scanner(System.in)) { // Preparar para leer mensajes del teclado
				while (carryOn) {
					String text = input.nextLine(); // Lee la entrada del usuario

					switch (text.toUpperCase()) { // Usar switch en lugar de if-else para claridad
					case "LOGOUT":
						sendMessage(new ChatMessage(id, MessageType.LOGOUT, ""));
						return true; // Termina la ejecución después de la desconexión

					case "SHUTDOWN":
						System.out.println("Enviando apagado al servidor...");
						sendMessage(new ChatMessage(id, MessageType.SHUTDOWN, ""));
						return true; // Considera el cierre después de enviar el comando de apagado

					default:
						sendMessage(new ChatMessage(id, MessageType.MESSAGE, text));
						break;
					}
				}
			} // El scanner se cierra automáticamente gracias al try-with-resources
		} finally {
			disconnect(); // Asegurar que el cliente se desconecte correctamente al salir
		}
		return true;
	}

	/**
	 * Envia un mensaje al servidor.
	 *
	 * @param msg Mensaje a enviar.
	 */
	@Override
	public void sendMessage(ChatMessage msg) {
		try {
			outputStream.writeObject(msg); // Envia el mensaje por el canal de salida
		} catch (IOException e) {
			System.err.println("ERROR: No se ha podido enviar el mensaje al servidor.");
			e.printStackTrace(); // Muestra la traza de la excepcion
			disconnect();
		}
	}

	/**
	 * Desconecta el cliente del servidor.
	 */
	@Override
	public void disconnect() {
		carryOn = false; // Dejamos de leer del canal y aseguramos que el Listener se detenga primero

		closeResource(input); // Cierra el Scanner si no es nulo
		closeResource(outputStream); // Cierra el OutputStream si no es nulo
		closeResource(socket); // Cierra el Socket si no es nulo
	}

	private void closeResource(AutoCloseable resource) {
		if (resource != null) {
			try {
				resource.close();
			} catch (Exception e) { // IOException es suficiente para los recursos de red, pero
									// AutoCloseable.close() lanza Exception
				e.printStackTrace();
			}
		}
	}

	/**
	 * Envia una peticion de login al servidor y se queda a la espera de recibir
	 * respuesta.
	 */
	private void connect() {
		ChatMessage msg = new ChatMessage(0, MessageType.MESSAGE, username);

		try {
			inputStream = new ObjectInputStream(socket.getInputStream());
			sendMessage(msg);
			msg = (ChatMessage) inputStream.readObject();

			System.out.println(msg.getMessage());
			if (msg.getType() == MessageType.LOGOUT) {
				System.out.println("Desconectando el cliente...");
				disconnect();
				System.exit(0);
			}
			// Establecemos el id del cliente con el id otorgado por el servidor
			id = msg.getId();
			// System.out.println("Cliente: ID del mensaje recibido del server: " + id);
			new Thread(new ChatClientListener(inputStream, id)).start();
		} catch (IOException | ClassNotFoundException e) {
			System.err.println("ERROR: No se recibe respuesta por el servidor");
			disconnect();
			System.exit(1);
		}
	}

	/**
	 * Muestra un mensaje deayuda para el uso del programa Cliente.
	 */
	private static void printHelp() {
		System.out.println("AYUDA:");
		System.out.println("\tmvn exec:java@server");
		System.out.println("\tmvn exec:java@cliente-blas");
		System.out.println("\tmvn exec:java@cliente-pio");
		System.out.println("\tmvn exec:java@cliente-admin");
	}

	/**
	 * Metodo principal de ejecucion del cliente
	 *
	 * @param args argumentos de entrada del programa cliente.
	 */
	public static void main(String[] args) {
		final int port = 1500; // Puerto predeterminado para la conexión
		String server = "localhost"; // Servidor predeterminado
		String username = "Anonimo"; // Nombre de usuario predeterminado

		// Verificar el número correcto de argumentos
		if (args.length == 2) {
			server = args[0];
			username = args[1];
		} else if (args.length != 0) {
			// Si hay un número de argumentos distinto de 2 y distinto de 0, se muestra un
			// mensaje de error y la ayuda
			System.err.println("Error: Número incorrecto de parámetros.");
			printHelp();
			System.exit(1);
		}

		// Si no hay errores en los argumentos, iniciar el cliente
		new ChatClientImpl(server, port, username).start();
	}

	/**
	 * Clase interna ChatClientListener, crea un hilo de escucha continuamente los
	 * mensajes que entran del servidor y los muestra al usuario del cliente del
	 * chat. Implementa la interfaz Runnable.
	 *
	 * @see ChatClientImpl
	 */
	class ChatClientListener implements Runnable {

		/** Input. */
		ObjectInputStream serverInput;

		/**
		 * Constructor.
		 *
		 * @param in Canal de entrada
		 */
		public ChatClientListener(ObjectInputStream in, int id) {
			this.serverInput = in;
		}

		/**
		 * Escucha en el canal de entrada los mensajes que provienen del servidor.
		 */
		@Override
		public void run() {
			try {
				while (carryOn) { // Mientras pueda leer del canal de entrada
					ChatMessage msg = (ChatMessage) serverInput.readObject();
					System.out.println(msg.getMessage());
				}
			} catch (IOException e) {
				System.err.println("ERROR: Conexión perdida...");
				carryOn = false;
			} catch (ClassNotFoundException e) {
				System.err.println("ERROR: Servidor no disponible...");
			} finally {
				try {
					serverInput.close();
					System.out.println("Apagando cliente...");
					disconnect();
				} catch (IOException e2) {
					System.err.println("ERROR: No se puede cerrar la conexión.");
				}
			}
		}
	}
}