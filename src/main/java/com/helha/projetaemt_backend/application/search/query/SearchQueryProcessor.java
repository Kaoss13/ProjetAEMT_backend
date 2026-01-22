package com.helha.projetaemt_backend.application.search.query;

import org.springframework.stereotype.Service;

@Service
public class SearchQueryProcessor {

    public final SearchHandler searchHandler;

    public SearchQueryProcessor(SearchHandler searchHandler) {
        this.searchHandler = searchHandler;
    }
}
