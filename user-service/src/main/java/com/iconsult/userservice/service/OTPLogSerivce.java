package com.iconsult.userservice.service;



import com.iconsult.userservice.model.dto.request.OTPDto;
import com.iconsult.userservice.model.dto.response.ResponseDTO;
import com.iconsult.userservice.model.entity.OTPLog;
import com.zanbeel.customUtility.model.CustomResponseEntity;

import java.util.List;

public interface OTPLogSerivce {

    OTPLog save(OTPLog otpLog);

    List<OTPLog> findByMobileNumberAndIsExpired(String mobileNumber, Boolean isExpired);

    CustomResponseEntity<ResponseDTO> createOTP(OTPDto OTPDto);

    CustomResponseEntity<ResponseDTO> verifyOTP(OTPDto verifyOTPDto);
}
