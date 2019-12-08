package com.example.messagingrabbitmq.exchange.confirm;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

public class EmitLogTopic {  
  
    private static final String EXCHANGE_NAME = "topic_confirm_logs";
    private static volatile SortedSet<Long> unconfirmedSet = Collections.synchronizedSortedSet(new TreeSet<>());
      
    public static void main(String[] args) throws Exception {
          
        ConnectionFactory factory = new ConnectionFactory();  
        factory.setHost("localhost");  
        Connection connection = factory.newConnection();  
        Channel channel = connection.createChannel();  
          
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");

        channel.addConfirmListener(new ConfirmListener()
        {
            @Override public void handleAck(long deliveryTag, boolean multiple) throws IOException
            {
                System.out.println("Received broker confirm message " + deliveryTag + " multiple " + multiple);

                if(multiple)
                {
                    unconfirmedSet.headSet(deliveryTag+1).clear();
                }
                else{
                    unconfirmedSet.remove(deliveryTag);
                }
            }

            @Override public void handleNack(long deliveryTag, boolean multiple) throws IOException
            {
                System.out.println("Received broker handleNack message");
            }
        });
          
        // diff  
        String routingKey = "kern.critical";
        String message = getMessage(new String[] { "kern.critical test" });

        channel.confirmSelect();
        for (int i=0; i< 10; i++)
        {
            unconfirmedSet.add(channel.getNextPublishSeqNo());
            String msg = message + i;
            channel.basicPublish(EXCHANGE_NAME, routingKey, null, msg.getBytes());
            System.out.println("s[" + routingKey + "]:[" + msg + "]");
        }

        while (!unconfirmedSet.isEmpty())
        {
            Thread.sleep(10);
        }
        channel.close();  
        connection.close();  
          
    }  
  
    private static String getServerity(String[] strings) {  
        return "kern.critical";  
    }  
      
    private static String getMessage(String[] strings) {  
        if (strings.length < 1) {  
            return "Hello World!";  
        }  
        return joinStrings(strings, " ");  
    }  
  
    private static String joinStrings(String[] strings, String delimiter) {  
        int length = strings.length;  
        if (length == 0) {  
            return "";  
        }  
        StringBuilder words = new StringBuilder(strings[0]);  
        for (int i = 1; i < length; i++) {  
            words.append(delimiter).append(strings[i]);  
        }  
        return words.toString();  
    }  
}  