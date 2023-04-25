package com.bosch.lite.sdv.vehfeature.bean;



public class Payload {
	String topic = "com.bosch.sdvlite.preprod/test_fek_03_mob_client/things/live/messages/featureMessage";
	Headers headers;
	String path;
	Value value;
	
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public Headers getHeaders() {
		return headers;
	}
	public void setHeaders(Headers headers) {
		this.headers = headers;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Value getValue() {
		return value;
	}
	public void setValue(Value value) {
		this.value = value;
	}
	
	
	

}
