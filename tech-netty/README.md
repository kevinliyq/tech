# Netty

Netty in action 【https://waylau.gitbooks.io/essential-netty-in-action/content/GETTING%20STARTED/Asynchronous%20and%20Event%20Driven.html】

1. Netty
* BootStrap
  Netty 应用程序引导类，通过BootStrap，我们可以绑定端口或者是连接服务器，从而启动程序
* Channel
  Channel代表一个到硬件设备，文件和一个网络套接字或者是可以执行一个或多个I/O操作的连接，这些I/O操作包括读写

* ChannelHandler
ChannelHandler处理I/O实现或者拦截I/O操作，然后转交给下一个ChannelHandler处理，形成ChannelHandler链

* ChannelPipeline
包含一系列用户处理I/O事件的ChannelHandler的列表，当Channel创建时自动会创建出一个ChannelPipeline。
对于InboundEvent, ChannelPipeline使用自底向上的处理流程，从Socket.read(ByteBuffer)到每一个实现InboundChannelHandler的实例
对于OutboundEvent，ChannelPipeline采用从顶向下的处理流程, 从第一个OutboundChannelHandler到Socket.write(ByteBuffer)
每个ChannelHander通过fireEvent来触发下一个ChannelHandler执行

* EventLoop
处理Channel注册后的所有的I/O操作，一个EventLoop可以处理多个Channel上的I/O操作。

* EventLoopGroup
用户注册Channel，已经调用对应的Event用于处理注册后的所有I/O操作

2. 问题
* Channel和Socket的关系
  1. Socket是基于阻塞的连接，服务器需要为每个连接创建一个线程去出去，而在其上的I/O操作是阻塞的，即数据准备好才可以读，buffer不为空才可以写。
  所以性能上比较查。
  Channel可以支持阻塞和非阻塞的方式, 这样Server可以采用单个形成去轮训可用的I/O请求，当有请求来时select会被唤醒，这使得我们可以用单个形成去支持多个Channel的连接即多路I/O复用。
  
  2. Socket 是基于流的，而Channel是基于buffer缓冲区，以block进行读写，效率比较高，如果采用DMA，可以做到zero-copy，即数据在套接字和内核态中操作，而无需copy到用户态。
* 

                                                


