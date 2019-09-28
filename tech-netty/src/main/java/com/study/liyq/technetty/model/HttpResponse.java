package com.study.liyq.technetty.model;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.*;

import java.util.Objects;

import static io.netty.handler.codec.http.HttpHeaderNames.*;

public class HttpResponse extends DefaultFullHttpResponse {
    //this uses head bytebuf
    private static final PooledByteBufAllocator POOLED_BYTE_BUF_ALLOCATOR = new PooledByteBufAllocator(false);
    private String content;

    private HttpResponse(HttpResponseStatus status, String content, ByteBuf buffer) {
        super(HttpVersion.HTTP_1_1, status, buffer);
        this.content = content;

        headers().set(CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        headers().setInt(CONTENT_LENGTH, content().readableBytes());
        headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        headers().set(ACCESS_CONTROL_ALLOW_HEADERS, "Origin, X-Requested-With, Content-Type, Accept, RCS-ACCESS-TOKEN");
        headers().set(ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,DELETE");
    }

    public static FullHttpResponse fail(HttpResponseStatus failStatus, String content) {
        return build(failStatus,content);
    }

    public static FullHttpResponse ok(String content) {
        return build(HttpResponseStatus.OK,content);
    }

    private static FullHttpResponse build(HttpResponseStatus status, String content) {
        Objects.requireNonNull(content, "content is null");
        byte[] b = content.getBytes();
        ByteBuf byteBuf = POOLED_BYTE_BUF_ALLOCATOR.buffer(b.length);
        byteBuf.writeBytes(b);

        FullHttpResponse fullHttpResponse = new HttpResponse(status, content, byteBuf);
        return fullHttpResponse;
    }
}
