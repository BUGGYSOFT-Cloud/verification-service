package com.buggysoft.verification.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import java.util.Date;

public class JwtService {
  private static final String SECRET_KEY = System.getenv("JWT_SECRET_KEY");;
  private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET_KEY);
  private static final long EXPIRATION_TIME = 86400000;


  public static String generateToken(String email) {
    return JWT.create()
        .withSubject(email)
        .withIssuedAt(new Date())
        .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
        .sign(ALGORITHM);
  }

  public static String verifyToken(String token) {
    try {
      JWTVerifier verifier = JWT.require(ALGORITHM).build();
      DecodedJWT jwt = verifier.verify(token);
      return jwt.getSubject();
    } catch (TokenExpiredException e) {
      throw new RuntimeException("Token has expired. Please log in again.");
    } catch (JWTVerificationException e) {
      throw new RuntimeException("Invalid token. Please log in again.");
    }
  }
}