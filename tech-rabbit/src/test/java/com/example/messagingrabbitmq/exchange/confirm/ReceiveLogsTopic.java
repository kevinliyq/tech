package com.example.messagingrabbitmq.exchange.confirm;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.ShutdownSignalException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ReceiveLogsTopic {  
      
    // FIXME  
    // Some teasers:  
    // Will "*" binding catch a message sent with an empty routing key?  
    // Will "#.*" catch a message with a string ".." as a key? Will it catch a message with a single word key?  
    // How different is "a.*.#" from "a.#"?  
  
    private static final String EXCHANGE_NAME = "topic_confirm_logs";
      
    public static void main(String[] args) throws IOException, ShutdownSignalException, ConsumerCancelledException, TimeoutException
    {
          
        ConnectionFactory factory = new ConnectionFactory();  
        factory.setHost("localhost");  
        Connection connection = factory.newConnection();  
        Channel channel = connection.createChannel();  
          
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");  
        String queueName = channel.queueDeclare().getQueue();  
          
        String[] strs = new String[] { "*.critical" };
        for (String str : strs) {  
            channel.queueBind(queueName, EXCHANGE_NAME, str);  
        }  
          
        System.out.println("Press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }  
}  