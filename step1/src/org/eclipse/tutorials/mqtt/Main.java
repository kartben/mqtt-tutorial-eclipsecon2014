package org.eclipse.tutorials.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class Main {

	public static void main(String[] args) {
		try {
			MqttClient mqttClient = new MqttClient(
					"tcp://iot.eclipse.org:1883",
					MqttClient.generateClientId(), new MemoryPersistence());
			mqttClient.setCallback(new MqttCallback() {

				@Override
				public void messageArrived(String topic, MqttMessage message)
						throws Exception {
					System.out.println(topic + ": "
							+ new String(message.getPayload()));
				}

				@Override
				public void deliveryComplete(IMqttDeliveryToken token) {
					// not used
				}

				@Override
				public void connectionLost(Throwable cause) {
					System.out.println("Connection lost: "
							+ cause.getLocalizedMessage());
				}
			});
			mqttClient.connect();
			mqttClient.subscribe("/fosdem/#");
			// mqttClient.publish("/fosdem/demo/command/toggleRoof",
			// new MqttMessage());
		} catch (MqttSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
