package com.helha.projetaemt_backend.application.search.query;

// Input pour la recherche Quick Search
public class SearchInput {

    // Texte recherché (fuzzy)

    public String query;

    // ID utilisateur
    public int userId;

    // Limite de résultats
    public int limit = 20;
}
