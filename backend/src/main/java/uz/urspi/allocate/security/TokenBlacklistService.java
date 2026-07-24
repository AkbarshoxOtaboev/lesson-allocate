package uz.urspi.allocate.security;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory logout blacklist keyed by raw JWT, valued by the epoch millis at which
 * the token would have expired anyway. No Redis required; entries are swept lazily
 * on every lookup instead of via a scheduled job.
 */
@Service
public class TokenBlacklistService {

    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    public void blacklist(String token, long ttlMs) {
        if (token == null || token.isBlank() || ttlMs <= 0) {
            return;
        }
        blacklist.put(token, System.currentTimeMillis() + ttlMs);
    }

    public boolean isBlacklisted(String token) {
        cleanExpired();
        return token != null && blacklist.containsKey(token);
    }

    private void cleanExpired() {
        long now = System.currentTimeMillis();
        blacklist.entrySet().removeIf(entry -> entry.getValue() < now);
    }
}
