package com.zjx.star.springai.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;

/*
 *2025/4/29:21:35
 *version:1.0.0
 *@author:zjx
 */
@NoArgsConstructor
@Data
public class MessageVo {

    public String role ;
    public String content ;

    public MessageVo(Message message) {

        switch (message.getMessageType()) {
            case USER :
                role = "user";
                break;
            case ASSISTANT:
                role = "assistant";
                break;
            default:
                role = "unknown";
                break;
        }
        content = message.getText() ;

    }
}
