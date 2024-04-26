package es.ubu.lsi.server;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import es.ubu.lsi.client.ChatClient;
import es.ubu.lsi.common.ChatMessage;

/**
 * The Class ChatServerImpl.
 * 
 * @author Ismael Manzanera López
 * @author Daniel Fernández Barrientos
 */
public class ChatServerImpl implements ChatServer {

	/** Contador de clientes que inician en el servidor. */
	private int contador;

	/** The clientes. */
	// Mapa para almacenar los clientes por su nickname
	private ConcurrentHashMap<String, ChatClient> clientes = new ConcurrentHashMap<>();

	/** Mapa con los clientes baneados. */
	private ConcurrentHashMap<String, Boolean> clientesBaneados = new ConcurrentHashMap<String, Boolean>();

	/** Formato de fecha. */
	private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	/**
	 * Constructor ChatServerImpl que crea un nuevo servidor. Incializa el contador
	 * de clientes online en 0.
	 * 
	 * @throws RemoteException excepcion si existe problema en la comunicacion
	 * 
	 * @see ChatServer
	 */
	protected ChatServerImpl() throws RemoteException {
		super();
		contador = 0;

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
	 * Registra un nuevo usuario en el servidor y se le asigna un Id. Si el cliente
	 * tiene un nickname ya existente se rechaza su conexión al servidor.
	 *
	 * @param client cliente del servidor
	 * @return Id del cliente
	 * @throws RemoteException the remote exception
	 * @see ChatClient
	 */
	public int checkIn(ChatClient client) throws RemoteException {
    String currentDate = getDateString(); // Obtener la fecha una vez para reutilizarla y reducir llamadas
    String nicknameLower = client.getNickName().toLowerCase();
    System.out.println("[" + currentDate + "] CheckIn usuario con nickname: " + nicknameLower);

    // Verificar si el nickname ya está registrado para evitar duplicados
    if (clientes.containsKey(nicknameLower)) {
        System.out.println("[" + currentDate + "] Usuario duplicado detectado: " + nicknameLower);
        throw new RemoteException("[" + currentDate + "] Este usuario ya está en uso.");
    }

    // Configurar el ID del cliente y actualizar el contador de forma segura
    client.setId(contador++);
    clientes.put(nicknameLower, client);

    // Registro de éxito
    System.out.println("[" + currentDate + "] Usuario: " + nicknameLower + " registrado con éxito!");
    System.out.println("[" + currentDate + "] Usuarios online: " + clientes.size());

    return client.getId();
}


	/**
	 * Indica si un cliente abandona el chat y lo borra de la lista de clientes
	 * conectados.
	 *
	 * @param client cliente que se loguea
	 * @throws RemoteException the remote exception
	 */
	public void logout(ChatClient client) throws RemoteException {

		// Convertimos el nickname todo en minusculas para compararlos
		String nicknameMinus = client.getNickName().toLowerCase();

		// Si el cliente existe lo eliminamos removemos del mapa
		if (clientes.remove(nicknameMinus) != null) {
			System.out.println("[" + getDateString() + "] " + "El usuario " + client.getNickName() + " ha salido.");
			System.out.println("[" + getDateString() + "] " + "Usuario conectados: " + clientes.size());
		}

		// Mensaje para los clientes
		client.receive(new ChatMessage(-1, "SERVER", "Cerrando sesión..."));
	}

	/**
	 * Envia un mensaje a cada uno de los clientes excepto al cliente que envia
	 * dicho mensaje.
	 *
	 * @param msg mensaje que recibe
	 * @throws RemoteException the remote exception
	 * @see ChatMessage
	 * @see ChatClient
	 */
  public void publish(ChatMessage msg) throws RemoteException {
      // Verificar y gestionar el estado de baneo del emisor del mensaje
      if (!manageSenderBan(msg)) {
          return; // Si está baneado, no se procederá a enviar su mensaje
      }
  
      String senderNickname = msg.getNickname().toLowerCase();
      String currentDate = getDateString(); // Usar una sola vez por llamada al método
  
      // Envío del mensaje a todos los clientes excepto al emisor
      for (ChatClient client : clientes.values()) {
          if (!client.getNickName().equalsIgnoreCase(senderNickname)) {
              try {
                  client.receive(msg);
              } catch (RemoteException e) {
                  System.err.println("[" + currentDate + "] Error al enviar mensaje a " + client.getNickName());
              }
          }
      }
  }

  /**
   * Manage sender ban.
   *
   * @param msg the msg
   * @return true, if successful
   * @throws RemoteException the remote exception
   */
  private boolean manageSenderBan(ChatMessage msg) throws RemoteException {
      if (testBan(msg.getNickname())) {
          return false; // Si el emisor está baneado, se devuelve false.
      }
      banClient(msg); // Gestiona posibles acciones de baneo según el contenido del mensaje.
      return true; // Si no está baneado, se procede.
  }


	/**
	 * Comprueba si un cliente está baneado según su nickname.
	 *
	 * @param nickname del cliente
	 * @return true, si esta baneado
	 */
	private boolean testBan(String nickname) {
		return clientesBaneados.containsKey(nickname.toLowerCase());

	}

	/**
	 * Recibe un mensaje como argumento de entrada y comprueba si cumple con la
	 * estructura de un baneo o desbaneo. Si es asi, procede a añadir o elimianr del
	 * mapa el nickname del cliente a banear/desbanear.
	 * 
	 * @param msg mensaje a comprobar
	 * @see ChatMessage
	 */
  private void banClient(ChatMessage msg) {
      String[] parts = msg.getMessage().split(" ");
      // Asegurarse de que el mensaje tiene exactamente dos partes y la primera
      // parte es ban o unban
      if (parts.length != 2) {
    	  return;  
      } 

      String command = parts[0].toLowerCase(); // Comando (ban/unban)
      String nicknameToBan = parts[1].toLowerCase(); // Nickname del usuario a banear/desbanear
      String senderNickname = msg.getNickname().toLowerCase(); // Nickname del emisor en minúsculas
  
      // Verificar que no se intente banear o desbanear a sí mismo
      if (nicknameToBan.equals(senderNickname)) return;
  
      boolean isBanCommand = command.equalsIgnoreCase("ban");
      boolean isUnbanCommand = command.equalsIgnoreCase("unban");
  
      if (isBanCommand || (isUnbanCommand && clientesBaneados.containsKey(nicknameToBan))) {
          handleBanUnban(command, nicknameToBan, senderNickname);
      } else {
          //System.out.println("[" + getDateString() + "] Intento fallido de " + (isUnbanCommand ? "desbanear" : "banear") + " a " + nicknameToBan);
      }
  }

  /**
   * Handle ban unban.
   *
   * @param command the command
   * @param nicknameToBan the nickname to ban
   * @param senderNickname the sender nickname
   */
  private void handleBanUnban(String command, String nicknameToBan, String senderNickname) {
      String operationResult = "";
      boolean isBan = command.equalsIgnoreCase("ban");
  
      if (isBan) {
          clientesBaneados.put(nicknameToBan, true);
          operationResult = "baneado";
      } else {
          clientesBaneados.remove(nicknameToBan);
          operationResult = "desbaneado";
      }
  
      String message = "El usuario " + nicknameToBan + " ha sido " + operationResult + ".";
      try {
          publish(new ChatMessage(-1, "SERVER", message));
      } catch (RemoteException e) {
          System.out.println(e.getMessage());
      }
  
      System.out.println("[" + getDateString() + "] " + senderNickname + " ha " + operationResult + " a " + nicknameToBan);
  }


	/**
	 * Cierra todos los clientes y apaga el servidor.
	 *
	 * @param client cliente
	 * @throws RemoteException the remote exception
	 */
	public void shutdown(ChatClient client) throws RemoteException {
		// Enviar mensaje de cierre a todos los clientes
		for (ChatClient c : clientes.values()) {
			try {
				c.receive(new ChatMessage(-1, "SERVER", "El servidor se está cerrando..."));
				logout(c); // Desconectamos a todos los clientes
			} catch (RemoteException e) {
				System.err.println("[" + getDateString() + "] " + "Error enviando mensaje de cierre al usuario "
						+ c.getNickName());
			}
		}
		// Limpia el mapa de clientes después de enviar el mensaje de cierre
		clientes.clear();
		System.out.println(
				"[" + getDateString() + "] " + client.getNickName() + " ha apagado el servidor.\nAPAGANDO....");
		// El servidor se detendra abruptamente y finaliza la JVM.
		System.exit(0); // Cierre total de JVM
	}

}
