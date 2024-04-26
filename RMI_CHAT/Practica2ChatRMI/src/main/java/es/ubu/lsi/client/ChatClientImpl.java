package es.ubu.lsi.client;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import es.ubu.lsi.common.ChatMessage;
import es.ubu.lsi.server.ChatServer;

/**
 * Chat client implementation.
 * 
 * @author Ismael Manzanera López
 * @author Daniel Fernández Barrientos
 */
public class ChatClientImpl implements ChatClient {

	/** usuario. */
	private String nickName;

	/** Servidor al que se conecta. */
	private ChatServer servidor;

	/** Id del cliente. */
	private int id;

	/** Formato de fecha. */
	private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	/**
	 * Constructor de la clase ChatClientImpl.
	 * 
	 * Representa a un cliente del servidor.
	 *
	 * @param nickName nombre de usuario
	 * @throws RemoteException lanza excepción si ocurre error en la comunicación
	 */
	protected ChatClientImpl(String nickName) throws RemoteException {
		super();
		this.nickName = nickName;

	}

	/**
	 * Devuelve el servidor.
	 *
	 * @return servidor
	 */
	public ChatServer getServidor() {
		return servidor;
	}

	/**
	 * Establece el servidor.
	 *
	 * @param servidor reemoto
	 */
	public void setServidor(ChatServer servidor) {
		this.servidor = servidor;
	}

	/**
	 * Devuelve el ID del cliente.
	 *
	 * @return id
	 * @throws RemoteException the remote exception
	 */
	public int getId() throws RemoteException {

		return this.id;
	}

	/**
	 * Establece un ID recibido por parámetro al cliente.
	 *
	 * @param id the new id
	 * @throws RemoteException the remote exception
	 */
	public void setId(int id) throws RemoteException {
		this.id = id;
	}

	/**
	 * Recibe un mensaje escrito por un cliente y lo muestra.
	 *
	 * @param msg the msg
	 * @throws RemoteException the remote exception
	 */
	public void receive(ChatMessage msg) throws RemoteException {
		System.out.println("[" + getDateString() + "] " + msg.getNickname() + ": " + msg.getMessage());
	}

	/**
	 * Devuelve el usuario del cliente.
	 *
	 * @return usuario alias del cliente
	 * @throws RemoteException the remote exception
	 */

	public String getNickName() throws RemoteException {
		return this.nickName;
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

}
