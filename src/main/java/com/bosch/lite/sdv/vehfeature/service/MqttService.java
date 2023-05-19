package com.bosch.lite.sdv.vehfeature.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import com.bosch.lite.sdv.vehfeature.websocket.SocketHandler;
import com.google.gson.Gson;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt.MqttClientConnectionEvents;
import software.amazon.awssdk.crt.mqtt.MqttMessage;
import software.amazon.awssdk.crt.mqtt.QualityOfService;
import software.amazon.awssdk.iot.AwsIotMqttConnectionBuilder;

@Service
public class MqttService {

	public MqttClientConnection connection;
	Logger logger = LoggerFactory.getLogger(MqttService.class);

	private static final String ENDPOINT = "aia19hth4m2a-ats.iot.eu-central-1.amazonaws.com";
	private static final String TOPIC = "sdvlite/live/command";
	private static final String CLIENT_ID = "test_fek_03_mob_client";
	private static final int PORT = 8883;
	private String caCertPath = null;
	private String inputCertPath = null;
	private String inputKeyPath = null;
	private String proxyHost = null;
	private int proxyPort = 0;
	public AwsIotMqttConnectionBuilder builder;
	public boolean sessionPresent=false;

	private SocketHandler socketHandler;

	@Autowired
	public void setSocketHandler(SocketHandler socketHandler) {
		this.socketHandler = socketHandler;
		logger.info("setter based dependency injection for socketHandler");
	}

	@SuppressWarnings("unused")
	private byte[] getFileAsByteStream(final String file) {
		byte filedate[] = {};
		try {
			if(file==null ||file.isEmpty())
				System.out.println("it is emty");
			filedate = Thread.currentThread().getContextClassLoader().getResourceAsStream(file).readAllBytes();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return filedate;
	}

	public MqttService() {
		this.setCaCertPath();
		this.setInputCertPath();
		this.setInputKeyPath();
		this.setProxy();
		logger.info("CA Cert Path = " + this.caCertPath);
		logger.info("Input Key Path = " + this.inputKeyPath);
		try {
			this.builder=AwsIotMqttConnectionBuilder.newMtlsBuilder(getFileAsByteStream(this.inputCertPath), getFileAsByteStream(this.inputKeyPath));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setCaCertPath() {
		try {
			this.caCertPath = "root-CA.crt";
		} catch (Exception e) {
			this.caCertPath = "";
			e.printStackTrace();
		}
	}

	private void setInputCertPath() {
		try {
			this.inputCertPath = "test_fek_03.cert.pem";
		} catch (Exception e) {
			this.inputCertPath = "";
			e.printStackTrace();
		}
	}

	private void setInputKeyPath() {
		try {
			this.inputKeyPath = "test_fek_03.private.key";
		} catch (Exception e) {
			this.inputKeyPath = "";
			e.printStackTrace();
		}
	}

	private void setProxy() {
		this.proxyHost = "";
		this.proxyPort = 0;
	}

	static void onApplicationFailure(Throwable cause) {
		System.out.println("Throwable Exception encountered: " + cause.toString());
	}

	public void publish(final String message) {

		connection.publish(new MqttMessage(TOPIC, message.getBytes(), QualityOfService.AT_LEAST_ONCE, false));
		logger.info("message send to AWS");
	}

	public void disconnect() {
		try {
			logger.info(" " + connection);
			connection.disconnect();
			// Close the connection now that we are completely done with it.
			connection.close();
			this.builder.close();
			logger.info("Connection closed!!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("Complete!");
	}

	public boolean isNotConnected() {
		return connection == null;
	}
	
   class  CustomMqttEvents implements MqttClientConnectionEvents{
	   
	   public  MqttService mqttService;
	   public CustomMqttEvents(MqttService mqttService) {
		   this.mqttService=mqttService;
	   }

	   @Override
		public void onConnectionInterrupted(int errorCode) {
			if (errorCode != 0) {
				logger.error("Connection interrupted: " + errorCode + ": " + CRT.awsErrorString(errorCode));
			}
		}

	   @Override
		public void onConnectionResumed(boolean sessionPresent) {
			logger.info("Connection resumed: " + (sessionPresent ? "existing session" : "clean session"));
			if(!sessionPresent)
			{
				
//				this.mqttService.connect();
				this.mqttService.subscribedAws(false);
				logger.error("conected");
			}
				
			
		}
	   
   }
	MqttClientConnectionEvents callbacks = new CustomMqttEvents(this); 	
	
	public void connect() {
		
		try {
			this.builder.withConnectionEventCallbacks(callbacks)
            .withClientId(this.CLIENT_ID)
            .withEndpoint(this.ENDPOINT)
            .withPort((short)this.PORT)
            .withCleanSession(true)
            .withKeepAliveSecs(8640);
			if (this.proxyHost != "" && this.proxyPort > 0) {
				HttpProxyOptions proxyOptions = new HttpProxyOptions();
				proxyOptions.setHost(this.proxyHost);
				proxyOptions.setPort(this.proxyPort);
				builder.withHttpProxyOptions(proxyOptions);
			}
			connection = this.builder.build();
			

			// Connect the MQTT client
			
			CompletableFuture<Boolean> connected = connection.connect();
			try {
				 sessionPresent = connected.get();
				logger.info("i Connected to " + (!sessionPresent ? "new" : "existing") + " session!"+ sessionPresent);
			} catch (Exception ex) {
				logger.info("error mqtt aws "+ex);
				connection.disconnect();
				connection.close();
				connect();
				try {
					sessionPresent = connected.get();
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return ;
				}
			}
			
			subscribedAws(true);

		} catch (Exception ex) {
			onApplicationFailure(ex);
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
	public void subscribedAws(boolean data)
	{
		if(!data)
		{
			logger.info("error mqtt aws "+data);
			connection.disconnect();
			connection.close();
			connect();
		}
		// Subscribe to the topic
		try {
			CompletableFuture<Integer> subscribed = connection.subscribe(TOPIC, QualityOfService.AT_LEAST_ONCE,
					(message) -> {
						String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
						try {
							logger.info("sending the value to websorket"+payload);
							socketHandler.handleTextMessage(null, new TextMessage(message(payload)));
						} catch (InterruptedException e) {
							logger.error("Subscribe " + e);
						} catch (Exception e) {
							logger.error("Subscribe IOException " + e);
						}
					});
			subscribed.get();
			System.out.println("conected");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
