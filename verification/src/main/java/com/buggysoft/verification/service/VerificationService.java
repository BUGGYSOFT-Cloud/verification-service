package com.buggysoft.verification.service;

import com.buggysoft.verification.entity.Verification;
import com.buggysoft.verification.mapper.VerificationCodeMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VerificationService {
  @Autowired
  VerificationCodeMapper codeMapper;
  @Autowired
  private JavaMailSender mailSender;

  public ResponseEntity<?> sendVerificationCode(String email) {
    int code = generateCode(6);
    try {
      Map<String, Object> verificationMap = new HashMap<>();
      verificationMap.put("email", email);
      List<Verification> codes = codeMapper.selectByMap(verificationMap);
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(email);
      message.setSubject("BuggySoft Job Tracker Verification Code");
      message.setText("Greetings Esteemed User!\n" +
          "Your verification code is: " + code +
          "\n This code will expire in 10 minutes. \n All the best, \n BuggySoft Team");
      mailSender.send(message);
      if (codes.isEmpty()) {
        Verification verification = new Verification();
        verification.setEmail(email);
        verification.setCode(String.valueOf(code));
        verification.setCreatedAt(LocalDateTime.now());
        codeMapper.insert(verification);
      } else {
        Verification existingVerification = codes.get(0);
        existingVerification.setCode(String.valueOf(code));
        existingVerification.setCreatedAt(LocalDateTime.now());
        codeMapper.updateById(existingVerification);
      }
      return new ResponseEntity<>("Verification code sent!", HttpStatus.OK);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>("Failed to send verification code", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public ResponseEntity<?> verifyCode(String email, String code) {
    Map<String, Object> verificationMap = new HashMap<>();
    verificationMap.put("email", email);
    List<Verification> codes = codeMapper.selectByMap(verificationMap);
    if (codes.isEmpty()) {
      return new ResponseEntity<>("Unable to verify. Please make sure you've received the verification code.", HttpStatus.BAD_REQUEST);
    }
    Verification verification = codes.get(0);
    if ((Duration.between(verification.getCreatedAt(), LocalDateTime.now())).toMinutes() > 10) {
      codeMapper.deleteById(verification.getEmail());
      return new ResponseEntity<>("Code expired. Please request a new code.", HttpStatus.BAD_REQUEST);
    }
    String target = verification.getCode();
    if (target.equals(code)) {
      codeMapper.deleteById(verification.getEmail());
      return new ResponseEntity<>("Verification successful!", HttpStatus.OK);
    } else {
      return new ResponseEntity<>("Verification failed. Please make sure the code is correct.", HttpStatus.BAD_REQUEST);
    }
  }

  private int generateCode(int size) {
    return (int) ((Math.random()*8 + 1) * Math.pow(10, size - 1));
  }
}
