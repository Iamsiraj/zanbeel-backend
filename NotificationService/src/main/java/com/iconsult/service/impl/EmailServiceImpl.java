package com.iconsult.service.impl;

import com.iconsult.userservice.model.dto.request.CreateOTPDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl {
    private JavaMailSender javaMailSender;


    @Autowired
    public EmailServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }
    @Async
    public void sendEmail(SimpleMailMessage mailMessage, String email, CreateOTPDto otpDto) {
        mailMessage.setTo(email);
        mailMessage.setSubject("Your Opt");
        mailMessage.setFrom("officesajjadahmeda@gmail.com");
        mailMessage.setText("Dear Customer, your OTP to complete your request is "
                +otpDto.getOtp()
                );


        javaMailSender.send(mailMessage);
    }
}
