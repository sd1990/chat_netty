package org.songdan.chat.server.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.songdan.chat.common.domain.Message;
import org.songdan.chat.server.handler.message.MessageHandler;
import org.songdan.chat.server.util.SpringContextUtil;

/**
 * @author: Songdan
 * @create: 2019-08-25 15:27
 **/
public class DefaultMessageHandler extends SimpleChannelInboundHandler<Message> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        MessageHandler messageHandler = SpringContextUtil.getBean("MessageHandler", msg.getHeader().getType().toString().toLowerCase());
        messageHandler.handle(msg, ctx);
    }
}
