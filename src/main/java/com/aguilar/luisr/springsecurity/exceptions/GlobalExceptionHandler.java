package com.aguilar.luisr.springsecurity.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ApiError(String code, String message) {}

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadCredentialsException.class)
    public ApiError handleNotFound(BadCredentialsException ex) {
        return new ApiError("CONFLICT_LOGIN", ex.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(NotAuthorizedException.class)
    public ApiError handleNotFound(NotAuthorizedException ex) {
        return new ApiError("UNAUTHORIZED", ex.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(HttpClientErrorException.Forbidden.class)
    public ApiError handleNotFound(HttpClientErrorException.Forbidden ex) {
        return new ApiError("FORBIDDEN", ex.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ApiError handleNotFound(NotFoundException ex) {
        return new ApiError("REGISTER_NOT_FOUND", ex.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ApiError handleNotFound(EmailAlreadyUsedException ex) {
        return new ApiError("DUPLICATE_EMAIL", ex.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ApiError handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        return new ApiError("CONCURRENCY_CONFLICT", "The resource was updated by another request. Please reload and retry.");
    }

}
