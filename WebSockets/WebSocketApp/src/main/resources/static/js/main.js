'use strict';

/**
 * Java Script para el manejo del chat, mensajes y conexiones.
 * 
 * @author Daniel Fernández Barrientos
 * @author Ismael Manzanera López
 * 
 * @version 2.0
 * 
 */

/** Variables que se obtienen del html. */
var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');


/** Variables globales  */
var stompClient = null;
var username = null;
var userLevel = null;
var accept = null;
var password = null;

/** Listado de colores */
var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

/**
 * Función connect.
 * 
 * 1. Recoge el evento de conexión.
 * 2. Comprueba si el usuario y contraseña son correctos.
 * 3. Recupera el nivel del usuario y lo almacena.
 * 4. Modifica las partes del html para esconder la parte de login y mostrar la de chat.
 * 
 */
function connect(event) {
	
	
    username = document.querySelector('#from').value.trim();
    password = document.querySelector('#password').value.trim();
    accept = document.querySelector('#level:checked').value.trim();
    
    console.log('Data: ', accept, username, password);
    
    if (username && password) {
        fetch(`/validateUser?username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`)
            .then(response => response.json())
            .then(isValid => {
                if (isValid) {
                    // Almacenar el nivel del usuario actual
                    fetch(`/getLevel?username=${encodeURIComponent(username)}`)
                        .then(response => response.text())
                        .then(level => {
							
                            userLevel = parseInt(level, 10);
                            console.log('Current User level: ', userLevel);

                            usernamePage.classList.add('hidden');
                            chatPage.classList.remove('hidden');

                            var socket = new SockJS('/ws');
                            stompClient = Stomp.over(socket);

                            stompClient.connect({}, onConnected, onError);
                        })
                        .catch(error => {
                            console.error('No se ha podido recoger el nivel:', error);
                        });
                } else {
					alert("El usuario no existe.");
                }
            })
            .catch(error => {
                console.error('No se ha podido validar el usuario:', error);
            });
    }
    event.preventDefault();

}

/** Función onConnected
 * 
 * Función que agrega el usuario al chat.
 * 
 */
function onConnected() {
    // Subscribe to the Public Topic
    stompClient.subscribe('/topic/public', onMessageReceived);

    // Tell your username to the server
    stompClient.send("/app/chat.addUser",
        {},
        JSON.stringify({from: username, from_level: userLevel, type: 'JOIN'})
    )

    connectingElement.classList.add('hidden');
}

/**
 * Función onError.
 * 
 * Función que muestra un error si no se consigue conectar al websocket.
 * 
 */
function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
    alert(error);
}

/**
 * Función sendMessage.
 * 
 * Función que sirve para enviar los mensajes al servidor.
 * 
 */
function sendMessage(event) {
    var messageContent = messageInput.value.trim();
    if(messageContent && stompClient) {
        var chatMessage = {
            from: username,
            text: messageInput.value,
            type: 'CHAT'
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
    event.preventDefault();
}

/**
 * Función onMessageReceived.
 * 
 * Función que lee los mensajes recibidos por el servidor y "actúa" en consecuencia:
 * 
 * - Join --> Nuevo usuario se ha unido al chat.
 * - Leave --> Usuario que abandona el chat.
 * - Resto --> Mensaje que envía cada usuario:
 * 		- Si acepta todos los mensajes --> Recibe todos los mensajes.
 * 		- Si no acepta todos los mensajes --> Recibe aquellos mensajes de niveles superiores o iguales al suyo.
 */
function onMessageReceived(payload) {
	
    var message = JSON.parse(payload.body);
    
    var fromLevel = parseInt(message.from_level, 10);
    
	var messageElement = document.createElement('li');

    if(message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        message.text = message.from + ' se ha unido! - nivel ' + fromLevel;
        var textElement = document.createElement('p');
	    var messageText = document.createTextNode(message.text);
	    textElement.appendChild(messageText);
	
	    messageElement.appendChild(textElement);
	
	    messageArea.appendChild(messageElement);
    	messageArea.scrollTop = messageArea.scrollHeight;
    	
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.text = message.from + ' se ha ido!';
        
    	var textElement = document.createElement('p');
	    var messageText = document.createTextNode(message.text);
	    textElement.appendChild(messageText);
	
	    messageElement.appendChild(textElement);
	
	    messageArea.appendChild(messageElement);
    	messageArea.scrollTop = messageArea.scrollHeight;
    	
    } else {
		if ((accept === 'si' || (accept === 'no' && fromLevel >= userLevel))){
	        messageElement.classList.add('chat-message');
	
	        var avatarElement = document.createElement('i');
	        var avatarText = document.createTextNode(message.from[0]);
	        avatarElement.appendChild(avatarText);
	        avatarElement.style['background-color'] = getAvatarColor(message.from);
	
	        messageElement.appendChild(avatarElement);
	
	        var usernameElement = document.createElement('span');
	        var usernameText = document.createTextNode(message.from_level + " - " + message.from + " " + message.from_id);
	        usernameElement.appendChild(usernameText);
	        messageElement.appendChild(usernameElement);
	        
	        var textElement = document.createElement('p');
		    var messageText = document.createTextNode(message.text);
		    textElement.appendChild(messageText);
		
		    messageElement.appendChild(textElement);
		
		    messageArea.appendChild(messageElement);
	    	messageArea.scrollTop = messageArea.scrollHeight;
	     } 
   
    }
    
    
	


}

/**
 * Funcón clearMessageArea.
 * 
 * Límpia el área de chat cada vez que se desconecta un usuario.
 * 
 */
function clearMessageArea() {
    while (messageArea.firstChild) {
        messageArea.removeChild(messageArea.firstChild);
    }
}

/**
 * Función getAvatarColor.
 * 
 * Función que devuelve el color del "avatar".
 * 
 */
function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    var index = Math.abs(hash % colors.length);
    return colors[index];
}

/** Función disconnect.
 * 
 * Función que desconecta a los usuarios y muestra de nuevo la página de login.
 * 
 */
function disconnect() {
    clearMessageArea();
    stompClient.disconnect();
    usernamePage.classList.remove('hidden');
    chatPage.classList.add('hidden');

}

/** Eventos para gestionar distintos botones del html. */
usernameForm.addEventListener('submit', connect, true);
messageForm.addEventListener('submit', sendMessage, true);
document.getElementById('disconnectButton').addEventListener('click', disconnect)
