package org.songdan.chat.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.songdan.chat.server.netty.DefaultMessageHandler;
import org.songdan.chat.server.netty.MessageDecoderHandler;
import org.songdan.chat.server.netty.MessageEncoderHandler;

import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * 先做简单的登录功能
 * Slf4j可以在打印的字符串中添加占位符，以避免字符串的拼接
 */
@Slf4j
public class ChatServer {
    public static final int DEFAULT_BUFFER_SIZE = 1024;
    public static final int PORT = 9000;
    public static final String QUIT = "QUIT";

    private NioEventLoopGroup group;

    public ChatServer() {
        log.info("服务器启动");
        initServer();
    }

    private void initServer() {
        group = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(PORT))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new MessageDecoderHandler())
                                    .addLast(new MessageEncoderHandler())
                                    .addLast(new DefaultMessageHandler());
                            //添加入站出站事件handler
                        }
                    });
            serverBootstrap.bind().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 关闭服务器
     */
    public void shutdownServer() {
        try {
            group.shutdownGracefully().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        System.out.println("Initialing...");
        ChatServer chatServer = new ChatServer();
        Scanner scanner = new Scanner(System.in, "UTF-8");
        while (scanner.hasNext()) {
            String next = scanner.next();
            if (next.equalsIgnoreCase(QUIT)) {
                System.out.println("服务器准备关闭");
                chatServer.shutdownServer();
                System.out.println("服务器已关闭");
            }
        }
    }
}
