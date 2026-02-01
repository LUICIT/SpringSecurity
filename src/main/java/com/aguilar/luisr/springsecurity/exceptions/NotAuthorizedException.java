package com.aguilar.luisr.springsecurity.exceptions;

public class NotAuthorizedException extends RuntimeException {

    public NotAuthorizedException() {
        super("User not authorized!");
    }

}
