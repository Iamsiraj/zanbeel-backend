package com.iconsult.userservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KakftaConfig {
    @Bean
    public NewTopic topic1()
    {
        return TopicBuilder.name("OTP").build();
    }
}
