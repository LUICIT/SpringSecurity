package com.aguilar.luisr.springsecurity.exceptions;

public class NotFoundException extends RuntimeException {

    public NotFoundException() {
        super("Register not found!");
    }

}
