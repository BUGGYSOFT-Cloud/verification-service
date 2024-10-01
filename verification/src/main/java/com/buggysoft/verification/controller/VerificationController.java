package com.buggysoft.verification.controller;

import com.buggysoft.verification.service.VerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VerificationController {
  private final VerificationService verificationService;

  @Autowired
  public VerificationController(VerificationService verificationService) {
    this.verificationService = verificationService;
  }

  @PostMapping("/sendCode")
  public ResponseEntity<?> sendCode(@RequestParam String email) {
    return verificationService.sendVerificationCode(email);
  }

  @PostMapping("/verify")
  public ResponseEntity<?> verifyUser(@RequestParam String email, @RequestParam String code) {
    return verificationService.verifyCode(email, code);
  }

  @GetMapping({"/", "/index", "/home"})
  public String index() {
    return "Welcome to verification services!";
  }
}
