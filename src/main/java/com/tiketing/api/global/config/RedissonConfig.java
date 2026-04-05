package com.tiketing.api.global.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
	
	@Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;
    
    private static final String REDISSON_HOST_PREFIX = "redis://";
    
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        
        // 싱글톤(단일 서버) 모드로 Redis에 접속하도록 설정
        config.useSingleServer()
              .setAddress(REDISSON_HOST_PREFIX + redisHost + ":" + redisPort);
        
        // 이 설정을 바탕으로 RedissonClient 객체를 생성해서 스프링 컨테이너에 쏙 넣어줍니다!
        return Redisson.create(config);
    }
}
