package com.zjx.star.springai.repository;

import java.util.List;

/*
 *2025/4/29:21:11
 *version:1.0.0
 *@author:zjx
 */
public interface ChatHistoryRepository {

//    保存会话记录
    void save(String type , String chatId);

//    获取会话id列表
    List<String> getChatIds(String type);

}
