package com.iconsult.userservice.service.Impl;

import com.iconsult.userservice.Util.Util;
import com.iconsult.userservice.exception.ServiceException;
import com.iconsult.userservice.model.dto.request.OTPDto;
import com.iconsult.userservice.model.dto.response.KafkaMessageDto;
import com.iconsult.userservice.model.dto.response.ResponseDTO;
import com.iconsult.userservice.model.entity.Customer;
import com.iconsult.userservice.model.entity.OTPLog;
import com.iconsult.userservice.repository.OTPLogRepository;
import com.iconsult.userservice.service.OTPLogSerivce;
import com.zanbeel.customUtility.model.CustomResponseEntity;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class OTPLogImpl implements OTPLogSerivce {

    private static final Logger LOGGER = LoggerFactory.getLogger(OTPLogImpl.class);

    private CustomResponseEntity<ResponseDTO> response;

    @Autowired
    private OTPLogRepository otpLogRepository;

    @Autowired
    private CustomerServiceImpl customerServiceImpl;

    private KafkaMessageDto kafkaMessage;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Override
    public OTPLog save(OTPLog otpLog) {
        return this.otpLogRepository.save(otpLog);
    }

    @Override
    public List<OTPLog> findByMobileNumberAndIsExpired(String mobileNumber, Boolean isExpired) {
        return this.otpLogRepository.findByMobileNumberAndIsExpired(mobileNumber, isExpired);
    }

    @Override
    public CustomResponseEntity<ResponseDTO> createOTP(OTPDto OTPDto)
    {
        LOGGER.info("Executing createOTP Request...");

        Customer customer = this.customerServiceImpl.findByMobileNumber(OTPDto.getMobileNumber());

        if(customer != null)
        {
            LOGGER.error("Customer already exists with mobile [" + OTPDto.getMobileNumber() + "], cannot allow signup, rejecting...");
            throw new ServiceException(String.format("User with Mobile Number %s already exists", OTPDto.getMobileNumber()));
        }

        // making all OTP for this mobile number expire
        for (OTPLog otp : findByMobileNumberAndIsExpired(OTPDto.getMobileNumber(), false)) {
            otp.setIsExpired(true);
            otp.setIsVerified(false);
            save(otp);
        }

        if(!createandSendOTP(OTPDto))
        {
            LOGGER.error("Failed to create & Send OTP for Mobile [" + OTPDto.getMobileNumber() + "], rejecting...");
            throw new ServiceException("SMS Gateway Down");
        }

        return response;
    }

    @Override
    public CustomResponseEntity<ResponseDTO> verifyOTP(OTPDto verifyOTPDto)
    {
        LOGGER.info("Executing confirmOTP Request...");

        List<OTPLog> otpLogList = findByMobileNumberAndIsExpired(verifyOTPDto.getMobileNumber(), false);

        if(otpLogList != null && !otpLogList.isEmpty())
        {
            for(OTPLog otp : otpLogList)
            {
                if(otp.getExpiryDateTime() > Long.parseLong(Util.dateFormat.format(new Date())))
                {
                    if(otp.getOTP().equals(verifyOTPDto.getOtp()) && !otp.getIsVerified())
                    {
                        otp.setIsExpired(true);
                        otp.setVerifyDateTime(Long.parseLong(Util.dateFormat.format(new Date())));

                        if(save(otp).getId() != null)
                        {
                            LOGGER.info("OTP verified successfully for customer [{}], replying...", verifyOTPDto.getMobileNumber());
                            response = new CustomResponseEntity<>(new ResponseDTO("Success", 200), null);
                            return response;
                        }
                    }
                    else
                    {
                        LOGGER.info("OTP does been match for customer [{}], replying...", verifyOTPDto.getMobileNumber());
                        response = new CustomResponseEntity<>(new ResponseDTO("Failed", 200), null);
                        return response;
                    }
                }
                else
                {
                    LOGGER.info("OTP has been expired for customer [{}], replying...", verifyOTPDto.getMobileNumber());
                    response = new CustomResponseEntity<>(new ResponseDTO("OTP has been expired for customer [" + verifyOTPDto.getMobileNumber() + "]", 101), null);
                    return response;
                }
            }

        }

        throw new ServiceException(String.format("No Otp found against mobilenumber [%s], rejecting...", verifyOTPDto.getMobileNumber()));
    }

    public Boolean createandSendOTP(OTPDto OTPDto)
    {
        LOGGER.info("Generating OTP...");
        String otp = Util.generateOTP(6); // Generating OTP of length 6
        LOGGER.info("OTP Generated...");

        OTPLog otpLog = new OTPLog();
        otpLog.setMobileNumber(OTPDto.getMobileNumber());
        otpLog.setEmail(OTPDto.getEmail());
        otpLog.setOTP(otp);
        otpLog.setIsExpired(false);
        otpLog.setIsVerified(false);
        otpLog.setCreateDateTime(Long.parseLong(Util.dateFormat.format(new Date())));
        otpLog.setExpiryDateTime(Long.parseLong(Util.dateFormat.format(DateUtils.addMinutes(new Date(), 1))));
        otpLog.setSmsMessage("Dear Customer, your OTP to complete your request is " + otp);

        response = new CustomResponseEntity<>(new ResponseDTO("Success", 200), null);
        response.getData().addField("OTP", otp);

        if(save(otpLog).getId() != null)
        {
            OTPDto.setOtp(otp);
            LOGGER.info("OTP has been saved with Id: {}", otpLog.getId());
            kafkaMessage = new KafkaMessageDto(OTPDto.getEmail(), "OTP", "Dear Customer, your OTP is " + OTPDto.getOtp(), true, false);
            sendMessage(kafkaMessage, "OTP");
            LOGGER.info("OTP Sent Successfully to [{}]", OTPDto.getMobileNumber());
            return true;
        }
        return false;
    }

    public void sendMessage(Object message, String topicName)
    {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topicName, message);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                LOGGER.info("Sent message=[" + message.toString() +
                        "] with offset=[" + result.getRecordMetadata().offset() + "]");
            } else {
                LOGGER.error("Unable to send message=[" +
                        message + "] due to : " + ex.getMessage());
            }
        });
    }
}