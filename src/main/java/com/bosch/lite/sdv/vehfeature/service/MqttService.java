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
	private byte[] caCertPath = null;
	private byte[] inputCertPath = null;
	private byte[] inputKeyPath = null;
	private String proxyHost = null;
	private int proxyPort = 0;

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
		this.setProxy();
		logger.info("CA Cert Path = " + this.caCertPath);
		logger.info("Input Key Path = " + this.inputKeyPath);
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

	private void setProxy() {
		this.proxyHost = "";
		this.proxyPort = 0;
	}

	static void onApplicationFailure(Throwable cause) {
		System.out.println("Throwable Exception encountered: " + cause.toString());
	}

	public void publish(final String message) {

		this.connection.publish(new MqttMessage(TOPIC, message.getBytes(), QualityOfService.AT_LEAST_ONCE, false));
		logger.info("message send to AWS");
	}

	public void disconnect() {
		try {
			CompletableFuture<Void> disconnected = this.connection.disconnect();
			disconnected.get(1,TimeUnit.SECONDS);
			
		} catch (Exception e) {
			logger.info("Error from disconnected");
		}
		
	}

	public boolean isNotConnected() {
		return connection == null;
	}

	class CustomMqttEvents implements MqttClientConnectionEvents {

		public MqttService mqttService;

		public CustomMqttEvents(MqttService mqttService) {
			this.mqttService = mqttService;
		}

		@Override
		public void onConnectionInterrupted(int errorCode) {
			if (errorCode != 0) {
				logger.error("Connection interrupted: " + errorCode + ": " + CRT.awsErrorString(errorCode));
			}
		}

		@Override
		public void onConnectionResumed(boolean sessionPresent) {
			reConnect();
			logger.info("Connection resumed: " + (sessionPresent ? "existing session" : "clean session"));

		}

	}

	MqttClientConnectionEvents callbacks = new CustomMqttEvents(this);

	public void connect() {
		AwsIotMqttConnectionBuilder builder;
		try {
			builder = AwsIotMqttConnectionBuilder.newMtlsBuilder(inputCertPath, inputKeyPath);
			builder.withConnectionEventCallbacks(callbacks).withClientId(this.CLIENT_ID).withEndpoint(this.ENDPOINT)
					.withPort((short) this.PORT).withCleanSession(true).withProtocolOperationTimeoutMs(60000);

			this.connection = builder.build();
			builder.close();

			CompletableFuture<Boolean> connected = this.connection.connect();
			boolean sessionPresent;
			sessionPresent = connected.get(1,TimeUnit.SECONDS);
			logger.info("Connected to " + (!sessionPresent ? "new" : "existing") + " session!");
			subscribedAws();

		} catch (UnsupportedEncodingException | InterruptedException | ExecutionException | TimeoutException e) {
			// TODO Auto-generated catch block
			logger.error("in conection trying to cone conect "+e);
			reConnect();
		}

	}
	public void reConnect() {
		try {
			subscribedAws();
			logger.info("Connection resumed with subscribed");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("in conection trying to cone conect "+e);
			reConnect();
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

	public void subscribedAws() {
		// Subscribe to the topic
		try {
			CompletableFuture<Integer> subscribed = this.connection.subscribe(TOPIC, QualityOfService.AT_LEAST_ONCE,
					(message) -> {
						String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
						try {
							logger.info("sending the value to websorket" + payload);
							socketHandler.handleTextMessage(null, new TextMessage(message(payload)));
						} catch (InterruptedException e) {
							logger.error("Subscribe " + e);
						} catch (Exception e) {
							logger.error("Subscribe IOException " + e);
						}
					});
			logger.info(subscribed.get(1,TimeUnit.SECONDS)+ " <------subscribed back");
			logger.info("Subscribed to the topic is done");
		} catch (Exception e) {
			disconnect();
			connect();
		}
	}
}
