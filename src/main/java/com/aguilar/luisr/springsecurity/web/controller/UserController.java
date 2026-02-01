package com.aguilar.luisr.springsecurity.web.controller;

import com.aguilar.luisr.springsecurity.exceptions.GlobalExceptionHandler;
import com.aguilar.luisr.springsecurity.exceptions.NotFoundException;
import com.aguilar.luisr.springsecurity.service.UserService;
import com.aguilar.luisr.springsecurity.web.model.UserModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Controller", description = "API Rest para gestión de usuarios")
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * {@code GET /{id}} : get a user.
     *
     * @param id the managed user View Model.
     * @throws NotFoundException {@code 404 (Not Found)} if the register not found.
     */
    @Operation(
            summary = "Obtener a una persona por ID",
            description = "Devuelve una persona si existe",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operación exitosa",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserModel.class))),
                    @ApiResponse(responseCode = "400", description = "Error de cliente",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = GlobalExceptionHandler.ApiError.class))),
                    @ApiResponse(responseCode = "401", description = "No autorizado"),
                    @ApiResponse(responseCode = "404", description = "No encontrado",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = GlobalExceptionHandler.ApiError.class))),
                    @ApiResponse(responseCode = "409", description = "Conflictos con datos ingresados",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = GlobalExceptionHandler.ApiError.class)))
            }
    )
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserModel> getById(@Parameter(description = "ID de la persona", example = "1", required = true) @PathVariable Long id) {

        UserModel userModel = userService.getById(id);
        LOG.debug("ResponseEntity user found: {}", userModel);
        return ResponseEntity.ok(userModel);

    }

}
