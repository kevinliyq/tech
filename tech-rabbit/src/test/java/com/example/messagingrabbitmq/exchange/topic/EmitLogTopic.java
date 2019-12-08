package com.example.messagingrabbitmq.exchange.topic;

import java.io.IOException;
  
import com.rabbitmq.client.Channel;  
import com.rabbitmq.client.Connection;  
import com.rabbitmq.client.ConnectionFactory;  
  
public class EmitLogTopic {  
  
    private static final String EXCHANGE_NAME = "topic_logs";  
      
    public static void main(String[] args) throws Exception {
          
        ConnectionFactory factory = new ConnectionFactory();  
        factory.setHost("localhost");  
        Connection connection = factory.newConnection();  
        Channel channel = connection.createChannel();  
          
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");  
          
        // diff  
        String routingKey = "kern.critical";
        String message = getMessage(new String[] { "kern.critical test" });

        channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes());  
        System.out.println("s[" + routingKey + "]:[" + message + "]");  
          
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