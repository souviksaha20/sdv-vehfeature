var featureApp = angular.module('companionApp', []).run(function($rootScope){
	
});

featureApp.controller('FeatureController', function($window, $rootScope, $timeout, $http, $interval) {  
	console.log('FeatureController');
	var jsonContentType = {
    	'Content-Type': 'application/json; charset=utf-8',
    };
	var vm = this;	
	var requestHeaders = {
		'Content-Type': 'application/json; charset=utf-8'
    };
	
	vm.context = {
		'deviceid' : 'NEVONEX_FEK_TEST_30',
		'featureid' : 'NVX_FE_04102021144126',
		'environment' : 'dev',
		'vehicle' : {
			'doorlock' : false,
			'wiper' : false,
			'ac' : false,
			'acValue' : 24
		},
		'incoming' : {
			'messages' : []
		}
	};
	
	vm.populateIncomingMessages = function(message){
		var msg = JSON.parse(message);
		console.log(msg);
		$timeout(function(){
			vm.context.incoming.messages.unshift({
				'message' : msg.value.payload,
				'feature' : msg.value.featureId,
				'target'  : msg.headers.target,
				'source'  : msg.topic.replace('/things/live/messages/featureMessage','')
			});
		},100);
	}
	
	vm.doorLock = function(){
		if(vm.context.deviceid && vm.context.deviceid && vm.context.deviceid){
			console.log('vm.context.vehicle.doorlock '+vm.context.vehicle.doorlock);
			$http({
				method : 'POST',
				headers: jsonContentType,
				url : '/doorlock',
				data : {
					'deviceid' : vm.context.deviceid,
					'featureid' : vm.context.featureid,
					'environment' : vm.context.environment,
					'value' : vm.context.vehicle.doorlock ? 'LOCK' : 'UNLOCK'
				}
			}).then(function success(response) {
				console.log("Success");
				console.log(response);
			}, function error(response) {
				console.log("Error");
				console.log(response);
			});
		}else{
			console.log("Set device settings");
		}
		
	}
	vm.wiper = function(){
		if(vm.context.deviceid && vm.context.deviceid && vm.context.deviceid){
			console.log('vm.context.vehicle.wiper '+vm.context.vehicle.wiper);
			$http({
				method : 'POST',
				headers: jsonContentType,
				url : '/wiper',
				data : {
					'deviceid' : vm.context.deviceid,
					'featureid' : vm.context.featureid,
					'environment' : vm.context.environment,
					'value' : vm.context.vehicle.wiper ? 'ON' : 'OFF'
				}
			}).then(function success(response) {
				console.log("Success");
				console.log(response);
			}, function error(response) {
				console.log("Error");
				console.log(response);
			});
		}else{
			console.log("Set device settings");
		}
	}
	
	vm.ac = function(){
		if(vm.context.deviceid && vm.context.deviceid && vm.context.deviceid){
			console.log('vm.context.vehicle.ac '+vm.context.vehicle.ac);
			$http({
				method : 'POST',
				headers: jsonContentType,
				url : '/ac',
				data : {
					'deviceid' : vm.context.deviceid,
					'featureid' : vm.context.featureid,
					'environment' : vm.context.environment,
					'value' : vm.context.vehicle.ac ? 'ON' : 'OFF',
					'signalValue' : vm.context.vehicle.acValue
				}
			}).then(function success(response) {
				console.log("Success");
				console.log(response);
			}, function error(response) {
				console.log("Error");
				console.log(response);
			});
		}else{
			console.log("Set device settings");
		}
	}
	
	function initSocket(host,port){
		function connect() {
			var prefix = "ws://"+host+":"+port+"/";
			ws = new WebSocket(prefix + "featureSocket");
			ws.onmessage = function(data){
				console.log("Got websocket message");
				console.log(data.data);
				vm.populateIncomingMessages(data.data);
			}
			ws.onopen = function() {
				console.log('Socket opened');
			};
		}
		
		function disconnect() {
		    if (ws != null) {
		        ws.close();
		    }
		    console.log("Disconnected");
		}
		
		function send(message) {
		    ws.send(message);
		}
		connect();
	}
	initSocket(window.location.hostname, 8080);
	
});