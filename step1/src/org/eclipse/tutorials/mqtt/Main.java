package org.eclipse.tutorials.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class Main {

    public static void main(String[] args) {

        try {

            final MqttClient mqttClient = new MqttClient("tcp://iot.eclipse.org:1883", MqttClient.generateClientId(),
                    new MemoryPersistence());

            mqttClient.setCallback(new MqttCallback() {

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                    try {
                        System.out.println(topic + " => " + new String(message.getPayload()));

                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // not used
                }

                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("Connection lost: " + cause.getLocalizedMessage());
                }
            });
            mqttClient.connect();
            mqttClient.subscribe("/fosdem/#");

        } catch (MqttException e) {

            e.printStackTrace();
        }
    }
}
