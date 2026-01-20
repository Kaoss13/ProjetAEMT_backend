package com.helha.projetaemt_backend.infrastructure.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// Service pour générer et valider les tokens JWT
@Service
public class JwtService {

    // Clé secrète pour signer les tokens (à mettre dans application.properties)
    @Value("${jwt.secret:monSuperSecretQuiDoitEtreTresLongPourEtreSecurise123456}")
    private String secret;

    // Durée de validité du token (24 heures par défaut)
    @Value("${jwt.expiration:86400000}")
    private long expiration;

    // Génère un token JWT pour un utilisateur
    public String generateToken(Integer userId, String userName) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(userName)                              // Le sujet du token (nom d'utilisateur)
                .claim("userId", userId)                        // Ajout de l'ID utilisateur dans le token
                .issuedAt(new Date())                           // Date de création
                .expiration(new Date(System.currentTimeMillis() + expiration)) // Date d'expiration
                .signWith(key)                                  // Signature avec la clé secrète
                .compact();                                     // Génère le token final
    }

    // Extrait le nom d'utilisateur du token
    public String extractUserName(String token) {
        return extractClaims(token).getSubject();
    }

    // Extrait l'ID utilisateur du token
    public Integer extractUserId(String token) {
        return extractClaims(token).get("userId", Integer.class);
    }

    // Vérifie si le token est valide (non expiré)
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // Vérifie si le token est expiré
    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    // Extrait toutes les claims (données) du token
    private Claims extractClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
