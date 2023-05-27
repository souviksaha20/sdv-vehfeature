package com.bosch.lite.sdv.vehfeature.bean;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;

import com.bosch.lite.sdv.vehfeature.service.MqttService;

//public class TerminateBean {
	
//	private MqttService mqttService;
//    @Autowired
//    public void setMqttService(MqttService mqttService) {
//        this.mqttService = mqttService;
//        if(mqttService.isNotConnected()) {
//        	mqttService.connect();
//        }
//    }
//
//    @PreDestroy
//    public void onDestroy() throws Exception {
//    	System.out.println("1.Spring Container is destroyed!");
//        mqttService.disconnect();
//        System.out.println("2.Spring Container is destroyed!");
//    }
//}
