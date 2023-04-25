package com.bosch.lite.sdv.vehfeature.bean;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix="spring")
@Configuration
public class Property {


	private String inputCa;
	private String endPoint;
	private String clientId;
	private String inputCert;
	private String inputKey;
	private String awsTopic;
	private String awsport;

	public String getInputCa() {
		return inputCa;
	}

	public void setInputCa(String inputCa) {
		this.inputCa = inputCa;
	}

	
	public String getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getInputCert() {
		return inputCert;
	}

	public void setInputCert(String inputCert) {
		this.inputCert = inputCert;
	}

	public String getInputKey() {
		return inputKey;
	}

	public void setInputKey(String inputKey) {
		this.inputKey = inputKey;
	}

	public String getAwsTopic() {
		return awsTopic;
	}

	public void setAwsTopic(String awsTopic) {
		this.awsTopic = awsTopic;
	}

	public String getAwsport() {
		return awsport;
	}

	public void setAwsport(String awsport) {
		this.awsport = awsport;
	}

}
