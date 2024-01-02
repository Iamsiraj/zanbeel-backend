package com.iconsult.userservice.service.Impl;

import com.iconsult.userservice.Util.Util;
import com.iconsult.userservice.exception.ServiceException;
import com.iconsult.userservice.model.dto.request.OTPDto;
import com.iconsult.userservice.model.dto.response.ResponseDTO;
import com.iconsult.userservice.model.entity.Customer;
import com.iconsult.userservice.model.entity.OTPLog;
import com.iconsult.userservice.repository.OTPLogRepository;
import com.iconsult.userservice.service.OTPLogSerivce;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class OTPLogImpl implements OTPLogSerivce {

    private static final Logger LOGGER = LoggerFactory.getLogger(OTPLogImpl.class);

    private ResponseDTO response;

    @Autowired
    private OTPLogRepository otpLogRepository;

    @Autowired
    private CustomerServiceImpl customerServiceImpl;

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
    public ResponseDTO createOTP(OTPDto OTPDto)
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
    public ResponseDTO verifyOTP(OTPDto verifyOTPDto)
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
                            response = new ResponseDTO("Success", 200);
                            return response;
                        }
                    }
                    else
                    {
                        LOGGER.info("OTP does been match for customer [{}], replying...", verifyOTPDto.getMobileNumber());
                        response = new ResponseDTO("Failed", 101);
                        return response;
                    }
                }
                else
                {
                    LOGGER.info("OTP has been expired for customer [{}], replying...", verifyOTPDto.getMobileNumber());
                    response = new ResponseDTO("OTP has been expired for customer [" + verifyOTPDto.getMobileNumber() + "]", 101);
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

        response = new ResponseDTO("Success", 200);
        response.addField("OTP", otp);

        if(save(otpLog).getId() != null)
        {
            OTPDto.setOtp(otp);
            LOGGER.info("OTP has been saved with Id: {}", otpLog.getId());
            //kafkaTemplate.send("OTP", createOTPDto);
            LOGGER.info("OTP Sent Successfully to [{}]", OTPDto.getMobileNumber());
            return true;
        }
        return false;
    }
}
