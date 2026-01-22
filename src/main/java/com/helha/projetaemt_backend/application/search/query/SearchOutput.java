package com.helha.projetaemt_backend.application.search.query;

import java.util.ArrayList;
import java.util.List;

// Output of Quick Search
public class SearchOutput {

    // List of results sorted by relevance
    public List<SearchResultItem> results = new ArrayList<>();

    // A search result item
    public static class SearchResultItem {

        // Type: FOLDER, NOTE, NOTE_CONTENT
        public String type;

        // Element ID
        public int id;

        // Parent folder ID (for notes)
        public Integer folderId;

        // Title or name
        public String title;

        // Content snippet (for NOTE_CONTENT)
        public String snippet;

        // Matching line (for NOTE_CONTENT)
        public Integer line;

        // Relevance score
        public double score;
    }
}
