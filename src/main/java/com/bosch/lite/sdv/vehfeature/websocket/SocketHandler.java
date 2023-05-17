package com.bosch.lite.sdv.vehfeature.websocket;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class SocketHandler extends TextWebSocketHandler {
	 
	Logger logger = LoggerFactory.getLogger(SocketHandler.class);
	
	static List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message)
			throws InterruptedException, IOException {
		logger.info("Reached handleTextMessage");
		logger.info("session size "+sessions.size());
		for(WebSocketSession webSocketSession : sessions) {
			
			webSocketSession.sendMessage(message);
			logger.info("messend send to web sorket :" +message);
		}
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		super.afterConnectionEstablished(session);
		logger.info("Reached afterConnectionEstablished");
		sessions.add(session);
		logger.info("session size on connect "+sessions.size());
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		super.afterConnectionClosed(session, status);
		sessions.remove(session);
	}
}
