package com.bosch.lite.sdv.vehfeature.websocket;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class SocketHandler extends TextWebSocketHandler {
	
	static List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

	@Override
	
	public void handleTextMessage(WebSocketSession session, TextMessage message)
			throws InterruptedException, IOException {
		System.out.println("Reached handleTextMessage");
		System.out.println(sessions.size());
		for(WebSocketSession webSocketSession : sessions) {
			webSocketSession.sendMessage(message);
			System.out.println("messend send to web sorket :" +message);
		}
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		super.afterConnectionEstablished(session);
		System.out.println("Reached afterConnectionEstablished");
		sessions.add(session);
		System.out.println(sessions.size());
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		super.afterConnectionClosed(session, status);
		System.out.println("Reached afterConnectionClosed");
		sessions.remove(session);
	}
}
