package org.eclipse.tutorials.mqtt;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class Main {

    private static Map<String, ConsolidatedValues> consolidation = new HashMap<>();

    private static long endMinute;

    private static BlockingQueue<ToPublish> queue = new LinkedBlockingQueue<ToPublish>();

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

            final MqttClient mqttClient = new MqttClient("tcp://iot.eclipse.org:1883", MqttClient.generateClientId(),
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
                                Calendar c = Calendar.getInstance();
                                c.setTime(new Date(endMinute));

                                p.topic = String.format("/conso/%s/%d/%d/%d/%d/%d", e.getKey(), c.get(Calendar.YEAR),
                                        c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
                                        c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                                p.message = new MqttMessage(Double.toString(e.getValue().getAverage()).getBytes());
                                p.message.setRetained(true);
                                p.message.setQos(1);
                                queue.add(p);
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
            mqttClient.connect();
            mqttClient.subscribe("/fosdem/#");

            // thread used to publish message outside of the reception callback
            Thread t = new Thread() {
                public void run() {
                    for (;;) {
                        ToPublish p;
                        try {
                            p = queue.take();

                            System.out.println("publishing!");

                            mqttClient.publish(p.topic, p.message);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                    }

                }
            };

            t.start();

        } catch (MqttException e) {

            e.printStackTrace();
        }
    }
}
