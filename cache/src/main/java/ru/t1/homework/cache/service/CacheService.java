package ru.t1.homework.cache.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import ru.t1.homework.cache.model.CacheKey;
import ru.t1.homework.cache.model.CacheEntry;

import java.util.concurrent.*;

@Slf4j
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
        log.debug("[CacheService] Put key={} with TTL={} ms", key, defaultTtlMillis);

        scheduler.schedule(() -> {
            // проверка истечения срока действия
            CacheEntry entry = cacheMap.get(key);
            if (entry != null && entry.getExpiryTimestamp() <= System.currentTimeMillis()) {
                cacheMap.remove(key);
                log.debug("[CacheService] Evicted expired key={} at {}, expiry timestamp={}",
                        key, System.currentTimeMillis(), expiryTimestamp);
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
            log.debug("[CacheService] Found expired key={} in get(), removed it", key);
            return null;
        }
        return entry.getValue();
    }

    @PreDestroy
    public void shutdownScheduler() {
        scheduler.shutdownNow();
    }
}
