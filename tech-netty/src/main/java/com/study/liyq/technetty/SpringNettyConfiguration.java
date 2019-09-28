package com.study.liyq.technetty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

/**
 * Start Netty server
 * 1. All Channel Handler will be executed when channel is registered.
 * in case of they are not shared, then it will re-initialized.
 */
@Configuration
public class SpringNettyConfiguration implements ApplicationListener<ApplicationStartedEvent> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${server.port}")
    private int port;

    @Value("${http.max.content.length}")
    private int maxContentLength;

    @Autowired
    @Qualifier("interceptorHandler")
    private ChannelHandler interceptorHandler;

    @Autowired
    @Qualifier("dispatcherServlet")
    private ChannelHandler helloChannelHandler;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
        startNettyServer();
    }

    private void startNettyServer() {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.childOption(NioChannelOption.TCP_NODELAY, true);
        bootstrap.childOption(NioChannelOption.SO_REUSEADDR, true);
        bootstrap.childOption(NioChannelOption.SO_KEEPALIVE, false);
        bootstrap.childOption(NioChannelOption.SO_RCVBUF, 1024 * 1024);
        bootstrap.childOption(NioChannelOption.SO_SNDBUF, 1024 * 1024);

        bootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        //decode request header
                        socketChannel.pipeline().addLast(new HttpServerCodec());
                        //decode request body
                        socketChannel.pipeline().addLast(new HttpObjectAggregator(maxContentLength));
                        //print log
                        socketChannel.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                        socketChannel.pipeline().addLast(interceptorHandler);
                        //handle http request
                        socketChannel.pipeline().addLast("dispatcherServlet", helloChannelHandler);
                    }
                });
        ChannelFuture channelFuture = bootstrap.bind(port).syncUninterruptibly().addListener(future -> {
            logger.info("Netty Server is started on {}", port);
        });

        channelFuture.channel().closeFuture().addListener(future -> {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
            System.out.println("Netty Server is closed");
        });
    }
}
