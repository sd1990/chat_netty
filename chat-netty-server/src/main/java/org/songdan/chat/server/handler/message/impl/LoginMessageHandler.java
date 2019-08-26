package org.songdan.chat.server.handler.message.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import org.songdan.chat.common.domain.Message;
import org.songdan.chat.common.domain.MessageHeader;
import org.songdan.chat.common.domain.Response;
import org.songdan.chat.common.domain.ResponseHeader;
import org.songdan.chat.common.enumeration.ResponseCode;
import org.songdan.chat.common.enumeration.ResponseType;
import org.songdan.chat.server.handler.message.MessageHandler;
import org.songdan.chat.server.property.PromptMsgProperty;
import org.songdan.chat.server.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by SinjinSong on 2017/5/23.
 */
@Component("MessageHandler.login")
public class LoginMessageHandler extends MessageHandler {
    @Autowired
    private UserManager userManager;


    @Override
    public void handle(Message message, ChannelHandlerContext ctx) {
        SocketChannel clientChannel = (SocketChannel) ctx.channel();
        MessageHeader header = message.getHeader();
        String username = header.getSender();
        String password = new String(message.getBody(), PromptMsgProperty.charset);
        if (userManager.login(clientChannel, username, password)) {
            ctx.writeAndFlush(new Response(
                    ResponseHeader.builder()
                            .type(ResponseType.PROMPT)
                            .sender(message.getHeader().getSender())
                            .timestamp(message.getHeader().getTimestamp())
                            .responseCode(ResponseCode.LOGIN_SUCCESS.getCode()).build(),
                    String.format(PromptMsgProperty.LOGIN_SUCCESS, userManager.getOnlineUserNum()).getBytes(PromptMsgProperty.charset)));
        } else {
            ctx.writeAndFlush(new Response(
                    ResponseHeader.builder()
                            .type(ResponseType.PROMPT)
                            .responseCode(ResponseCode.LOGIN_FAILURE.getCode())
                            .sender(message.getHeader().getSender())
                            .timestamp(message.getHeader().getTimestamp()).build(),
                    PromptMsgProperty.LOGIN_FAILURE.getBytes(PromptMsgProperty.charset)));
        }
    }
}
