package com.zjx.star.springai.controller;

import com.zjx.star.springai.entity.vo.MessageVo;
import com.zjx.star.springai.repository.ChatHistoryRepository;
import com.zjx.star.springai.repository.InMemoryChatHistoryRepository;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/*
 *2025/4/29:21:25
 *version:1.0.0
 *@author:zjx
 */
@RequestMapping("/ai/history")
@RestController
public class ChatHistoryController {

    @Autowired
    private ChatHistoryRepository chatHistoryRepository;

    @Autowired
    private ChatMemory chatMemory;

    @GetMapping("{type}")
    public List<String> getChatIds(@PathVariable("type") String type){

        return chatHistoryRepository.getChatIds(type);

    }

    @GetMapping("{type}/{chatId}")
    public List<MessageVo> getChatHistory(@PathVariable("type") String type, @PathVariable("chatId") String chatId) {

        List<Message> messages = chatMemory.get(chatId, Integer.MAX_VALUE);
        if (messages == null) {
            return List.of();
        }
        return messages.stream().map(MessageVo::new).collect(Collectors.toList());

    }


}
