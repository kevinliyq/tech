rabbitmq

# delay queue
在以下集中情况，消息会被转到死信交换器。我们可以借助死信队列来完成延迟队列。

1. 一个消息被Consumer拒收了，并且reject方法的参数里requeue是false。也就是说不会被再次放在队列里，被其他消费者使用。

2. 上面的消息的TTL到了，消息过期了。

3. 队列的长度限制满了。排在前面的消息会被丢弃或者扔到死信路由上。

消息中包含消息的过期时间即TTL，当消息过期后会转到私信队列
定义两个queue，其中是存储有未过期的消息如dd_order, 以及负责接受过期消息的queue如dd_order_expiration

- 定义exchange, dd_order_exchange, 负责把routing key转发到dd_order
- 配置dd_order中的DLX以及routing key用于路由到第二个queue dd_order_expiration
```java
x-dead-letter-exchange:	dd_pay_expiration_exchange
x-dead-letter-routing-key:	dd_pay_expiration_queue
```
- 配置dd_order_expiration的bind关系
Binding：
From dd_pay_expiration_exchange
routing key dd_pay_expiration_queue

根据这个配置，从dd_order发出来的消息，如过期之后则会发送到死信队列dd_order_expiration。
message consumer可以监听dd_order_expiration获取过期的消息。

# Publisher confirm
如果需要确保消息发送不丢失，有两种方式，一是开启事务的方式，该方式的性能会比较差，
第二种方式是发送者确认的方式，当broker 对于持久化消息fsync到磁盘或者路由到all queue，则会ack发送端，
发送者从而知道发送消息是否成功。

