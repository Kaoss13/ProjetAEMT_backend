package com.helha.projetaemt_backend.application.search.query;

import java.util.ArrayList;
import java.util.List;

// Output de la recherche Quick Search
public class SearchOutput {

    // Liste des résultats triés par pertinence
    public List<SearchResultItem> results = new ArrayList<>();

    // Un résultat de recherche
    public static class SearchResultItem {

        // Type: FOLDER, NOTE, NOTE_CONTENT
        public String type;

        // ID de l'élément
        public int id;

        // ID du dossier parent (pour notes)
        public Integer folderId;

        // Titre ou nom
        public String title;

        // Extrait du contenu (pour NOTE_CONTENT)
        public String snippet;

        // Ligne du match (pour NOTE_CONTENT)
        public Integer line;

        // Score de pertinence
        public double score;
    }
}
