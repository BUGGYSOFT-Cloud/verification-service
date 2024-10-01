package com.buggysoft.verification;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.buggysoft.verification.mapper")
public class VerificationApplication {

  public static void main(String[] args) {
    SpringApplication.run(VerificationApplication.class, args);
  }

}
