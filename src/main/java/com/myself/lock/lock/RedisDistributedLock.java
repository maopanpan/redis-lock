package com.myself.lock.lock;

import com.myself.lock.common.constants.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;

import java.util.*;

/**
 * 类名称：RedisDistributedLock<br>
 * 类描述：<br>
 * 创建时间：2018年12月26日<br>
 *
 * @author maopanpan
 * @version 1.0.0
 */
@Slf4j
public class RedisDistributedLock implements DistributedLock {
    private static final String LUA_LOCK_SCRIPT;
    public static final String LUA_UNLOCK_SCRIPT;

    private final long lockRequestTimeout;
    private final long keyExpire;
    private final RedisTemplate<String, String> template;
    private ThreadLocal<String> threadLocal;
    private String defaultValue;
    private volatile boolean isLocked = false;


    static {
        //LUA加锁脚本
        StringBuilder lockSB = new StringBuilder();
        lockSB.append("if (redis.call(\"exists\",KEYS[1]) == 0 or redis.call(\"get\",KEYS[1]) == ARGV[2]) ");
        lockSB.append("then ");
        lockSB.append("    return redis.call('setex',KEYS[1],ARGV[1],ARGV[2]) ");
        lockSB.append("else ");
        lockSB.append("    return \"0\" ");
        lockSB.append("end ");
        LUA_LOCK_SCRIPT = lockSB.toString();
        //LUA锁释放脚本
        StringBuilder unLockSB = new StringBuilder();
        unLockSB.append("if (redis.call(\"exists\",KEYS[1]) == 0 or redis.call(\"get\",KEYS[1]) == ARGV[1]) ");
        unLockSB.append("then ");
        unLockSB.append("    return redis.call(\"del\",KEYS[1]) ");
        unLockSB.append("else ");
        unLockSB.append("    return \"0\" ");
        unLockSB.append("end ");
        LUA_UNLOCK_SCRIPT = unLockSB.toString();
    }

    public RedisDistributedLock(long lockRequestTimeout, long keyExpire, RedisTemplate<String, String> template) {
        this.lockRequestTimeout = lockRequestTimeout;
        this.keyExpire = keyExpire;
        this.template = template;
        threadLocal = new ThreadLocal<>();
    }

    @Override
    public boolean tryLock(String key) {
        defaultValue = key + ":" + UUID.randomUUID().toString();
        long start = System.nanoTime();
        Random random = new Random();
        long diff = System.nanoTime() - start;
        while (diff < lockRequestTimeout) {
            final List<String> keys = Collections.singletonList(key);
            final List<String> args = Arrays.asList(String.valueOf(keyExpire), defaultValue);
            Object res = template.execute(new RedisCallback<Object>() {
                @Override
                public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                    Object conn = redisConnection.getNativeConnection();
                    if (conn instanceof JedisCluster) {
                        return ((JedisCluster) conn).eval(LUA_LOCK_SCRIPT, keys, args);
                    } else if (conn instanceof Jedis) {
                        return ((Jedis) conn).eval(LUA_LOCK_SCRIPT, keys, args);
                    } else {
                        return "0";
                    }
                }
            });
            if (Objects.equals(res, RedisConstants.OK)) {
                threadLocal.set(defaultValue);
                isLocked = Boolean.TRUE;
                return Boolean.TRUE;
            }
            try {
                //短暂休眠，避免可能的活锁
                Thread.sleep(10, random.nextInt(500));
            } catch (InterruptedException e) {
                throw new RuntimeException("Locking error", e);
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean unLock(String key) {
        try {
            if (isLocked) {
                String value = threadLocal.get();
                if (Objects.nonNull(value)) {
                    LOGGER.trace("释放锁{}，值为{}", key, value);
                    final List<String> keys = Collections.singletonList(key);
                    final List<String> args = Collections.singletonList(value);
                    Object res = template.execute(new RedisCallback<Object>() {
                        @Override
                        public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                            Object conn = redisConnection.getNativeConnection();
                            if (conn instanceof JedisCluster) {
                                return ((JedisCluster) conn).eval(LUA_UNLOCK_SCRIPT, keys, args);
                            } else if (conn instanceof Jedis) {
                                return ((Jedis) conn).eval(LUA_UNLOCK_SCRIPT, keys, args);
                            } else {
                                return 0;
                            }
                        }
                    });
                    if (Objects.nonNull(res) && Long.parseLong(res.toString()) > 0) {
                        isLocked = Boolean.FALSE;
                        return Boolean.TRUE;
                    }
                }
            }
            return Boolean.FALSE;
        } finally {
            threadLocal.remove();
        }
    }
}
