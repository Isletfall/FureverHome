package cn.fzu.edu.furever_home.chat.service.impl;

import cn.fzu.edu.furever_home.auth.entity.User;
import cn.fzu.edu.furever_home.auth.mapper.UserMapper;
import cn.fzu.edu.furever_home.chat.dto.ConversationDTO;
import cn.fzu.edu.furever_home.chat.dto.MessageDTO;
import cn.fzu.edu.furever_home.chat.entity.Chat;
import cn.fzu.edu.furever_home.chat.entity.Message;
import cn.fzu.edu.furever_home.chat.mapper.ChatMapper;
import cn.fzu.edu.furever_home.chat.mapper.MessageMapper;
import cn.fzu.edu.furever_home.chat.request.SendMessageRequest;
import cn.fzu.edu.furever_home.chat.service.ChatService;
import cn.fzu.edu.furever_home.common.result.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final ChatMapper chatMapper;
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final cn.fzu.edu.furever_home.ws.WebSocketSessionManager wsSessionManager;
    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    @Override
    public PageResult<ConversationDTO> listConversations(Integer userId, int page, int pageSize) {
        Page<Chat> mpPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Chat> wrapper = new LambdaQueryWrapper<Chat>()
                .eq(Chat::getCreatorId, userId).or().eq(Chat::getReceiverId, userId)
                .orderByDesc(Chat::getLastTime);
        Page<Chat> result = chatMapper.selectPage(mpPage, wrapper);
        List<ConversationDTO> records = result.getRecords().stream().map(c -> {
            Integer targetId = Objects.equals(c.getCreatorId(), userId) ? c.getReceiverId() : c.getCreatorId();
            User u = userMapper.selectById(targetId);
            Message last = messageMapper.findLastByChat(c.getChatId());
            Long unread = countUnread(userId, c.getChatId());
            ConversationDTO dto = new ConversationDTO();
            dto.setConversationId(c.getChatId());
            dto.setTargetUserId(targetId);
            dto.setTargetUserName(u == null ? null : u.getUserName());
            dto.setTargetUserAvatar(u == null ? null : u.getAvatarUrl());
            dto.setLastMessage(last == null ? null : last.getContent());
            dto.setLastMessageTime(last == null ? null : last.getCreateTime());
            dto.setUnreadCount(unread);
            return dto;
        }).collect(Collectors.toList());
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public PageResult<MessageDTO> listMessages(Integer userId, Integer conversationId, int page, int pageSize,
            Integer beforeMessageId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<Message>()
                .eq(Message::getChatId, conversationId);
        if (beforeMessageId != null) {
            wrapper.lt(Message::getMessageId, beforeMessageId);
        }
        wrapper.orderByDesc(Message::getMessageId);
        Page<Message> mpPage = new Page<>(page, pageSize);
        Page<Message> result = messageMapper.selectPage(mpPage, wrapper);
        Chat c = chatMapper.selectById(conversationId);
        Integer receiverId = Objects.equals(c.getCreatorId(), userId) ? c.getReceiverId() : c.getCreatorId();
        Integer lastReadId = getLastReadId(userId, conversationId);
        List<MessageDTO> records = result.getRecords().stream().map(m -> {
            MessageDTO dto = new MessageDTO();
            dto.setMessageId(m.getMessageId());
            dto.setConversationId(m.getChatId());
            dto.setSenderId(m.getSenderId());
            dto.setReceiverId(receiverId);
            dto.setContent(m.getContent());
            dto.setMessageType("text");
            dto.setIsRead(lastReadId != null && m.getMessageId() <= lastReadId);
            dto.setCreatedAt(m.getCreateTime());
            return dto;
        }).collect(Collectors.toList());
        long total = messageMapper
                .selectCount(new LambdaQueryWrapper<Message>().eq(Message::getChatId, conversationId));
        boolean hasMore = !records.isEmpty() && (messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .eq(Message::getChatId, conversationId)
                .lt(Message::getMessageId, records.get(records.size() - 1).getMessageId())) > 0);
        return new PageResult<>(result.getCurrent(), result.getSize(), hasMore ? total : result.getTotal(), records);
    }

    @Override
    public MessageDTO sendMessage(Integer userId, SendMessageRequest req) {
        Integer chatId = req.getConversationId();
        if (chatId == null) {
            Chat found = findChatBetween(userId, req.getReceiverId());
            if (found == null) {
                Chat c = new Chat();
                c.setCreatorId(userId);
                c.setReceiverId(req.getReceiverId());
                c.setCreateTime(LocalDateTime.now());
                c.setLastTime(LocalDateTime.now());
                chatMapper.insert(c);
                chatId = c.getChatId();
            } else {
                chatId = found.getChatId();
            }
        }
        Message m = new Message();
        m.setChatId(chatId);
        m.setSenderId(userId);
        m.setContent(req.getContent());
        m.setCreateTime(LocalDateTime.now());
        messageMapper.insert(m);
        Chat c = chatMapper.selectById(chatId);
        c.setLastTime(LocalDateTime.now());
        chatMapper.updateById(c);
        Integer receiverId = Objects.equals(c.getCreatorId(), userId) ? c.getReceiverId() : c.getCreatorId();
        MessageDTO dto = new MessageDTO();
        dto.setMessageId(m.getMessageId());
        dto.setConversationId(chatId);
        dto.setSenderId(userId);
        dto.setReceiverId(receiverId);
        dto.setContent(m.getContent());
        dto.setMessageType(req.getMessageType() == null ? "text" : req.getMessageType());
        dto.setIsRead(false);
        dto.setCreatedAt(m.getCreateTime());
        try {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("type", "new_message");
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("messageId", dto.getMessageId());
            data.put("conversationId", dto.getConversationId());
            data.put("senderId", dto.getSenderId());
            cn.fzu.edu.furever_home.auth.entity.User su = userMapper.selectById(userId);
            data.put("senderName", su == null ? null : su.getUserName());
            data.put("senderAvatar", su == null ? null : su.getAvatarUrl());
            data.put("content", dto.getContent());
            data.put("messageType", dto.getMessageType());
            data.put("createdAt", dto.getCreatedAt());
            payload.put("data", data);
            String text = objectMapper.writeValueAsString(payload);
            log.info("WS event new_message -> userId={} payload={}", receiverId, text);
            wsSessionManager.sendToUser(receiverId, text);
            Integer unread = totalUnread(receiverId);
            java.util.Map<String, Object> uPayload = new java.util.HashMap<>();
            uPayload.put("type", "unread_count");
            java.util.Map<String, Object> uData = new java.util.HashMap<>();
            uData.put("totalUnread", unread);
            uPayload.put("data", uData);
            String uText = objectMapper.writeValueAsString(uPayload);
            log.info("WS event unread_count -> userId={} payload={}", receiverId, uText);
            wsSessionManager.sendToUser(receiverId, uText);
        } catch (Exception ignored) {
        }
        return dto;
    }

    @Override
    public void markConversationRead(Integer userId, Integer conversationId) {
        Message last = messageMapper.findLastByChat(conversationId);
        if (last != null) {
            setLastReadId(userId, conversationId, last.getMessageId());
            Chat c = chatMapper.selectById(conversationId);
            if (c != null) {
                Integer otherId = java.util.Objects.equals(c.getCreatorId(), userId) ? c.getReceiverId()
                        : c.getCreatorId();
                try {
                    java.util.Map<String, Object> payload = new java.util.HashMap<>();
                    payload.put("type", "read");
                    java.util.Map<String, Object> data = new java.util.HashMap<>();
                    data.put("conversationId", conversationId);
                    data.put("readerId", userId);
                    data.put("lastReadMessageId", last.getMessageId());
                    payload.put("data", data);
                    String text = objectMapper.writeValueAsString(payload);
                    log.info("WS event read -> userId={} payload={}", otherId, text);
                    wsSessionManager.sendToUser(otherId, text);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public PageResult<ConversationDTO> searchConversations(Integer userId, String keyword, int page, int pageSize) {
        Page<User> mpPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>().like(User::getUserName, keyword);
        Page<User> result = userMapper.selectPage(mpPage, wrapper);
        List<User> users = result.getRecords();
        java.util.List<ConversationDTO> records = users.stream().map(u -> {
            Chat c = findChatBetween(userId, u.getUserId());
            ConversationDTO dto = new ConversationDTO();
            dto.setConversationId(c == null ? null : c.getChatId());
            dto.setTargetUserId(u.getUserId());
            dto.setTargetUserName(u.getUserName());
            dto.setTargetUserAvatar(u.getAvatarUrl());
            dto.setUnreadCount(c == null ? 0L : countUnread(userId, c.getChatId()));
            if (c != null) {
                Message last = messageMapper.findLastByChat(c.getChatId());
                dto.setLastMessage(last == null ? null : last.getContent());
                dto.setLastMessageTime(last == null ? null : last.getCreateTime());
            }
            return dto;
        }).collect(Collectors.toList());
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public Integer totalUnread(Integer userId) {
        List<Chat> chats = chatMapper.selectList(
                new LambdaQueryWrapper<Chat>().eq(Chat::getCreatorId, userId).or().eq(Chat::getReceiverId, userId));
        long sum = 0L;
        for (Chat c : chats) {
            Long cnt = countUnread(userId, c.getChatId());
            sum += cnt == null ? 0 : cnt;
        }
        return (int) sum;
    }

    private Chat findChatBetween(Integer a, Integer b) {
        return chatMapper.selectOne(new LambdaQueryWrapper<Chat>()
                .and(w -> w.eq(Chat::getCreatorId, a).eq(Chat::getReceiverId, b))
                .or(w -> w.eq(Chat::getCreatorId, b).eq(Chat::getReceiverId, a)));
    }

    private String lastReadKey(Integer userId, Integer conversationId) {
        return "chat:lastread:" + conversationId + ":" + userId;
    }

    private Integer getLastReadId(Integer userId, Integer conversationId) {
        String v = stringRedisTemplate.opsForValue().get(lastReadKey(userId, conversationId));
        if (v == null || v.isBlank())
            return null;
        try {
            return Integer.parseInt(v);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void setLastReadId(Integer userId, Integer conversationId, Integer messageId) {
        stringRedisTemplate.opsForValue().set(lastReadKey(userId, conversationId), String.valueOf(messageId));
    }

    private Long countUnread(Integer userId, Integer conversationId) {
        Integer lastReadId = getLastReadId(userId, conversationId);
        if (lastReadId == null)
            lastReadId = 0;
        return messageMapper.countUnread(conversationId, userId, lastReadId);
    }
}
