package com.thkim.toyproject.fintrack.infrastructure.persistence;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryRefreshTokenStore {
    private final Map<String, String> store = new ConcurrentHashMap<>();

    public void saveOrRotate(String username, String refresh) {
        store.put(username, refresh);
    }

    public boolean isValid(String refresh) {
        return store.containsValue(refresh);
    }

    public void rotate(String username, String oldRefresh, String newRefresh) {
        if (oldRefresh != null && oldRefresh.equals(store.get(username))) {
            store.put(username, newRefresh);
        }
        else {
            store.remove(username); // 재사용 의심 → 만료
        }
    }

    public void revoke(String refresh) {
        store.entrySet().removeIf(e -> refresh.equals(e.getValue()));
    }
}
