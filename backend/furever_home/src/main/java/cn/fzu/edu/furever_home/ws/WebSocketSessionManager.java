package cn.fzu.edu.furever_home.ws;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class WebSocketSessionManager {
    private final ConcurrentHashMap<Integer, CopyOnWriteArraySet<WebSocketSession>> sessions = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(WebSocketSessionManager.class);

    public void register(Integer userId, WebSocketSession session) {
        sessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
        log.info("WS register userId={} sessionId={}", userId, session.getId());
    }

    public void unregister(Integer userId, WebSocketSession session) {
        Set<WebSocketSession> set = sessions.get(userId);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty())
                sessions.remove(userId);
        }
        log.info("WS unregister userId={} sessionId={}", userId, session.getId());
    }

    public void sendToUser(Integer userId, String text) {
        Set<WebSocketSession> set = sessions.get(userId);
        if (set == null || set.isEmpty())
            return;
        TextMessage msg = new TextMessage(text);
        log.info("WS push to userId={} payload={}", userId, text);
        for (WebSocketSession s : set) {
            if (s.isOpen()) {
                try {
                    s.sendMessage(msg);
                } catch (IOException ignored) {
                }
            }
        }
    }

    public boolean hasOnline(Integer userId) {
        Set<WebSocketSession> set = sessions.get(userId);
        return set != null && !set.isEmpty();
    }
}
