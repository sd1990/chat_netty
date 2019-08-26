package org.songdan.chat.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Songdan
 * @create: 2019-08-24 21:11
 **/
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Response {

    private ResponseHeader header;

    private byte[] body;

}
