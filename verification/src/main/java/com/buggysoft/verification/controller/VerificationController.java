package com.buggysoft.verification.controller;

import com.buggysoft.verification.request.GoogleExchangeRequest;
import com.buggysoft.verification.service.VerificationService;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.web.client.RestTemplate;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping
@Tag(name = "Verification API", description = "User verification management operations")
public class VerificationController {
  private final VerificationService verificationService;
  private RestTemplate restTemplate;

  @Autowired
  public VerificationController(VerificationService verificationService) {
    this.verificationService = verificationService;
    restTemplate = new RestTemplate();
  }

  @PostMapping("/generateCodeAndSave")
  @Operation(summary = "Generate Verification Code", description = "Generates a verification code and save it to the database.")
  @ApiResponse(
      responseCode = "200",
      description = "Verification code saved successfully",
      content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Verification code sent!"))
  )
  @ApiResponse(
      responseCode = "500",
      description = "Failed to save verification code",
      content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Failed to send verification code"))
  )
  public ResponseEntity<?> generateCodeAndSave(@RequestParam String email) {
    return verificationService.generateAndSaveVerificationCode(email);
  }

  @PostMapping("/verify")
  @Operation(summary = "Verify Code", description = "Verifies the user's code sent to their email.")
  @ApiResponse(
      responseCode = "200",
      description = "Verification successful",
      content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Verification successful!"))
  )
  @ApiResponse(
      responseCode = "400",
      description = "Verification failed or code expired",
      content = @Content(
          mediaType = "text/plain",
          examples = @ExampleObject(value = "Unable to verify. Please make sure you've received the verification code. | Code expired. Please request a new code. | Verification failed. Please make sure the code is correct.")
      )
  )
  public ResponseEntity<?> verifyUser(@RequestParam String email, @RequestParam String code) {
    try {
      return verificationService.verifyCode(email, code);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>("Failed to verify.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

  }

  @GetMapping("/auth/callback")
  public ResponseEntity<?> callback(@RequestParam String code) {
    return verificationService.getGoogleUser(code);
  }

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestParam String email) {
    return verificationService.triggerRegister(email, restTemplate, "BUGGYSOFT");
  }

  @GetMapping("/register/status")
  public ResponseEntity<?> getRegisterStatus(@RequestParam String executionId) {
    try {
      return verificationService.getRegisterStatus(executionId);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>("Failed to get the access token", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping("/exchangeGoogleToken")
  public ResponseEntity<?> exchangeGoogleToken(@RequestBody GoogleExchangeRequest request) {
    return verificationService.exchangeGoogleToken(request.getEmail(), request.getToken());
  }


  @GetMapping({"/", "/index", "/home"})
  @Operation(summary = "Welcome Page", description = "Provides a welcome message for the Verification API.")
  @ApiResponse(
      responseCode = "200",
      description = "Welcome message displayed",
      content = @Content(
          mediaType = "application/json",
          examples = @ExampleObject(value = "{\"message\": \"Welcome to verification services!\"}")
      )
  )
  public ResponseEntity<?> index() {
    Link sendCodeLink = linkTo(WebMvcLinkBuilder.methodOn(VerificationController.class).generateCodeAndSave(null))
        .withRel("generateCodeAndSave")
        .withType("POST");

    Map<String, Object> response = new HashMap<>();
    response.put("message", "Welcome to verification services!");
    response.put("sendCodeLink", sendCodeLink);

    return ResponseEntity.ok(response);
  }
}
