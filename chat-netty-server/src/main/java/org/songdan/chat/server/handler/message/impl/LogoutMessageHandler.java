package org.songdan.chat.server.handler.message.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.songdan.chat.common.domain.Message;
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
@Component("MessageHandler.logout")
@Slf4j
public class LogoutMessageHandler extends MessageHandler {
    @Autowired
    private UserManager userManager;

    @Override
    public void handle(Message message, ChannelHandlerContext ctx) {
        SocketChannel clientChannel = (SocketChannel) ctx.channel();
        userManager.logout(clientChannel);
        Response response = new Response(ResponseHeader.builder()
                .type(ResponseType.PROMPT)
                .responseCode(ResponseCode.LOGOUT_SUCCESS.getCode())
                .sender(message.getHeader().getSender())
                .timestamp(message.getHeader().getTimestamp()).build(),
                PromptMsgProperty.LOGOUT_SUCCESS.getBytes(PromptMsgProperty.charset));
        ctx.writeAndFlush(response);
        //下线广播
        Response responseMsg = new Response(
                ResponseHeader.builder()
                        .type(ResponseType.NORMAL)
                        .sender(SYSTEM_SENDER)
                        .timestamp(message.getHeader().getTimestamp()).build(),
                String.format(PromptMsgProperty.LOGOUT_BROADCAST, message.getHeader().getSender()).getBytes(PromptMsgProperty.charset));
        broadcast(responseMsg);
        log.info("客户端退出");
        clientChannel.shutdown().addListener(future -> log.info("client close"));
    }
}
