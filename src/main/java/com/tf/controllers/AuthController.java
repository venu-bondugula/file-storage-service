package com.tf.controllers;

import com.tf.exception.CustomException;
import com.tf.exception.ErrorResponse;
import com.tf.models.LoginRequest;
import com.tf.models.SecurityUser;
import com.tf.models.User;
import com.tf.services.JPAUserDetailsService;
import com.tf.services.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Log4j2
@CrossOrigin(originPatterns = "*", methods = {RequestMethod.POST})
@Tag(name = "Auth Operations", description = "APIs for signup and login for the users.")
@RequestMapping("api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private final TokenService tokenService;
    private final AuthenticationManager authManager;
    private final JPAUserDetailsService userDetailsService;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Generates a JWT token for the given username and password.", responses = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = Map.class), mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(), mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(), mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    public ResponseEntity<Map<String, String>> getJwtToken(@Validated(LoginRequest.class) @RequestBody LoginRequest request) {
        var authentication = authManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        String accessToken = tokenService.generateToken((SecurityUser) authentication.getPrincipal());
        return new ResponseEntity<>(Map.of("access_token", accessToken), HttpStatus.OK);
    }

    @PostMapping("/signup")
    @Operation(summary = "Sign Up", description = "Creates a new user with the given username and password.", responses = {
            @ApiResponse(responseCode = "201", description = "Success", content = @Content(schema = @Schema(implementation = User.class), mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    public ResponseEntity<User> createUser(@RequestBody LoginRequest request) throws CustomException {
        final String username = request.getUsername();
        if (userDetailsService.findByUsername(username).isPresent()) {
            throw new CustomException("A user with same username exists", HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(userDetailsService.saveUser(username, request.getPassword()), HttpStatus.CREATED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleSQLExceptions(Exception exe) {
        log.error(exe.getMessage(), exe);
        if (CustomException.class.isAssignableFrom(exe.getClass())) {
            CustomException cse = (CustomException) exe;
            return ResponseEntity.status(cse.getErrorCode())
                    .body(new ErrorResponse(exe.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(exe.getMessage()));
    }

}