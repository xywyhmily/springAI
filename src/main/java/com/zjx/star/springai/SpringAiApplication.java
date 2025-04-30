package com.zjx.star.springai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.zjx.star.springai.mapper")
@SpringBootApplication
public class SpringAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiApplication.class, args);
    }

}
