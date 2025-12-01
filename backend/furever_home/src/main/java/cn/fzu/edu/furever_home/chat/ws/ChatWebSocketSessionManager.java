package cn.fzu.edu.furever_home.chat.ws;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class ChatWebSocketSessionManager {
    private final ConcurrentHashMap<Integer, CopyOnWriteArraySet<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    public void register(Integer userId, WebSocketSession session) {
        sessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
    }

    public void unregister(Integer userId, WebSocketSession session) {
        Set<WebSocketSession> set = sessions.get(userId);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) sessions.remove(userId);
        }
    }

    public void sendToUser(Integer userId, String text) {
        Set<WebSocketSession> set = sessions.get(userId);
        if (set == null || set.isEmpty()) return;
        TextMessage msg = new TextMessage(text);
        for (WebSocketSession s : set) {
            if (s.isOpen()) {
                try { s.sendMessage(msg); } catch (IOException ignored) {}
            }
        }
    }
}