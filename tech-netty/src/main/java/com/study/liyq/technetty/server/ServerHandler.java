package com.study.liyq.technetty.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        System.out.println("The server receive msg :" + buf.toString());
        ctx.write(buf);
    }

    //when read is completed successfully
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    //any exception when read
    @Override
    public void exceptionCaught (ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}
