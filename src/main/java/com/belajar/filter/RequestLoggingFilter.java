package com.belajar.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

// @Component membuat class ini otomatis terdaftar sebagai middleware global
@Component
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // 1. Logika SEBELUM Controller berjalan (Mencatat waktu & URL)
        long startTime = System.currentTimeMillis();
        String method = req.getMethod();
        String uri = req.getRequestURI();

        System.out.println("\n[MIDDLEWARE] ---> " + method + " " + uri);

        // 2. Lanjutkan ke Controller berikutnya (Persis seperti next() di Express atau c.Next() di Go)
        chain.doFilter(request, response);

        // 3. Logika SETELAH Controller selesai (Mencatat status code & durasi eksekusi)
        long duration = System.currentTimeMillis() - startTime;
        int status = res.getStatus();

        System.out.println("[MIDDLEWARE] <--- " + method + " " + uri + " [Status: " + status + "] (" + duration + " ms)\n");
    }
}
