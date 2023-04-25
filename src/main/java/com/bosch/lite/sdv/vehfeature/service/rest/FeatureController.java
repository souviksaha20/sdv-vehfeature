package com.bosch.lite.sdv.vehfeature.service.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.bosch.lite.sdv.vehfeature.bean.Payload;
import com.bosch.lite.sdv.vehfeature.service.MqttService;
import com.google.gson.Gson;

@CrossOrigin
@RestController
public class FeatureController {
	
	
	@Autowired
	private MqttService mqttService;
	
	@PostMapping("/send/featureMessage")
	public ResponseEntity<String> sendFeatureMessage(@RequestBody Payload payload) {
		try {
			 String data=new Gson().toJson(payload);
			mqttService.publish(data.toString());
			return ResponseEntity.status(HttpStatus.OK).body("message send");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("message failed");
		}
	}
	
//	@PostMapping("/doorlock")
//	public String doorlock(@RequestBody DoorLockBean doorLock) {
//		JSONObject returnObj = new JSONObject();
//		returnObj.put("result", "Success");
//		System.out.println(doorLock.getValue());
//		System.out.println(doorLock.getDeviceid());
//		System.out.println(doorLock.getFeatureid());
//		System.out.println(doorLock.getEnvironment());
//		JSONObject payload = new JSONObject();
//		JSONObject headers = new JSONObject();
//		JSONObject value = new JSONObject();
//		payload.put("topic", doorLock.getNamespace()+"/"+CLIENTIDENTIFIER+"/things/live/messages/featureMessage");
//		headers.put("target", doorLock.getDeviceid());
//		payload.put("headers", headers);
//		payload.put("path", "");
//		value.put("featureId", doorLock.getFeatureid());
//		JSONObject temp = new JSONObject();
//		temp.put("signal", doorLock.getSignal());
//		temp.put("value", doorLock.getValue());
//		value.put("payload", temp.toString());
//		payload.put("value", value);
//		mqttService.publish(payload.toString());
//		return returnObj.toString();
//	}
	
	
}
