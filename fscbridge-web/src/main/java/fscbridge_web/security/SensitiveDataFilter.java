package fscbridge_web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Order(1)
public class SensitiveDataFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain)
            throws ServletException, IOException {

        log.debug("Incoming request: {} {}",
                request.getMethod(),
                request.getRequestURI());

        addSecurityHeaders(response);

        filterChain.doFilter(request, response);

        log.debug("Response status: {} for {} {}",
                response.getStatus(),
                request.getMethod(),
                request.getRequestURI());
    }

    private void addSecurityHeaders(HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Cache-Control",
                "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Server", "FSC-Bridge");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("Content-Security-Policy",
                "default-src 'self'; frame-ancestors 'none'");
    }
}
