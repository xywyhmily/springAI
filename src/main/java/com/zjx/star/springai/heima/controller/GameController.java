package com.zjx.star.springai.heima.controller;

import com.zjx.star.springai.heima.repository.ChatHistoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/*
 *2025/4/30:14:13
 *version:1.0.0
 *@author:zjx
 */
@RequestMapping("ai")
@RestController
public class GameController {

    @Autowired
    private ChatClient gameChatClient;

    @Autowired
    private ChatHistoryRepository chatHistoryRepository;

    @RequestMapping(value = "game" , produces = "text/html;charset=utf-8")
    public Flux<String> demoStream(String prompt , String chatId) {

        chatHistoryRepository.save("chat",chatId);
        System.out.println("-----------------"+prompt);
        return gameChatClient.prompt()
                .user(prompt)
                .advisors(a->a.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY,chatId))
                .stream()
                .content();

    }
}
