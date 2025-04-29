package com.zjx.star.springai.repository;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
 *2025/4/29:21:14
 *version:1.0.0
 *@author:zjx
 */
@Component
public class InMemoryChatHistoryRepository implements ChatHistoryRepository {

    private final HashMap<String, List<String>> chatHistory = new HashMap<>();

    @Override
    public void save(String type, String chatId) {

        if (!chatHistory.containsKey(type)) {
            chatHistory.put(type,new ArrayList<String>());
        }
        List<String> chatIds = chatHistory.get(type);
        if (!chatIds.contains(chatId)) {
            chatIds.add(chatId);
        }

    }

    @Override
    public List<String> getChatIds(String type) {

        return chatHistory.getOrDefault(type,new ArrayList<>());

    }
}
