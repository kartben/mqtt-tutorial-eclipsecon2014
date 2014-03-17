/*******************************************************************************
 * Copyright (c) 2014, Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Julien Vermillard - initial API and implementation
 *     Benjamin Cabé - improvements
 *******************************************************************************/
package org.eclipse.tutorials.mqtt;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class Main {

	private static final String BROKER_URI = "tcp://iot.eclipse.org:1883";

	private static Map<String, ConsolidatedValues> consolidation = new HashMap<>();

	private static long nextMinuteTimestamp;

	private static void clearConsolidation() {
		consolidation = new HashMap<>();

		// next minute
		Calendar cal = Calendar.getInstance();

		cal.add(Calendar.MINUTE, 1);
		cal.set(Calendar.SECOND, 0);

		nextMinuteTimestamp = cal.getTime().getTime();

	}

	public static void main(String[] args) {
		clearConsolidation();

		try {
			final MqttAsyncClient mqttClient = new MqttAsyncClient(BROKER_URI,
					MqttClient.generateClientId(), new MemoryPersistence());

			mqttClient.setCallback(new MqttCallback() {

				@Override
				public void messageArrived(String topic, MqttMessage message)
						throws Exception {

					try {
						// TODO retrieve sensor value and sensor name
						double sensorValue = 0;
						// sensorValue = ...
						String sensorName = null;
						// sensorName = ...

						ConsolidatedValues conso = consolidation
								.get(sensorName);
						if (conso == null) {
							conso = new ConsolidatedValues();
							consolidation.put(sensorName, conso);
						}

						conso.addSample(sensorValue);

						// end of the minute?
						if (System.currentTimeMillis() > nextMinuteTimestamp) {
							System.out.println("PUBLISH CONSOLIDATION");

							// TODO publish computed average value

							clearConsolidation();
						}
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
			mqttClient.connect(null, new IMqttActionListener() {
				@Override
				public void onSuccess(IMqttToken asyncActionToken) {

					// TODO subscribe to live data
				}

				@Override
				public void onFailure(IMqttToken asyncActionToken,
						Throwable exception) {
					exception.printStackTrace();
				}
			});
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
}
