package com.iconsult.service;

import com.iconsult.userservice.model.dto.request.CreateOTPDto;
import org.springframework.messaging.handler.annotation.Payload;

public interface NotificationConsumerService {

    public void consumeMsg(@Payload CreateOTPDto otpDto);
}
