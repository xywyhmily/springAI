package com.zjx.star.springai.config;

/*
 *2025/4/29:15:06
 *version:1.0.0
 *@author:zjx
 */

import com.zjx.star.springai.constant.ChatRoleConstant;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(OpenAiChatModel model,ChatMemory chatMemory) {

        return ChatClient.builder(model)
                .defaultSystem(ChatRoleConstant.douyin_xiaomei)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();

    }

    @Bean
    public ChatClient gameChatClient(OpenAiChatModel model,ChatMemory chatMemory) {

        return ChatClient.builder(model)
                .defaultSystem(ChatRoleConstant.help_girlfriend_happy)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();

    }

    @Bean
    public ChatMemory chatMemory() {

        return new InMemoryChatMemory();

    }

}
