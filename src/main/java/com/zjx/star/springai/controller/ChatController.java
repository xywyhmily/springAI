package com.zjx.star.springai.controller;

/*
 *2025/4/29:15:14
 *version:1.0.0
 *@author:zjx
 */

import com.zjx.star.springai.repository.ChatHistoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("ai")
public class ChatController {

    @Autowired
    private ChatClient chatModel;

    @Autowired
    private ChatHistoryRepository chatHistoryRepository;

    @Autowired
    private ChatMemory chatMemory;

//    @GetMapping(value = "{message}" , produces = "text/html;charset=utf-8")
//    public String demo(@PathVariable("message") String message) {
//
////        System.out.println(message);
//        return chatModel.prompt()
//                .user(message)
//                .call()
//                .content();
//
//    }

    @RequestMapping(value = "chat" , produces = "text/html;charset=utf-8")
    public Flux<String> demoStream(String prompt , String chatId) {

        chatHistoryRepository.save("chat",chatId);
        System.out.println("-----------------"+prompt);
        return chatModel.prompt()
                .user(prompt)
                .advisors(a->a.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY,chatId))
                .stream()
                .content();

    }

}
