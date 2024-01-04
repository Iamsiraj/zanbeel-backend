package com.iconsult.userservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic topicOTP()
    {
        return TopicBuilder.name("OTP").build();
    }

    @Bean
    public NewTopic topicForgetUserName()
    {
        return TopicBuilder.name("forgetUserName").build();
    }
}
