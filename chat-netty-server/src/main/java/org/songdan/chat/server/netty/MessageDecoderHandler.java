package org.songdan.chat.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.songdan.chat.common.domain.Message;
import org.songdan.chat.common.util.ProtoStuffUtil;

import java.util.List;

/**
 * @author: Songdan
 * @create: 2019-08-25 15:18
 **/
public class MessageDecoderHandler extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        byte[] intactBytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(intactBytes);
        Message message = ProtoStuffUtil.deserialize(intactBytes, Message.class);
        out.add(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
