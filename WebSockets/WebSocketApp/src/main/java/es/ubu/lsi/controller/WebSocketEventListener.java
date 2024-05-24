package es.ubu.lsi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import es.ubu.lsi.model.ChatMessage;


/**
 * Clase WebSocketEventListener.
 * 
 * Clase para comprobar que los usuarios pueden iniciar sesión en el chat.
 * 
 * @author Daniel Fernández Barrientos
 * @author Ismael Manzanera López
 * 
 * @version 1.0
 */
@Component
public class WebSocketEventListener {

	/** Variable privada para el loger de los eventos */
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    /** Inyexta SimMessageSendingOperations */
    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    /**
     * Método handleWebSocketConnectListener.
     * 
     * Gestiona el evento de conexión al socket.
     * 
     * @param event
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        logger.info("Received a new web socket connection");
    }

    /**
     * Método handleWebSocketDisconnectListener.
     * 
     * Gestiona el evento de desconexión del socket.
     * 
     * @param event
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if(username != null) {
            logger.info("User Disconnected : " + username);

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(ChatMessage.MessageType.LEAVE);
            chatMessage.setFrom(username);

            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        }
    }
}
