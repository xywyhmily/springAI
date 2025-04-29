package com.zjx.star.springai.config;

/*
 *2025/4/29:15:06
 *version:1.0.0
 *@author:zjx
 */

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(OpenAiChatModel model) {

        return ChatClient.builder(model)
                .defaultSystem("你是电影中的小美，现在抖音短视频里的角色，请以她的视角回答")
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();

    }

}
