package com.helha.projetaemt_backend.infrastructure.utils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

// Filtre qui intercepte chaque requête pour vérifier le token JWT
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Récupère le header Authorization
        String authHeader = request.getHeader("Authorization");

        // Si pas de header ou ne commence pas par "Bearer ", on passe au filtre suivant
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extrait le token (enlève "Bearer ")
        String token = authHeader.substring(7);

        // Vérifie si le token est valide
        if (jwtService.isTokenValid(token)) {
            // Extrait les infos de l'utilisateur
            String userName = jwtService.extractUserName(token);
            Integer userId = jwtService.extractUserId(token);

            // Crée un objet d'authentification Spring Security
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userName, userId, Collections.emptyList());

            // Met l'authentification dans le contexte Spring Security
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // Passe au filtre suivant
        filterChain.doFilter(request, response);
    }
}
