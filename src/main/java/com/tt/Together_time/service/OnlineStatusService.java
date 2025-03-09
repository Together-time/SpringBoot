package com.tt.Together_time.service;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OnlineStatusService {
    private final ConcurrentHashMap<String, Boolean> onlineMembers = new ConcurrentHashMap<>();

    public void setOnline(String email){
        onlineMembers.put(email, true);
    }

    public void setOffline(String email){
        onlineMembers.remove(email);
    }

    public boolean isOnline(String email){
        return onlineMembers.containsKey(email);
    }

    public Set<String> getOnlineUsers() {
        return new HashSet<>(onlineMembers.keySet());
    }
}
