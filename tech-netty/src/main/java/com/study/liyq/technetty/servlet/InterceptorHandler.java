package com.study.liyq.technetty.servlet;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("interceptorHandler")
@ChannelHandler.Sharable
public class InterceptorHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest)
        {
            HttpRequest request = (HttpRequest)msg;

            if (HttpMethod.GET.equals(request.method()))
            {
                ctx.fireChannelRead(msg);
                return;
            }

            if (HttpMethod.POST.equals(request.method()))
            {
                ctx.fireChannelRead(msg);
                return;
            }
        }

        ReferenceCountUtil.release(msg);
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
