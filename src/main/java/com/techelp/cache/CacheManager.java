package com.techelp.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.techelp.model.entity.Usuario;
import com.techelp.model.entity.Chamado;
import java.util.concurrent.TimeUnit;

public class CacheManager {
    private static CacheManager instance;
    
    private final Cache<Long, Usuario> usuarioCache;
    private final Cache<Long, Chamado> chamadoCache;
    
    private CacheManager() {
        usuarioCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();
            
        chamadoCache = Caffeine.newBuilder()
            .maximumSize(2000)
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build();
    }
    
    public static synchronized CacheManager getInstance() {
        if (instance == null) {
            instance = new CacheManager();
        }
        return instance;
    }
    
    public Cache<Long, Usuario> getUsuarioCache() {
        return usuarioCache;
    }
    
    public Cache<Long, Chamado> getChamadoCache() {
        return chamadoCache;
    }
    
    public void clearAll() {
        usuarioCache.invalidateAll();
        chamadoCache.invalidateAll();
    }
} 