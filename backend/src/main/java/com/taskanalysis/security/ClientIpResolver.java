package com.taskanalysis.security;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Determines the client IP address of a request
 * Uses X-Real-IP (set by Nginx to $remote_addr), falling back to the connection address.
 * X-Forwarded-For is ignored because its first entry is client-controlled and spoofable.
 */
public final class ClientIpResolver {

    private ClientIpResolver() {
    }

    public static String resolve(HttpServletRequest request) {
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}