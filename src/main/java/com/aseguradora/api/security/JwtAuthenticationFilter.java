package com.aseguradora.api.security;

import com.aseguradora.api.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                logger.error("Error al extraer usuario del token");
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Cargamos los datos frescos de la BD (incluyendo si está activo o no)
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Validamos el token Y TAMBIÉN si la cuenta está habilitada (isEnabled)
            if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                
                // --- NUEVA COMPROBACIÓN DE SEGURIDAD ---
                if (!userDetails.isEnabled()) {
                    // Si el usuario existe y el token es válido, PERO está inactivo en BD:
                    // No le autenticamos. Spring devolverá 403 y tu Frontend lo expulsará.
                    System.out.println("Usuario " + username + " está inactivo. Bloqueando acceso.");
                } else {
                    // Si está activo, adelante
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }
        chain.doFilter(request, response);
    }
}