package org.songdan.chat.common.domain;

import io.netty.channel.socket.SocketChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Created by SinjinSong on 2017/5/23.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private String username;
    private String password;
    private SocketChannel channel;
}
