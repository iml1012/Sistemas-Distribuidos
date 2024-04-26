package es.ubu.lsi.server;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * The Class ChatServerStarter.
 * 
 * @author Ismael Manzanera López
 * @author Daniel Fernández Barrientos
 */
public class ChatServerStarter {
	/**
	 * Constructor de la clase ChatServerStarter que llama al metodo start para
	 * crear un objeto de este tipo.
	 */
	public ChatServerStarter() {
		start();
	}

	/**
	 * Crea un nuevo servidor lo exporta, lo registra y vincula al dominio /servidor
	 * para que los clientes puedan utilizar sus metodos de forma remota.
	 * 
	 */
	public void start() {
		try {
			ChatServerImpl servidor = new ChatServerImpl();
			// Se exporta el servidor
			ChatServer stub = (ChatServer) UnicastRemoteObject.exportObject(servidor, 0);
			// Se registra el stub obtenido por el servidor
			Registry register = LocateRegistry.getRegistry();
			// Se vincula a un dominio
			register.rebind("/servidor", stub);
			System.out.println("\nServidor ejecutandose...");
			System.out.println("<<<------>>>");
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}
}
