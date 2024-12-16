package com.buggysoft.verification.service;

import ch.qos.logback.core.encoder.EchoEncoder;
import com.buggysoft.verification.entity.Verification;
import com.buggysoft.verification.mapper.VerificationCodeMapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.google.auth.oauth2.TokenVerifier;
import com.google.auth.oauth2.TokenVerifier.Builder;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VerificationService {
  @Autowired
  VerificationCodeMapper codeMapper;

  private static final String CLIENT_ID =  System.getenv("GOOGLE_CLIENT_ID");;
  private static final String CLIENT_SECRET = System.getenv("GOOGLE_CLIENT_SECRET");
  private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
  private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";
  private static final String GOOGLE_TOKEN_VALIDATE_URL = "https://oauth2.googleapis.com/tokeninfo?access_token=";

  public ResponseEntity<?> generateAndSaveVerificationCode(String email) {
    int code = generateCode(6);
    try {
      Map<String, Object> verificationMap = new HashMap<>();
      verificationMap.put("email", email);
      List<Verification> codes = codeMapper.selectByMap(verificationMap);
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
      return new ResponseEntity<>(Map.of("message", "Verification code saved!", "code", code), HttpStatus.OK);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>("Failed to save verification code", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public ResponseEntity<?> verifyCode(String email, String code) throws Exception{
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
      try {
        return publishRegisterComplete(verification.getEmail());
      } catch (Exception e) {
        e.printStackTrace();
        throw e;
      }
    } else {
      return new ResponseEntity<>("Verification failed. Please make sure the code is correct.", HttpStatus.BAD_REQUEST);
    }
  }

  public ResponseEntity<?> publishRegisterComplete(String email) throws Exception{
    String url = System.getenv("PUB_SUB_PUBLISH_URL");

    String accessToken;
    try {
      accessToken = getGoogleWorkflowAccessToken();
    } catch (IOException e) {
      e.printStackTrace();
      throw e;
    }
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + accessToken);
    headers.set("Content-Type", "application/json");
    String message = String.format(
        "{\"recipient\": \"%s\", \"subject\": \"Thank you for joining us!\", \"body\": \"Thank you for joining us!\\nHope you can enjoy our service!\\n\"}",
        email
    );

    String encodedMessage = Base64.getEncoder().encodeToString(message.getBytes());
    Map<String, Object> messagePayload = new HashMap<>();
    messagePayload.put("data", encodedMessage);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("messages", new Object[]{messagePayload});
    ObjectMapper objectMapper = new ObjectMapper();
    String jsonRequestBody = objectMapper.writeValueAsString(requestBody);

    HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);
    ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
      return response;
    }
    throw new RuntimeException("Failed to trigger Workflow.");
  }


  public ResponseEntity<?> getGoogleUser(String code) {
    String accessToken = getGoogleAccessToken(code);
    String url = USER_INFO_URL + "?access_token=" + accessToken;
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .GET()
        .build();
    try {
      HttpClient client = HttpClient.newHttpClient();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      ObjectMapper mapper = new ObjectMapper();
      Map<String, Object> userInfo = mapper.readValue(response.body(), Map.class);
      String email = (String) userInfo.get("email");
      return triggerRegister(email, new RestTemplate(), accessToken);
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to get access token", e);
    }
  }

  public ResponseEntity<?> exchangeGoogleToken(String email, String token) {
    if (token == null || token.isEmpty()) {
      throw new IllegalArgumentException("Google token cannot be null or empty");
    }
    try {
      boolean res = validateGoogleToken(token);
      if (res) {
        return new ResponseEntity<>(JwtService.generateToken(email), HttpStatus.OK);
      }
      return new ResponseEntity<>("Invalid google token", HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      return new ResponseEntity<>("Invalid google token", HttpStatus.BAD_REQUEST);
    }
  }

  public boolean validateGoogleToken(String token) throws Exception {
    try {
      String url = GOOGLE_TOKEN_VALIDATE_URL + token;
      HttpHeaders headers = new HttpHeaders();
      HttpEntity<String> requestEntity = new HttpEntity<>(headers);
      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
      if (response.getStatusCode() == HttpStatus.OK) {
        return true;
      }
      return false;

    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  private String getGoogleAccessToken(String code) {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(TOKEN_URL))
        .header("Content-Type", "application/x-www-form-urlencoded")
        .POST(HttpRequest.BodyPublishers.ofString(
            "code=" + code +
                "&client_id=" + CLIENT_ID +
                "&client_secret=" + CLIENT_SECRET +
                "&redirect_uri=http://localhost:8080/auth/callback" +
                "&grant_type=authorization_code"
        ))
        .build();

    try {
      HttpClient client = HttpClient.newHttpClient();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      ObjectMapper mapper = new ObjectMapper();
      Map<String, Object> map = mapper.readValue(response.body(), Map.class);
      return (String) map.get("access_token");
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to get access token", e);
    }
  }

  public static String getGoogleWorkflowAccessToken() throws IOException {
    InputStream serviceAccountStream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream("workflow_key.json");

    if (serviceAccountStream == null) {
      throw new FileNotFoundException("workflow_key.json not found in classpath.");
    }
    GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream)
        .createScoped("https://www.googleapis.com/auth/cloud-platform");
    credentials.refreshIfExpired();
    return credentials.getAccessToken().getTokenValue();
  }

  public ResponseEntity<?> triggerRegister(String email, RestTemplate restTemplate, String token) {
    try {
      String accessToken;
      try {
        accessToken = getGoogleWorkflowAccessToken();
      } catch (IOException e) {
        e.printStackTrace();
        throw e;
      }
      String executionId = triggerWorkflow(email, restTemplate, accessToken, token);
      String callbackUrl = "/register/status?executionId=" + executionId;

      return ResponseEntity
          .accepted()
          .header("Location", callbackUrl)
          .body(Map.of("executionId", executionId, "callbackUrl", callbackUrl));
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>("Failed to trigger workflow: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }


  public String triggerWorkflow (String email, RestTemplate restTemplate, String accessToken, String token) throws Exception{
    String url = System.getenv("REGISTER_WORKFLOW_URL");
    Map<String, String> requestBody = new HashMap<>();

    ObjectMapper objectMapper = new ObjectMapper();
    try {
      String argumentJson = objectMapper.writeValueAsString(Map.of("email", email, "token", token));
      requestBody.put("argument", argumentJson);
    } catch (IOException e) {
      e.printStackTrace();
      throw e;
    }

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + accessToken);
    headers.set("Content-Type", "application/json");
    HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
    ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
      try {
        return (String) objectMapper.readValue(response.getBody(), Map.class).get("name");
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException("Failed to parse response body", e);
      }
    } else {
      throw new RuntimeException("Failed to trigger Workflow.");
    }
  }

  public ResponseEntity<?> getRegisterStatus(String executionId) throws Exception {
    String accessToken;
    try {
      accessToken = getGoogleWorkflowAccessToken();
    } catch (IOException e) {
      e.printStackTrace();
      throw e;
    }
    String url = "https://workflowexecutions.googleapis.com/v1/" + executionId;
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + accessToken);
    HttpEntity<String> requestEntity = new HttpEntity<>(headers);

    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
    Map<String, Object> responseBody = response.getBody();

    if (responseBody != null && responseBody.containsKey("state")) {
      String state = (String) responseBody.get("state");

      if ("SUCCEEDED".equals(state)) {
        Object result = responseBody.get("result");
        return ResponseEntity.ok("Registration successful: " + result);
      } else if ("FAILED".equals(state)) {
        Object error = responseBody.get("error");
        if (error.toString().contains("UserExistsError")) {
          return new ResponseEntity<>("User already exists", HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>("Registration failed: " + error, HttpStatus.INTERNAL_SERVER_ERROR);
      }
      return new ResponseEntity<>("Request is still processing.", HttpStatus.ACCEPTED);
    }
    return new ResponseEntity<>("Unable to retrieve workflow status", HttpStatus.INTERNAL_SERVER_ERROR);
  }

  public ResponseEntity<?> sendRegistrationEmail() {

    return ResponseEntity.ok("Email sent successfully");
  }

  private int generateCode(int size) {
    return (int) ((Math.random()*8 + 1) * Math.pow(10, size - 1));
  }
}
