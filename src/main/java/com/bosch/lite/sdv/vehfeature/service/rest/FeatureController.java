package com.bosch.lite.sdv.vehfeature.service.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	Logger logger = LoggerFactory.getLogger(FeatureController.class);
	@Autowired
	private MqttService mqttService;
	
	@PostMapping("/send/featureMessage")
	public ResponseEntity<String> sendFeatureMessage(@RequestBody Payload payload) {
		try {
			 String data=new Gson().toJson(payload);
			if(mqttService.publishToAws(data.toString()))
				logger.info("data Published Success");
			
			return ResponseEntity.status(HttpStatus.OK).body("message send");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("message failed");
		}
	}
	
	
}
