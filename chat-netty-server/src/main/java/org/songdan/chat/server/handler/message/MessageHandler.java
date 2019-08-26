package org.songdan.chat.server.handler.message;

import io.netty.channel.ChannelHandlerContext;
import org.songdan.chat.common.domain.Message;
import org.songdan.chat.common.domain.Response;
import org.songdan.chat.common.domain.User;
import org.songdan.chat.server.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by SinjinSong on 2017/5/23.
 */
public abstract class MessageHandler {

    public static final String SYSTEM_SENDER = "系统提示";

    abstract public void handle(Message message, ChannelHandlerContext ctx);

    @Autowired
    private UserManager userManager;

    protected void broadcast(Response response) {
        List<User> userList = userManager.getAllOnlineUser();
        for (User onlineUser : userList) {
            onlineUser.getChannel().writeAndFlush(response);
        }
    }
}
