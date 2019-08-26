package org.songdan.chat.client;

import org.songdan.chat.common.domain.Message;
import org.songdan.chat.common.domain.MessageHeader;
import org.songdan.chat.common.domain.Response;
import org.songdan.chat.common.domain.ResponseHeader;
import org.songdan.chat.common.enumeration.MessageType;
import org.songdan.chat.common.enumeration.ResponseCode;
import org.songdan.chat.common.util.DateTimeUtil;
import org.songdan.chat.common.util.ProtoStuffUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * 2019-08-24 只做一个登录功能，实现通信
 */
public class ChatClient extends Frame {

    public static final int DEFAULT_BUFFER_SIZE = 1024;
    private Selector selector;
    private SocketChannel clientChannel;
    private ByteBuffer buf;
    /**
     * 输入框
     */
    private TextField tfText;
    /**
     * 展示区域
     */
    private TextArea taContent;
    private ReceiverHandler listener;
    private String username;
    private boolean isLogin = false;
    private boolean isConnected = false;
    private Charset charset = StandardCharsets.UTF_8;

    public ChatClient(String name, int x, int y, int w, int h) {
        super(name);
        initFrame(x, y, w, h);
        initNetWork();
        login();
    }

    /**
     * 初始化窗体
     *
     * @param x
     * @param y
     * @param w
     * @param h
     */
    private void initFrame(int x, int y, int w, int h) {
        this.tfText = new TextField();
        this.taContent = new TextArea();
        this.setBounds(x, y, w, h);
        this.setLayout(new BorderLayout());
        /*
         * 添加窗口关闭的监听事件
         */
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
                //断开连接
                disConnect();
                System.exit(0);
            }
        });
        this.taContent.setEditable(false);
        this.add(tfText, BorderLayout.SOUTH);
        this.add(taContent, BorderLayout.NORTH);
        /*
         * 添加回车监听时间
         */
        this.tfText.addActionListener((actionEvent) -> {
            String str = tfText.getText().trim();
            tfText.setText("");
            //发送消息
            send(str);
        });
        this.pack();
        this.setVisible(true);
    }

    /**
     * 初始化网络模块
     */
    private void initNetWork() {
        try {
            selector = Selector.open();
            clientChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9000));
            //设置客户端为非阻塞模式
            clientChannel.configureBlocking(false);
            //注册READ事件
            clientChannel.register(selector, SelectionKey.OP_READ);
            buf = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
            isConnected = true;
        } catch (ConnectException e) {
            JOptionPane.showMessageDialog(this, "连接服务器失败");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void launch() {
        this.listener = new ReceiverHandler();
        new Thread(listener).start();
    }

    private void login() {
        String username = JOptionPane.showInputDialog("请输入用户名");
        String password = JOptionPane.showInputDialog("请输入密码");
        Message message = new Message(
                MessageHeader.builder()
                        .type(MessageType.LOGIN)
                        .sender(username)
                        .timestamp(System.currentTimeMillis())
                        .build(), password.getBytes(charset));
        try {
            clientChannel.write(ByteBuffer.wrap(ProtoStuffUtil.serialize(message)));
            System.out.println("after start login");
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.username = username;
    }

    private void disConnect() {
        try {
            logout();
            if (!isConnected) {
                return;
            }
            listener.shutdown();
            //如果发送消息后马上断开连接，那么消息可能无法送达
            Thread.sleep(10);
            clientChannel.socket().close();
            clientChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void logout() {
        if (!isLogin) {
            return;
        }
        System.out.println("客户端发送下线请求");
        Message message = new Message(
                MessageHeader.builder()
                        .type(MessageType.LOGOUT)
                        .sender(username)
                        .timestamp(System.currentTimeMillis())
                        .build(), null);
        try {
            clientChannel.write(ByteBuffer.wrap(ProtoStuffUtil.serialize(message)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送信息，监听在回车键上
     *
     * @param content
     */
    public void send(String content) {
        if (!isLogin) {
            JOptionPane.showMessageDialog(null, "尚未登录");
            return;
        }
        try {
            Message message;
            //普通模式
            if (content.startsWith("@")) {
                String[] slices = content.split(":");
                String receiver = slices[0].substring(1);
                message = new Message(
                        MessageHeader.builder()
                                .type(MessageType.NORMAL)
                                .sender(username)
                                .receiver(receiver)
                                .timestamp(System.currentTimeMillis())
                                .build(), slices[1].getBytes(charset));
            } else {
                //广播模式
                message = new Message(
                        MessageHeader.builder()
                                .type(MessageType.BROADCAST)
                                .sender(username)
                                .timestamp(System.currentTimeMillis())
                                .build(), content.getBytes(charset));
            }
            System.out.println(message);
            clientChannel.write(ByteBuffer.wrap(ProtoStuffUtil.serialize(message)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 用于接收信息的线程
     */
    private class ReceiverHandler implements Runnable {
        private boolean connected = true;

        public void shutdown() {
            connected = false;
        }


        @Override
        public void run() {
            try {
                while (connected) {
                    int size = 0;
                    selector.select();
                    for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext(); ) {
                        SelectionKey selectionKey = it.next();
                        it.remove();
                        if (selectionKey.isReadable()) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            while ((size = clientChannel.read(buf)) > 0) {
                                buf.flip();
                                baos.write(buf.array(), 0, size);
                                buf.clear();
                            }
                            byte[] bytes = baos.toByteArray();
                            baos.close();
                            Response response = ProtoStuffUtil.deserialize(bytes, Response.class);
                            handleResponse(response);
                        }
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "服务器关闭，请重新尝试连接");
                isLogin = false;
            }
        }

        private void handleResponse(Response response) {
            System.out.println(response);
            ResponseHeader header = response.getHeader();
            switch (header.getType()) {
                case PROMPT:
                    if (header.getResponseCode() != null) {
                        ResponseCode code = ResponseCode.fromCode(header.getResponseCode());
                        if (code == ResponseCode.LOGIN_SUCCESS) {
                            isLogin = true;
                            System.out.println("登录成功");
                        } else if (code == ResponseCode.LOGOUT_SUCCESS) {
                            System.out.println("下线成功");
                            break;
                        }
                    }
                    String info = new String(response.getBody(), charset);
                    JOptionPane.showMessageDialog(ChatClient.this, info);
                    break;
                case NORMAL:
                    String content = formatMessage(taContent.getText(), response);
                    taContent.setText(content);
                    taContent.setCaretPosition(content.length());
                    break;
                default:
                    break;
            }
        }

        private String formatMessage(String originalText, Response response) {
            ResponseHeader header = response.getHeader();
            StringBuilder sb = new StringBuilder();
            sb.append(originalText)
                    .append(header.getSender())
                    .append(": ")
                    .append(new String(response.getBody(), charset))
                    .append("    ")
                    .append(DateTimeUtil.formatLocalDateTime(header.getTimestamp()))
                    .append("\n");
            return sb.toString();
        }
    }


    private static class PictureDialog extends JDialog {
        public PictureDialog(Frame owner, String title, boolean modal,
                             String path) {
            super(owner, title, modal);
            ImageIcon icon = new ImageIcon(path);
            JLabel lbPhoto = new JLabel(icon);
            this.add(lbPhoto);
            this.setSize(icon.getIconWidth(), icon.getIconHeight());
            this.setVisible(true);
        }
    }

    public static void main(String[] args) {
        System.out.println("Initialing...");
        ChatClient client = new ChatClient("Client", 200, 200, 300, 200);
        client.launch();
    }
}

