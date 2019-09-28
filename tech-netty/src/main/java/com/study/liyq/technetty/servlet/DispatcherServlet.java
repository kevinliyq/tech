package com.study.liyq.technetty.servlet;

import com.study.liyq.technetty.annotation.HttpHandler;
import com.study.liyq.technetty.controller.HelloController;
import com.study.liyq.technetty.controller.IController;
import com.study.liyq.technetty.model.HttpRequest;
import com.study.liyq.technetty.model.HttpResponse;
import com.study.liyq.technetty.model.Response;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Receive FullHttpRequest and respond same content to client
 * Has to set Content-Type
 *
 * Steps as following
 * 1. get content from request
 * 2. write it into response
 * 3. flush channelContext
 * 4. close the channel when write is completed
 *
 * implement ApplicationContextAware is to scan the annotation to get a list of controller with annotation @HttpHandler
 */

@Service
@Qualifier("dispatcherServlet")
@ChannelHandler.Sharable
public class DispatcherServlet extends SimpleChannelInboundHandler<FullHttpRequest> implements ApplicationContextAware {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final PooledByteBufAllocator BYTE_BUF_ALLOCATOR = new PooledByteBufAllocator(false);
    private Executor executor = Executors.newCachedThreadPool();

    private Map<String,IController> pathToControllerMap = new HashMap<>();

    public DispatcherServlet(){

    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        logger.info("Received {}, handler {}", fullHttpRequest, this);

        //as after read0, the channel is closed, and the fullHttPRequest can't not be read, thus needs to copy it.
        FullHttpRequest copyFullHttpRequest = fullHttpRequest.copy();
        executor.execute(() -> handleRequest(channelHandlerContext, copyFullHttpRequest));

        logger.info("channelRead0 end");
    }

    private void handleRequest(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) {
        String content = fullHttpRequest.content().toString(CharsetUtil.UTF_8);
        logger.info("content {}", content);

        IController controller = getController(fullHttpRequest.uri());
        FullHttpResponse fullHttpResponse;
        if (controller == null)
        {
            fullHttpResponse = HttpResponse.fail(HttpResponseStatus.BAD_REQUEST, "Path is not found");
        }else {
            try {
                Response response = controller.handleRequest(new HttpRequest(fullHttpRequest));
                fullHttpResponse = buildHttpResponse(response);
            } catch (Exception ex) {
                logger.error("handleRequest error", ex);
                fullHttpResponse = HttpResponse.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
            }
        }
        writeResponse(channelHandlerContext, content, fullHttpResponse);
    }

    private void writeResponse(ChannelHandlerContext channelHandlerContext, String content, FullHttpResponse fullHttpResponse) {
        ByteBuf buffer = BYTE_BUF_ALLOCATOR.buffer(content.length());
        buffer.writeBytes(content.getBytes());
        channelHandlerContext.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);

        logger.info("response {}", fullHttpResponse);
    }

    private FullHttpResponse buildHttpResponse(Response response) {
        Objects.requireNonNull(response, "response is null");
        return HttpResponse.ok(response.toJSONString());
    }

    private IController getController(String uri) {
        return pathToControllerMap.get(uri);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> maps = applicationContext.getBeansWithAnnotation(HttpHandler.class);

        maps.entrySet().stream().forEach((entry) -> {
            Object object = entry.getValue();
            HttpHandler httpHandler = object.getClass().getAnnotation(HttpHandler.class);
            if (!(object instanceof IController))
            {
                throw new RuntimeException("Annotated with @HttpHandler does not implement IController");
            }
            String path = httpHandler.path();
            Objects.requireNonNull(path, path + " is null");

            pathToControllerMap.put(path, (IController) object);
        });
    }
}
