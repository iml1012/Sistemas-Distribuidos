package es.ubu.lsi.model;


/**
 * Clase ChatMessage.
 * 
 * Clase que contiene los campos para gestionar los mensajes.
 * 
 * @author Daniel Fernández Barrientos
 * @author Ismael Manzanera López
 * 
 * @version 1.0
 */

public class ChatMessage {
	
	/** Atributos de la clase */
    private MessageType type;
    private String text; // Contenido del mensaje
    private String from; // Quien lo envia
    private String from_id; // Hora del mensaje
    private int from_level; // Nivel del usuario

    /**
     * Enum MessageType.
     * 
     * Establece los 3 tipos posibles de mensajes.
     */
    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }
    
    /** Getter y setters de la clase */
    
    /**
     * Método getType.
     * @return tipo de mensaje
     */
    public MessageType getType() {
        return type;
    }

    /**
     * Método setType.
     * @param type
     */
    public void setType(MessageType type) {
        this.type = type;
    }

    /**
     * Método getText.
     * @return
     */
    public String getText() {
        return text;
    }

    /**
     * Método setText.
     * @param text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Método getFrom.
     * @return
     */
    public String getFrom() {
        return from;
    }

    /**
     * Método setFrom.
     * @param from
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * Método getFrom_id.
     * @return
     */
	public String getFrom_id() {
		return from_id;
	}

	/**
	 * Método setFrom_id.
	 * @param from_id
	 */
	public void setFrom_id(String from_id) {
		this.from_id = from_id;
	}

	/**
	 * Método getFrom_level.
	 * @return
	 */
	public Integer getFrom_level() {
		return from_level;
	}

	/**
	 * Método setFrom_level.
	 * @param level
	 */
	public void setFrom_level(int level) {
		this.from_level = level;
	}
    
    
    
}