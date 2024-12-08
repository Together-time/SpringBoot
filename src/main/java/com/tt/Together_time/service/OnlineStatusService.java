package com.tt.Together_time.service;

import org.springframework.stereotype.Service;

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
}
