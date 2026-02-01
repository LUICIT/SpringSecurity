package com.aguilar.luisr.springsecurity.converter;

import com.aguilar.luisr.springsecurity.domain.entity.User;
import com.aguilar.luisr.springsecurity.web.model.RegisterUserModel;
import com.aguilar.luisr.springsecurity.web.model.UserModel;
import org.jspecify.annotations.NonNull;

public class UserConverter {

    public UserModel toModel(User user) {
        UserModel userModel = new UserModel();
        userModel.setId(user.getId());
        userModel.setNames(user.getNames());
        userModel.setLastName(user.getLastName());
        userModel.setSecondLastName(user.getSecondLastName());
        userModel.setEmail(user.getEmail());
        userModel.setPhone(user.getPhone());
        userModel.setUserType(user.getUserType());
        return userModel;
    }

    public User toEntity(RegisterUserModel registerUserModel, String encryptedPassword) {
        return getUser(encryptedPassword, null, registerUserModel.getNames(), registerUserModel.getLastName(), registerUserModel.getSecondLastName(), registerUserModel.getEmail(), registerUserModel.getPhone(), registerUserModel.getUserType());
    }

    public User toEntity(UserModel userModel, String encryptedPassword) {
        return getUser(encryptedPassword, userModel.getId(), userModel.getNames(), userModel.getLastName(), userModel.getSecondLastName(), userModel.getEmail(), userModel.getPhone(), userModel.getUserType());
    }

    @NonNull
    private User getUser(String encryptedPassword, Long id, String names, String lastName, String secondLastName, String email, String phone, String userType) {
        User user = new User();
        user.setId(id);
        user.setNames(names);
        user.setLastName(lastName);
        user.setSecondLastName(secondLastName);
        user.setEmail(email.toLowerCase());
        user.setPassword(encryptedPassword);
        user.setPhone(phone);
        user.setUserType(userType);
        return user;
    }

}
