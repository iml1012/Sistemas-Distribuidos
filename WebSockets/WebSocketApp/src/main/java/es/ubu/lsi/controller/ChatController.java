package es.ubu.lsi.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import es.ubu.lsi.model.ChatMessage;
import es.ubu.lsi.service.UserService;

/**
 * Class ChatController.
 * 
 * Clase controladora para el chat, que añade nuevos usuarios al chat o envía los mensajes al resto de destinatarios
 * 
 * @author Daniel Fernández Barrientos
 * @author Ismael Manzanera López
 * 
 * @version 1.0
 * 
 */
@Controller
public class ChatController {
	
	/** inyección del servicio de usuario */
	@Autowired
	private UserService userService;

	/**
	 * Método ChatMessage.
	 * 
	 * @param chatMessage - recibe un objeto de tipo chatMessage
	 * @return chatessage formateado con los datos necesarios
	 */
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
    	Date date = new Date();
    	String currentDate = new SimpleDateFormat().format(date);
    	chatMessage.setFrom_id(currentDate);
    	chatMessage.setFrom_level(userService.userLevel(chatMessage.getFrom()));
        return chatMessage;
    }

    /**
     * Méodo addUser.
     * 
     * Añade nuevos usuarios al chat.
     * 
     * @param chatMessage - parámetro que contiene el nombre de usuario.
     * @param headerAccessor - cabecera d
     * @return chatMessage
     */
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, 
                               SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getFrom());
        return chatMessage;
    }

}