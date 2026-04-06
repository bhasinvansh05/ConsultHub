package com.consultingplatform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import io.jsonwebtoken.Claims;
import com.consultingplatform.user.domain.Client;
import com.consultingplatform.user.domain.Admin;
import com.consultingplatform.user.domain.Consultant;
import com.consultingplatform.user.domain.Role;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil, CustomUserDetailsService userDetailsService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String header = request.getHeader("Authorization");
        String token = null;
        if (header != null && !header.isBlank()) {
            token = jwtTokenUtil.stripPrefix(header);
        }

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = jwtTokenUtil.getAllClaimsFromToken(token);
                String username = claims.getSubject();
                if (username != null) {
                    // Build a lightweight User instance from claims so existing code
                    // that casts principal to CustomUserDetails and calls getId() keeps working
                    Object rawUserId = claims.get("userId");
                    Long userId = null;
                    if (rawUserId instanceof Number) {
                        userId = ((Number) rawUserId).longValue();
                    } else if (rawUserId instanceof String) {
                        try { userId = Long.parseLong((String) rawUserId); } catch (NumberFormatException ignored) {}
                    }

                    // Determine role from claims (first role string if present)
                    Role roleEnum = null;
                    Object rolesObj = claims.get("roles");
                    if (rolesObj instanceof java.util.List) {
                        java.util.List<?> rlist = (java.util.List<?>) rolesObj;
                        if (!rlist.isEmpty() && rlist.get(0) instanceof String) {
                            String roleStr = ((String) rlist.get(0)).replace("ROLE_", "");
                            try { roleEnum = Role.valueOf(roleStr); } catch (Exception ignored) {}
                        }
                    }

                    // Create minimal concrete User subclass instance matching the role
                    com.consultingplatform.user.domain.User tokenUser = null;
                    if (roleEnum == Role.ADMIN) tokenUser = new Admin();
                    else if (roleEnum == Role.CONSULTANT) tokenUser = new Consultant();
                    else tokenUser = new Client();

                    tokenUser.setEmail(username);
                    if (userId != null) tokenUser.setId(userId);
                    if (roleEnum != null) tokenUser.setRole(roleEnum.name());

                    // Wrap into CustomUserDetails and validate expiration
                    com.consultingplatform.security.CustomUserDetails cud = new com.consultingplatform.security.CustomUserDetails(tokenUser);
                    if (!jwtTokenUtil.isTokenExpired(token)) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                cud, null, cud.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (Exception ex) {
                // invalid token or user not found — do nothing and continue filter chain
            }
        }

        filterChain.doFilter(request, response);
    }
}
