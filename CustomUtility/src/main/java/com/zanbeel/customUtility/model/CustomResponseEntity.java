package com.zanbeel.customUtility.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomResponseEntity<T> {
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int errorCode;
    private boolean success;
    private String message;
    private Map<String, Object> data;

    public CustomResponseEntity(T data, String message) {
        this.success = true;
        this.message = message;
        this.data = new HashMap<>();
    }

    public CustomResponseEntity(String message) {
        this.success = true;
        this.message = message;
        this.data = new HashMap<>();
    }

    public CustomResponseEntity(int errorCode, String message) {
        this.errorCode = errorCode;
        this.success = false;
        this.message = message;
    }

    public void addField(String fieldName, Object value) {
        data.put(fieldName, value);
    }

    // Static method to create an ApiResponse for exceptions
    public static <T> CustomResponseEntity<T> errorResponse(Exception exception) {
        return new CustomResponseEntity<>(1, "An error occurred: " + exception.getMessage());
    }

    public static <T> CustomResponseEntity<T> error(String error) {
        CustomResponseEntity<T> response = new CustomResponseEntity<>();
        response.setMessage(error);
        response.setErrorCode(1000);
        response.setSuccess(false);
        return response;
    }
}
