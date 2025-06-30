package ru.t1.starter.service;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import ru.t1.starter.model.CacheEntry;
import ru.t1.starter.model.CacheKey;

import java.util.concurrent.*;

@Service
public class CacheService {

    private final ConcurrentHashMap<CacheKey, CacheEntry> cacheMap = new ConcurrentHashMap<>();

    private final long defaultTtlMillis;
    private final ScheduledExecutorService scheduler;

    public CacheService(@Value("${cache.default-ttl-seconds}") long defaultTtlSeconds) {
        this.defaultTtlMillis = defaultTtlSeconds * 1000;

        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            t.setName("CacheEvictScheduler");
            return t;
        });
    }

    /**
     * Добавление в кэш, планировка удаления через defaultTtlMillis.
     *
     * @param key   ключ кэша
     * @param value значение
     */
    public void put(CacheKey key, Object value) {

        long expiryTimestamp = System.currentTimeMillis() + defaultTtlMillis;
        cacheMap.put(key, new CacheEntry(value, expiryTimestamp));

        scheduler.schedule(() -> {
            // проверка истечения срока действия
            CacheEntry entry = cacheMap.get(key);
            if (entry != null && entry.getExpiryTimestamp() <= System.currentTimeMillis()) {
                cacheMap.remove(key);
            }
        }, defaultTtlMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Возвращает из кэша: если записи нет или она уже устарела, возвращает null.
     *
     * @param key ключ
     * @return значение или null
     */
    public Object get(CacheKey key) {
        CacheEntry entry = cacheMap.get(key);
        if (entry == null) {
            return null;
        }
        if (System.currentTimeMillis() >= entry.getExpiryTimestamp()) {
            // Если время вышло, удаляем и возвращаем null
            cacheMap.remove(key);
            return null;
        }
        return entry.getValue();
    }

    @PreDestroy
    public void shutdownScheduler() {
        scheduler.shutdownNow();
    }
}
