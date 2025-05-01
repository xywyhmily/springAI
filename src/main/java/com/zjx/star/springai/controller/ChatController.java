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
import org.springframework.ai.model.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("ai")
public class ChatController {

    @Autowired
    private ChatClient chatClient;

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
    public Flux<String> demoStream(@RequestParam("prompt") String prompt ,
                                   @RequestParam("chatId")String chatId ,
                                   @RequestParam("files")List<MultipartFile> files) {

        chatHistoryRepository.save("chat",chatId);
        System.out.println("-----------------"+prompt);

        if (files==null||files.isEmpty()){
            return textChat(prompt,chatId);
        }else {
            return multiModChat(prompt,chatId,files);
        }

    }

    private Flux<String> multiModChat(String prompt, String chatId, List<MultipartFile> files) {

        List<Media> medias = files.stream().map(file -> new Media(MimeType.valueOf(file.getContentType()), file.getResource())).collect(Collectors.toList());

        return chatClient.prompt()
                .user(p->p.text(prompt).media(medias.toArray(Media[]::new)))
                .advisors(a->a.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY,chatId))
                .stream()
                .content();

    }

    private Flux<String> textChat(String prompt, String chatId) {

        return chatClient.prompt()
                .user(prompt)
                .advisors(a->a.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY,chatId))
                .stream()
                .content();

    }

}
