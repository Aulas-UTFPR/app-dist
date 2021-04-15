package br.edu.utfpr.fb.controller;

import br.edu.utfpr.fb.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;


@Component
public class WebSocketEventListener {
	
	public WebSocketEventListener() {
		// TODO Auto-generated constructor stub
		this.users.add("michel");
		this.users.add("edinei");
		this.users.add("marlon");
	}

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    // Authorized Users
    List<String> users = new ArrayList<String>();
    
    @Autowired
    private SimpMessageSendingOperations messagingTemplate;
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
    	StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    	System.out.println(headerAccessor.getSessionAttributes());
    	logger.info("Received a new web socket connection");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if(username != null) {
            logger.info("User Disconnected : " + username);

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(ChatMessage.MessageType.LEAVE);
            chatMessage.setSender(username);

            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        }
    }
}