package org.songdan.chat.server.handler.message.impl;

import io.netty.channel.ChannelHandlerContext;
import org.songdan.chat.common.domain.Message;
import org.songdan.chat.common.domain.Response;
import org.songdan.chat.common.domain.ResponseHeader;
import org.songdan.chat.common.domain.User;
import org.songdan.chat.common.enumeration.ResponseType;
import org.songdan.chat.common.util.ProtoStuffUtil;
import org.songdan.chat.server.handler.message.MessageHandler;
import org.songdan.chat.server.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by SinjinSong on 2017/5/23.
 */
@Component("MessageHandler.broadcast")
public class BroadcastMessageHandler extends MessageHandler {

    @Autowired
    private UserManager userManager;

    @Override
    public void handle(Message message, ChannelHandlerContext ctx) {
        Response response = new Response(
                ResponseHeader.builder()
                        .type(ResponseType.NORMAL)
                        .sender(message.getHeader().getSender())
                        .timestamp(message.getHeader().getTimestamp()).build(),
                message.getBody());
        broadcast(response);
    }
}
