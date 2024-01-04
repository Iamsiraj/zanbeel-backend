package com.zanbeel.BeneficiaryService.service.impl;


import com.zanbeel.BeneficiaryService.enums.BeneficiaryMessage;
import com.zanbeel.BeneficiaryService.enums.UserMessage;
import com.zanbeel.BeneficiaryService.feign.CustomerService;
import com.zanbeel.BeneficiaryService.model.dto.request.BeneficiaryRequestDto;
import com.zanbeel.BeneficiaryService.model.dto.request.UpdateBeneficiaryRequestDto;
import com.zanbeel.BeneficiaryService.model.dto.response.BeneficiaryResponseDto;
import com.zanbeel.BeneficiaryService.model.dto.response.CustomerResponseDto;
import com.zanbeel.BeneficiaryService.model.entity.Beneficiary;
import com.zanbeel.BeneficiaryService.model.mapper.BeneficiaryMapper;
import com.zanbeel.BeneficiaryService.repository.BeneficiaryRepository;
import com.zanbeel.BeneficiaryService.service.BeneficiaryService;

import com.zanbeel.customUtility.exception.ServiceException;
import com.zanbeel.customUtility.model.CustomResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BeneficiaryServiceImpl implements BeneficiaryService{
    @Autowired
    private BeneficiaryRepository beneficiaryRepository;

    @Autowired
    private CustomerService customerService;

    @Override
    public CustomResponseEntity<BeneficiaryResponseDto> getBeneficiaryById(Long beneficiaryId) {
        Optional<Beneficiary> beneficiary = beneficiaryRepository.findById(beneficiaryId);
        if (!beneficiary.isPresent()) {
            throw new ServiceException(BeneficiaryMessage.BENEFICIARY_NOT_FOUND.getValue());
        }
        BeneficiaryResponseDto beneficiaryResponseDto = BeneficiaryMapper.INSTANCE.
                mapBeneficiaryToBeneficiaryResponseDto(beneficiary.get());
        return CustomResponseEntity.<BeneficiaryResponseDto>builder().data(beneficiaryResponseDto).build();
    }
    @Override
    public CustomResponseEntity<List<BeneficiaryResponseDto>> getAllBeneficiaryByCustomerId(Long customerId) {
        List<Beneficiary> beneficiaries = beneficiaryRepository.findByCustomerId(customerId);
        List<BeneficiaryResponseDto> beneficiaryResponseDtos = beneficiaries.stream()
                .map(BeneficiaryMapper.INSTANCE::mapBeneficiaryToBeneficiaryResponseDto).collect(Collectors.toList());
        return CustomResponseEntity.<List<BeneficiaryResponseDto>>builder().data(beneficiaryResponseDtos).build();
    }
    @Override
    public CustomResponseEntity<String> deleteBeneficiaryById(Long BeneficiaryId) {
        Optional<Beneficiary> beneficiary = beneficiaryRepository.findById(BeneficiaryId);
        if (!beneficiary.isPresent()) {
            throw new ServiceException(BeneficiaryMessage.BENEFICIARY_NOT_FOUND.getValue());
        }
        beneficiaryRepository.delete(beneficiary.get());
        return CustomResponseEntity.<String>builder().data(BeneficiaryMessage.BENEFICIARY_DELETED.getValue()).build();
    }

    @Override
    public CustomResponseEntity<Beneficiary> addBeneficiary(BeneficiaryRequestDto beneficiaryRequestDto) {
        Optional<CustomerResponseDto> customerResponseDto = customerService.
                getCustomerById(beneficiaryRequestDto.getCustomerId());
        if (!customerResponseDto.isPresent()) {
            throw new ServiceException(UserMessage.USER_NOT_FOUND.getValue());
        }
        Optional<Beneficiary> OptionalBeneficiary = beneficiaryRepository.
                findByAccountNumberAndCustomerId(beneficiaryRequestDto.getAccountNumber(),
                beneficiaryRequestDto.getCustomerId());
        if (OptionalBeneficiary.isPresent()) {
            throw new ServiceException(BeneficiaryMessage.BENEFICIARY_ALREADY_EXIST.getValue());
        }
        Beneficiary beneficiary = BeneficiaryMapper.INSTANCE.mapBeneficiaryRequestDtoToBeneficiary(beneficiaryRequestDto);
        beneficiaryRepository.save(beneficiary);
        return CustomResponseEntity.<Beneficiary>builder().data(beneficiary).build();
    }

    @Override
    public CustomResponseEntity<Beneficiary> updateBeneficiary(Long beneficiaryId,
                                                               UpdateBeneficiaryRequestDto updateBeneficiaryRequestDto) {
        Optional<Beneficiary> beneficiary = beneficiaryRepository.findById(beneficiaryId);
        if (!beneficiary.isPresent()) {
            throw new ServiceException(BeneficiaryMessage.BENEFICIARY_NOT_FOUND.getValue());
        }
        beneficiary.get().setNickName(updateBeneficiaryRequestDto.getNickName());
        beneficiaryRepository.save(beneficiary.get());
        return CustomResponseEntity.<Beneficiary>builder().data(beneficiary.get()).build();
    }
}
