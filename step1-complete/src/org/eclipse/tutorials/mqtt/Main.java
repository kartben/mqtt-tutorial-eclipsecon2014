/*******************************************************************************
 * Copyright (c) 2014, Eclipse Foundation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Benjamin Cabé - initial API and implementation
 *     Julien Vermillard - improvements
 *******************************************************************************/
package org.eclipse.tutorials.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class Main {

	private static final String BROKER_URI = "tcp://iot.eclipse.org:1883";

	public static void main(String[] args) {

		try {

			final MqttClient mqttClient = new MqttClient(
					BROKER_URI,
					MqttClient.generateClientId(), new MemoryPersistence());

			mqttClient.setCallback(new MqttCallback() {

				@Override
				public void messageArrived(String topic, MqttMessage message)
						throws Exception {

					try {
						System.out.println(topic + " => "
								+ new String(message.getPayload()));

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
					System.out.println("Connection lost: "
							+ cause.getLocalizedMessage());
				}
			});
			mqttClient.connect();
			mqttClient.subscribe("greenhouse/LIVE/benjamin-bbb/data/#");

		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
}
