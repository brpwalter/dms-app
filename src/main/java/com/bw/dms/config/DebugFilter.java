package com.bw.dms.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1) // Ganz am Anfang der Kette
public class DebugFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        System.out.println(">>> DEBUG FILTER EMPFANGEN: " + req.getMethod() + " " + req.getRequestURI());
        chain.doFilter(request, response);
    }
}
