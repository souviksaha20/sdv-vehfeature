package com.bosch.lite.sdv.vehfeature.service;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import com.bosch.lite.sdv.vehfeature.websocket.SocketHandler;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt.MqttClientConnectionEvents;
import software.amazon.awssdk.crt.mqtt.MqttMessage;
import software.amazon.awssdk.crt.mqtt.QualityOfService;
import software.amazon.awssdk.iot.AwsIotMqttConnectionBuilder;

@Service
public class MqttService {
	
	MqttClientConnection connection;
	
	private static final String ENDPOINT = "aia19hth4m2a-ats.iot.eu-central-1.amazonaws.com";
	private static final String TOPIC = "sdvlite/live/command";
	private static final String CLIENT_ID = "test_fek_03_mob_client"; 
	private static final int PORT = 8883;
	private String caCertPath = null;
	private String inputCertPath = null;
	private String inputKeyPath = null;
	private String proxyHost = null;
	private int proxyPort = 0;
	
	private SocketHandler socketHandler;
	@Autowired
    public void setSocketHandler(SocketHandler socketHandler) {
        this.socketHandler = socketHandler;
        System.out.println("setter based dependency injection for socketHandler");
    }
	
	@SuppressWarnings("unused")
	private byte[] getFileAsByteStream(final String file) 
	{
		byte filedate[]= {};
		try {
			filedate =Thread.currentThread().getContextClassLoader().getResourceAsStream(file).readAllBytes();
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
		System.out.println("CA Cert Path = "+this.caCertPath);
		System.out.println("Input Cert Path = "+this.inputCertPath);
		System.out.println("Input Key Path = "+this.inputKeyPath);
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
		System.out.println("Exception encountered: " + cause.toString());
	}
	
	public void publish(final String message) {
		
		connection.publish(new MqttMessage(TOPIC, message.getBytes(), QualityOfService.AT_LEAST_ONCE, false));
	}
	
	public void disconnect() {
		try {
			System.out.println(connection);
			connection.disconnect();
			// Close the connection now that we are completely done with it.
			connection.close();
			System.out.println("Connection closed!!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Complete!");
	}
	
	public boolean isNotConnected() {
		return connection == null;
	}

	public void connect() {

		MqttClientConnectionEvents callbacks = new MqttClientConnectionEvents() {
			@Override
			public void onConnectionInterrupted(int errorCode) {
				if (errorCode != 0) {
					System.out.println("Connection interrupted: " + errorCode + ": " + CRT.awsErrorString(errorCode));
				}
			}

			@Override
			public void onConnectionResumed(boolean sessionPresent) {
				System.out.println("Connection resumed: " + (sessionPresent ? "existing session" : "clean session"));
			}
		};

		try {

			/**
			 * Create the MQTT connection from the builder
			 */
//			AwsIotMqttConnectionBuilder builder = AwsIotMqttConnectionBuilder.newMtlsBuilder(getFileAsByteStream("test_fek_03.cert.pem"), getFileAsByteStream("test_fek_03.private.key"));
			
//			if (this.caCertPath != "") {
//				builder.withCertificateAuthorityFromPath(null, this.caCertPath);
//			}
			AwsIotMqttConnectionBuilder builder = AwsIotMqttConnectionBuilder.newMtlsBuilder(getFileAsByteStream(this.inputCertPath), getFileAsByteStream(this.inputKeyPath));
			builder.withConnectionEventCallbacks(callbacks)
            .withClientId(this.CLIENT_ID)
            .withEndpoint(this.ENDPOINT)
            .withPort((short)this.PORT)
            .withCleanSession(true)
            .withProtocolOperationTimeoutMs(60000);
			if (this.proxyHost != "" && this.proxyPort > 0) {
				HttpProxyOptions proxyOptions = new HttpProxyOptions();
				proxyOptions.setHost(this.proxyHost);
				proxyOptions.setPort(this.proxyPort);
				builder.withHttpProxyOptions(proxyOptions);
			}
			connection = builder.build();
			builder.close();

			// Connect the MQTT client
			CompletableFuture<Boolean> connected = connection.connect();
			try {
				boolean sessionPresent = connected.get();
				System.out.println("Connected to " + (!sessionPresent ? "new" : "existing") + " session!");
			} catch (Exception ex) {
				throw new RuntimeException("Exception occurred during connect", ex);
			}

			// Subscribe to the topic
			CompletableFuture<Integer> subscribed = connection.subscribe(TOPIC, QualityOfService.AT_LEAST_ONCE,
					(message) -> {
						String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
						try {
							socketHandler.handleTextMessage(null, new TextMessage(payload));
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
			subscribed.get();

		} catch (Exception ex) {
			onApplicationFailure(ex);
		}
	}
}
