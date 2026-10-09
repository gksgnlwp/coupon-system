package com.hwije.coupon_system.exception;

public class ErrorResponse {

    private final boolean success;
    private final String message;

    public ErrorResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
