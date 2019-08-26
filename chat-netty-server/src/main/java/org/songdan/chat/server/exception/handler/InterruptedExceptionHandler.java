package org.songdan.chat.server.exception.handler;

import org.songdan.chat.common.domain.Message;
import org.songdan.chat.common.domain.Response;
import org.songdan.chat.common.domain.ResponseHeader;
import org.songdan.chat.common.enumeration.ResponseType;
import org.songdan.chat.common.util.ProtoStuffUtil;
import org.songdan.chat.server.property.PromptMsgProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@Component("interruptedExceptionHandler")
public class InterruptedExceptionHandler {
    
    public void handle(SocketChannel channel,Message message) {
        try {
            byte[] response = ProtoStuffUtil.serialize(
                    new Response(
                            ResponseHeader.builder()
                                    .type(ResponseType.PROMPT)
                                    .sender(message.getHeader().getSender())
                                    .timestamp(message.getHeader().getTimestamp()).build(),
                            PromptMsgProperty.SERVER_ERROR.getBytes(PromptMsgProperty.charset)));
            channel.write(ByteBuffer.wrap(response));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
