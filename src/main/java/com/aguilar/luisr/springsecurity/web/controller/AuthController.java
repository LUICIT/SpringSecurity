package com.aguilar.luisr.springsecurity.web.controller;

import com.aguilar.luisr.springsecurity.exceptions.EmailAlreadyUsedException;
import com.aguilar.luisr.springsecurity.exceptions.InvalidPasswordException;
import com.aguilar.luisr.springsecurity.service.AuthService;
import com.aguilar.luisr.springsecurity.web.model.LoginModel;
import com.aguilar.luisr.springsecurity.web.model.RegisterUserModel;
import com.aguilar.luisr.springsecurity.web.model.TokenModel;
import com.aguilar.luisr.springsecurity.web.model.UserModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth Controller", description = "API Rest para autenticación")
public class AuthController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * {@code POST  /register} : register the user.
     *
     * @param registerUserModel the managed user View Model.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already used.
     */
    @Operation(
            summary = "Crea a un usuario",
            description = "Devuelve al usuario creado",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Creación exitosa",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserModel.class)
                            )),
                    @ApiResponse(responseCode = "404", description = "No encontrado",
                            content = @Content(mediaType = "application/json"
//                                    schema = @Schema(implementation = GlobalExceptionHandler.ApiError.class)
                            )),
                    @ApiResponse(responseCode = "409", description = "Conflictos con datos ingresados",
                            content = @Content(mediaType = "application/json"
//                                    schema = @Schema(implementation = GlobalExceptionHandler.ApiError.class)
                            ))
            }
    )
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserModel> registerAccount(@Valid @RequestBody RegisterUserModel registerUserModel) {
        UserModel registeredUser = authService.registerUser(registerUserModel);
//        mailService.sendActivationEmail(registeredUser);
        LOG.info("ResponseEntity created for User: {}", registeredUser);
        return ResponseEntity.ok(registeredUser);
    }

    /**
     * {@code POST  /login} : login the user.
     *
     * @param loginModel the managed user View Model.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if you hace incorrect data.
     */
    @Operation(
            summary = "Login de usuario",
            description = "Autentica por email/password y devuelve un JWT (access token)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Autenticación exitosa",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = TokenModel.class)
                            )),
                    @ApiResponse(responseCode = "401", description = "Credenciales inválidas",
                            content = @Content(mediaType = "application/json"))
            }
    )
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TokenModel> login(@Valid @RequestBody LoginModel loginModel) {
        /*if (isPasswordLengthInvalid(managedUserVM.getPassword())) {
            throw new InvalidPasswordException();
        }*/

        TokenModel tokenModel = authService.login(loginModel);

        LOG.debug("User logged!!!");
        return ResponseEntity.ok(tokenModel);
    }

}
