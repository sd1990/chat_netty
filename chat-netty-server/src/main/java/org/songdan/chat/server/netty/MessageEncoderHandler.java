package org.songdan.chat.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import org.songdan.chat.common.domain.Message;
import org.songdan.chat.common.domain.Response;
import org.songdan.chat.common.util.ProtoStuffUtil;

import java.util.List;

/**
 * @author: Songdan
 * @create: 2019-08-25 15:18
 **/
public class MessageEncoderHandler extends MessageToByteEncoder<Response> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Response response, ByteBuf out) throws Exception {
        out.writeBytes(ProtoStuffUtil.serialize(response));
    }
}
