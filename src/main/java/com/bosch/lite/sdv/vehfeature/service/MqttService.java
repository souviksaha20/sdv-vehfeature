package com.bosch.lite.sdv.vehfeature.service;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import com.bosch.lite.sdv.vehfeature.websocket.SocketHandler;
import com.google.gson.Gson;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt.MqttClientConnectionEvents;
import software.amazon.awssdk.crt.mqtt.MqttMessage;
import software.amazon.awssdk.crt.mqtt.QualityOfService;
import software.amazon.awssdk.iot.AwsIotMqttConnectionBuilder;

@Service
public class MqttService 
{
	Logger logger = LoggerFactory.getLogger(MqttService.class);
	MqttClientConnection connection;
	private byte[] caCertPath = null;
	private byte[] inputCertPath = null;
	private byte[] inputKeyPath = null;
	private static final String ENDPOINT = "aia19hth4m2a-ats.iot.eu-central-1.amazonaws.com";
	private static final String TOPIC = "sdvlite/live/command";
	private static final String CLIENT_ID = "test_fek_03_mob_client";
	private static final int PORT = 8883;
	private SocketHandler socketHandler;
	
	@Autowired
	public void setSocketHandler(SocketHandler socketHandler) {
		this.socketHandler = socketHandler;
		logger.info("setter based dependency injection for socketHandler");
	}
	
	
	public MqttService() {
		this.setCaCertPath();
		this.setInputCertPath();
		this.setInputKeyPath();
		logger.info("CA Cert Path = " + this.caCertPath);
		logger.info("Input Key Path = " + this.inputKeyPath);
		connectToAws();
	}

	private void setCaCertPath() {
		try {
			this.caCertPath = Thread.currentThread().getContextClassLoader().getResourceAsStream("root-CA.crt")
					.readAllBytes();
		} catch (Exception e) {
			this.caCertPath = null;
			e.printStackTrace();
		}
	}

	private void setInputCertPath() {
		try {
			this.inputCertPath = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("test_fek_03.cert.pem").readAllBytes();
		} catch (Exception e) {
			this.inputCertPath = null;
			e.printStackTrace();
		}
	}

	private void setInputKeyPath() {
		try {
			this.inputKeyPath = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("test_fek_03.private.key").readAllBytes();
		} catch (Exception e) {
			this.inputKeyPath = null;
			e.printStackTrace();
		}
	}	
	
	class CustomMqttEvents implements MqttClientConnectionEvents {

		public MqttService mqttService;

		public CustomMqttEvents(MqttService mqttService) {
			this.mqttService = mqttService;
		}

		@Override
		public void onConnectionInterrupted(int errorCode) {
			if (errorCode != 0) {
				logger.error("Connection interrupted for PublishToAws :" + errorCode + ": " + CRT.awsErrorString(errorCode));
			}
		}

		@Override
		public void onConnectionResumed(boolean sessionPresent) {
			logger.info("Connection resumed for PublishToAws : " + (sessionPresent ? "existing session" : "clean session"));

		}

	}


   public void connectToAws() 
   {
	 try {
		MqttClientConnectionEvents callbacks = new CustomMqttEvents(this);
		   AwsIotMqttConnectionBuilder builder;
		   builder = AwsIotMqttConnectionBuilder.newMtlsBuilder(inputCertPath, inputKeyPath);
			builder.withConnectionEventCallbacks(callbacks).withClientId(this.CLIENT_ID).withEndpoint(this.ENDPOINT)
					.withPort((short) this.PORT).withCleanSession(true).withProtocolOperationTimeoutMs(60000);
			 this.connection = builder.build();
			 builder.close();
			 CompletableFuture<Boolean> connected = connection.connect();
			 boolean sessionPresent = connected.get(60,TimeUnit.SECONDS);
			 logger.info("Connected to " + (!sessionPresent ? "new" : "existing") + " session!");
			 // Subscribe to the topic
	          CompletableFuture<Integer> subscribed = connection.subscribe(TOPIC, QualityOfService.AT_LEAST_ONCE, (message) -> {
	                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
	                try {
						socketHandler.handleTextMessage(null, new TextMessage(message(payload)));
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            });
	            subscribed.get(60,TimeUnit.SECONDS);
	} catch (InterruptedException | ExecutionException | UnsupportedEncodingException | TimeoutException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
   }
	
   public boolean publishToAws(final String message) 
   {
	   try {
		   CompletableFuture<Integer> published = this.connection.publish
			   (new MqttMessage(TOPIC, message.getBytes(), QualityOfService.AT_LEAST_ONCE, false));
       
		   published.get(60,TimeUnit.SECONDS);
		   return true;
	} catch (InterruptedException | ExecutionException | TimeoutException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return false;
	}
	  
   } 
   public void disconnect() 
   {
	 try {
		 logger.info("disconnect is called for PublishToAws ");
		 
		 CompletableFuture<Void> disconnected = this.connection.disconnect();
         disconnected.get(60,TimeUnit.SECONDS);

         // Close the connection now that we are completely done with it.
         this.connection.close();
         logger.info("disconnect  for PublishToAws success");
		
	} catch (InterruptedException | ExecutionException | TimeoutException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
   }

   public String message(String message) {
		Gson gson = new Gson();
		Map data = gson.fromJson(message, Map.class);
		String deviceId = data.get("topic").toString().split("/")[1];
		Map send = (Map) data.get("value");
		send.put("deviceId", deviceId);
		return gson.toJson(send);
	}
}