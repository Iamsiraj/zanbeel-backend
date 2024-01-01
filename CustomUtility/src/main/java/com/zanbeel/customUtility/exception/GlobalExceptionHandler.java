package com.zanbeel.customUtility.exception;

import com.zanbeel.customUtility.model.CustomResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public CustomResponseEntity<Object> handleNotFoundException(ServiceException ex) {
        return CustomResponseEntity.error(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public CustomResponseEntity<Object> handleGenericException(Exception ex) {
        return CustomResponseEntity.error(ex.getMessage());
    }
}
