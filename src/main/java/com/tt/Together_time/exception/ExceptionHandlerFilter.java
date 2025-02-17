package com.tt.Together_time.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//필터 기반 예외 처리 - Spring Security 필터 체인에 포함되어, 컨트롤러에 도달하기 전에 발생한 예외를 처리
//글로벌 예외 처리 클래스는 컨트롤러 내부에서 발생한 예외 처리

@Component
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 필터 체인의 다음 단계로 요청 전달
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException ex) {
            setErrorResponse(HttpServletResponse.SC_UNAUTHORIZED, response, "Expired Token");
        } catch (JwtException | IllegalArgumentException ex) {
            setErrorResponse(HttpServletResponse.SC_UNAUTHORIZED, response, "Invalid Token");
        } catch (AccessDeniedException ex) {
            setErrorResponse(HttpServletResponse.SC_FORBIDDEN, response, "Access Denied");
        } catch (Exception ex) {
            setErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response, "Internal Server Error");
        }
    }

    private void setErrorResponse(int status, HttpServletResponse response, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}