package com.example.newsforeveryone.user.config;

import com.example.newsforeveryone.user.config.auth.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class UserIdInjectionFilter extends OncePerRequestFilter {

  private static final String USER_ID_HEADER = "MoNew-Request-User-ID";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication != null && authentication.isAuthenticated()
        && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {

      // 사용자 ID를 요청 헤더에 설정
      request.setAttribute(USER_ID_HEADER, userDetails.getUserId());
    }

    filterChain.doFilter(request, response);
  }
}
