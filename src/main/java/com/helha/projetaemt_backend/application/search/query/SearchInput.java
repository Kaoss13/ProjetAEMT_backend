package com.helha.projetaemt_backend.application.search.query;

// Input for Quick Search
public class SearchInput {

    // Searched text (fuzzy)
    public String query;

    // User ID
    public int userId;

    // Result limit
    public int limit = 20;
}
