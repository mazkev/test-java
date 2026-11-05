package com.belajar.security;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Fase 18: Centralized Logging & Correlation ID.
 * Menjamin setiap HTTP request memiliki Correlation ID unik yang disuntikkan ke Logback MDC,
 * sehingga semua log di seluruh thread memiliki penanda identitas transaksi yang sama.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String MDC_CORRELATION_KEY = "correlationId";

    private final ObjectProvider<Tracer> tracerProvider;

    public CorrelationIdFilter(ObjectProvider<Tracer> tracerProvider) {
        this.tracerProvider = tracerProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Ambil Correlation ID dari client atau generate UUID baru jika tidak ada
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        // 2. Simpan ke MDC agar otomatis tercetak di setiap baris log sistem
        MDC.put(MDC_CORRELATION_KEY, correlationId);

        // 3. Pasang Correlation ID di header respon kembali ke client
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        // 4. Jika ada tracer aktif (Fase 16: Micrometer Tracing), pasang juga Trace ID di header response
        Tracer tracer = tracerProvider.getIfAvailable();
        if (tracer != null && tracer.currentSpan() != null) {
            String traceId = tracer.currentSpan().context().traceId();
            response.setHeader(TRACE_ID_HEADER, traceId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Bersihkan MDC agar tidak bocor ke request berikutnya di thread pool
            MDC.remove(MDC_CORRELATION_KEY);
        }
    }
}
