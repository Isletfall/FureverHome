package cn.fzu.edu.furever_home.chat.service;

import cn.fzu.edu.furever_home.chat.dto.ConversationDTO;
import cn.fzu.edu.furever_home.chat.dto.MessageDTO;
import cn.fzu.edu.furever_home.chat.request.SendMessageRequest;
import cn.fzu.edu.furever_home.common.result.PageResult;

import java.util.List;

public interface ChatService {
    PageResult<ConversationDTO> listConversations(Integer userId, int page, int pageSize);
    PageResult<MessageDTO> listMessages(Integer userId, Integer conversationId, int page, int pageSize, Integer beforeMessageId);
    MessageDTO sendMessage(Integer userId, SendMessageRequest req);
    void markConversationRead(Integer userId, Integer conversationId);
    PageResult<ConversationDTO> searchConversations(Integer userId, String keyword, int page, int pageSize);
    Integer totalUnread(Integer userId);
}