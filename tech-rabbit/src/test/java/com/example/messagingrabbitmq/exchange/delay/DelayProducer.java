package com.example.messagingrabbitmq.exchange.delay;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class DelayProducer
{

  private static final String EXCHANGE_NAME = "dd_order_exchange";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    try (Connection connection = factory.newConnection();
         Channel channel = connection.createChannel()) {
        //as define exchange by rabbit management, thus no needs to declare here
        //channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        String severity = getSeverity(argv);
        String message = getMessage(argv);

        byte[] messageBodyBytes = "Hello, world!".getBytes();

        byte i = 10;
        while (i-- > 0) {
            channel.basicPublish(EXCHANGE_NAME, "dd_order", new AMQP.BasicProperties.Builder().expiration(String.valueOf(i * 5000)).build(),
                                 ("Hello, world " +i ).getBytes());
            System.out.println(" [x] Sent '" + severity + "':'" + new String(messageBodyBytes) + "'");

        }

    }
  }
    private static String getSeverity(String[] strings) {
        if (strings.length < 1)
            return "info";
        return strings[0];
    }

    private static String getMessage(String[] strings) {
        if (strings.length < 2)
            return "Hello World!";
        return joinStrings(strings, " ", 1);
    }

    private static String joinStrings(String[] strings, String delimiter, int startIndex) {
        int length = strings.length;
        if (length == 0) return "";
        if (length <= startIndex) return "";
        StringBuilder words = new StringBuilder(strings[startIndex]);
        for (int i = startIndex + 1; i < length; i++) {
            words.append(delimiter).append(strings[i]);
        }
        return words.toString();
    }
}