package com.zjx.star.springai.controller;

/*
 *2025/4/29:15:14
 *version:1.0.0
 *@author:zjx
 */

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("demo")
public class DemoController {

    @Autowired
    private ChatClient chatModel;

    @GetMapping(value = "{message}" , produces = "text/html;charset=utf-8")
    public String demo(@PathVariable("message") String message) {

        System.out.println(message);
        return chatModel.prompt()
                .user(message)
                .call()
                .content();

    }

    @GetMapping(value = "stream/{message}" , produces = "text/html;charset=utf-8")
    public Flux<String> demoStream(@PathVariable("message") String message) {

        System.out.println(message);
        return chatModel.prompt()
                .user(message)
                .stream()
                .content();

    }

}
