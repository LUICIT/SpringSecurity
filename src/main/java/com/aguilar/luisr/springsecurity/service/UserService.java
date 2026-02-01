package com.aguilar.luisr.springsecurity.service;

import com.aguilar.luisr.springsecurity.converter.UserConverter;
import com.aguilar.luisr.springsecurity.domain.repository.UserRepository;
import com.aguilar.luisr.springsecurity.exceptions.NotFoundException;
import com.aguilar.luisr.springsecurity.web.model.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserConverter userConverter = new UserConverter();

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserModel getById(Long id) {
        return userConverter.toModel(userRepository.findById(id).orElseThrow(NotFoundException::new));
    }

}
