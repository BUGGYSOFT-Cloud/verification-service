package com.buggysoft.verification.controller;

import com.buggysoft.verification.service.JwtService;
import com.buggysoft.verification.service.VerificationService;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

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
      content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Verification code sent!"))
  )
  @ApiResponse(
      responseCode = "500",
      description = "Failed to send verification code",
      content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Failed to send verification code"))
  )
  public ResponseEntity<?> sendCode(@RequestParam String email) {
    return verificationService.sendVerificationCode(email);
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
    return verificationService.verifyCode(email, code);
  }

  @PostMapping("/verifyToken")
  @Operation(
      summary = "Verify Token",
      description = "Verifies the user's token sent as a parameter.",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Verification successful",
              content = @Content(
                  mediaType = "text/plain",
                  examples = @ExampleObject(value = "Verification successful!")
              )
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Verification failed or token expired",
              content = @Content(
                  mediaType = "text/plain",
                  examples = @ExampleObject(
                      value = "Unable to verify. Please provide a valid token. | Token expired. Please request a new token. | Verification failed. Invalid token."
                  )
              )
          )
      }
  )
  public ResponseEntity<String> verifyToken(@RequestParam String token) {
    try {
      String email = JwtService.verifyToken(token);
      return ResponseEntity.ok(email);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }


  @GetMapping({"/", "/index", "/home"})
  @Operation(summary = "Welcome Page", description = "Provides a welcome message for the Verification API.")
  @ApiResponse(
      responseCode = "200",
      description = "Welcome message displayed",
      content = @Content(
          mediaType = "application/json",
          examples = @ExampleObject(value = "{\"message\": \"Welcome to user services!\"}")
      )
  )
  public ResponseEntity<?> index() {
    Link sendCodeLink = linkTo(WebMvcLinkBuilder.methodOn(VerificationController.class).sendCode(null))
        .withRel("sendCode")
        .withType("POST");

    Map<String, Object> response = new HashMap<>();
    response.put("message", "Welcome to verification services!");
    response.put("sendCodeLink", sendCodeLink);

    return ResponseEntity.ok(response);
  }
}
