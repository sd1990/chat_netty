package org.songdan.chat.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.songdan.chat.common.enumeration.ResponseType;

/**
 * @author: Songdan
 * @create: 2019-08-24 21:11
 **/
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ResponseHeader {

    private String sender;

    private ResponseType type;

    private Long timestamp;

    private Integer responseCode;

}
