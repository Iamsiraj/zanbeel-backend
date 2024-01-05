package com.iconsult.service.impl;


import com.iconsult.userservice.model.dto.request.CreateOTPDto;
import com.iconsult.service.NotificationConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;


@Service
public class NotificationConsumerServiceImpl implements NotificationConsumerService {
    @Autowired
    private EmailServiceImpl emailServiceImpl;
    @KafkaListener(topics = "OTP", groupId = "myGroup")


    public void consumeMsg(@Payload CreateOTPDto otpDto) {
        System.out.println(otpDto.toString());
        SimpleMailMessage mailMessage=new SimpleMailMessage();
        String email=otpDto.getEmail();
        emailServiceImpl.sendEmail(mailMessage,email,otpDto);
        System.out.println("email has been sent");

    }
}