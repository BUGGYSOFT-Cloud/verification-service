package com.buggysoft.verification.interceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.logging.Logger;

@Component
public class LoggingInterceptor implements HandlerInterceptor {
  private static final Logger logger = Logger.getLogger(LoggingInterceptor.class.getName());
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    logger.info("Incoming request: " + request.getMethod() + " " + request.getRequestURI());
    Enumeration<String> headers = request.getHeaderNames();
    while (headers.hasMoreElements()) {
      String header = headers.nextElement();
      logger.info("Header: " + header + " = " + request.getHeader(header));
    }
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    logger.info("Response status: " + response.getStatus());
    logger.info("Completed processing request for " + request.getMethod() + " " + request.getRequestURI());
  }
}