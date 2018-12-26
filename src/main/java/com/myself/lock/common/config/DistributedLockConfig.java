package com.myself.lock.common.config;

import com.myself.lock.lock.DistributedLock;
import com.myself.lock.lock.RedisDistributedLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 类名称：DistributedLockConfig<br>
 * 类描述：<br>
 * 创建时间：2018年12月26日<br>
 *
 * @author maopanpan
 * @version 1.0.0
 */
@Configuration
@ConditionalOnMissingBean(DistributedLock.class)
public class DistributedLockConfig {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    //纳秒
    private long lockRequestTimeout = 1000 * 1000 * 1000 * 2;
    //秒
    private long keyExpire = 120L;

    @Bean
    public DistributedLock buildDistributedLock() {
        return new RedisDistributedLock(lockRequestTimeout, keyExpire, redisTemplate);
    }
}
