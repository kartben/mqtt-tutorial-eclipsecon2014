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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

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

    private static long endMinute;

    private static class ToPublish {

        String topic;

        MqttMessage message;
    }

    private static void clearConsolidation() {
        consolidation = new HashMap<>();

        // next minute
        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.MINUTE, 1);
        cal.set(Calendar.SECOND, 0);

        endMinute = cal.getTime().getTime();

    }

    public static void main(String[] args) {
        clearConsolidation();

        try {
            final MqttAsyncClient mqttClient = new MqttAsyncClient(BROKER_URI, MqttClient.generateClientId(),
                    new MemoryPersistence());

            mqttClient.setCallback(new MqttCallback() {

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                    try {
                        double data = Double.parseDouble(new String(message.getPayload()));

                        System.out.println(topic + " => " + data);

                        String dataName = topic.substring(topic.lastIndexOf('/') + 1);

                        ConsolidatedValues conso = consolidation.get(dataName);
                        if (conso == null) {
                            conso = new ConsolidatedValues();
                            consolidation.put(dataName, conso);
                        }

                        conso.addSample(data);

                        System.out.println("average " + dataName + ": " + conso.getAverage() + " ("
                                + conso.getSampleCount() + " samples)");
                        if (System.currentTimeMillis() > endMinute) {
                            System.out.println("PUBLISH CONSOLIDATION");

                            // publish averages and
                            for (Map.Entry<String, ConsolidatedValues> e : consolidation.entrySet()) {
                                ToPublish p = new ToPublish();
                                Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                                c.setTime(new Date(endMinute));

                                p.topic = String.format(
                                        "greenhouse/CONSOLIDATED/benjamin-bbb/data/%s/%d/%02d/%d/%d/%d", e.getKey(),
                                        c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH),
                                        c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                                p.message = new MqttMessage(Double.toString(e.getValue().getAverage()).getBytes());
                                p.message.setRetained(true);
                                p.message.setQos(1);

                                mqttClient.publish(p.topic, p.message);

                            }

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
                    System.out.println("Connection lost: " + cause.getLocalizedMessage());
                }
            });
            mqttClient.connect(null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    try {
                        mqttClient.subscribe("greenhouse/LIVE/benjamin-bbb/data/#", 1);
                    } catch (MqttException e) {
                        System.out.println("Connection failed: ");
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    exception.printStackTrace();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
