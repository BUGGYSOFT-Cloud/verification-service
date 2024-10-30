package com.buggysoft.verification.controller;

import com.buggysoft.verification.service.VerificationService;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping
@Tag(name = "Verification API", description = "User verification management operations")
public class VerificationController {
  private final VerificationService verificationService;

  @Autowired
  public VerificationController(VerificationService verificationService) {
    this.verificationService = verificationService;
  }


  @PostMapping("/sendCode")
  @Operation(summary = "Send Verification Code", description = "Sends a verification code to the specified email address.")
  @ApiResponse(
      responseCode = "200",
      description = "Verification code sent successfully",
      content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class, example = "Verification code sent!"))
  )
  @ApiResponse(
      responseCode = "500",
      description = "Failed to send verification code",
      content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class, example = "Failed to send verification code"))
  )
  public ResponseEntity<?> sendCode(@RequestParam String email) {
    return verificationService.sendVerificationCode(email);
  }

  @PostMapping("/verify")
  @Operation(summary = "Verify Code", description = "Verifies the user's code sent to their email.")
  @ApiResponse(
      responseCode = "200",
      description = "Verification successful",
      content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class, example = "Verification successful!"))
  )
  @ApiResponse(
      responseCode = "400",
      description = "Verification failed or code expired",
      content = @Content(
          mediaType = "text/plain",
          schema = @Schema(
              implementation = String.class,
              example = "Unable to verify. Please make sure you've received the verification code. | Code expired. Please request a new code. | Verification failed. Please make sure the code is correct."
          )
      )
  )
  public ResponseEntity<?> verifyUser(@RequestParam String email, @RequestParam String code) {
    return verificationService.verifyCode(email, code);
  }

  @GetMapping({"/", "/index", "/home"})
  @Operation(summary = "Welcome Page", description = "Provides a welcome message for the Verification API.")
  @ApiResponse(
      responseCode = "200",
      description = "Welcome message displayed",
      content = @Content(
          mediaType = "text/plain",
          schema = @Schema(implementation = String.class, example = "Welcome to verification services!")
      )
  )
  public ResponseEntity<String> index() {
    return ResponseEntity.ok("Welcome to verification services!");
  }
}
