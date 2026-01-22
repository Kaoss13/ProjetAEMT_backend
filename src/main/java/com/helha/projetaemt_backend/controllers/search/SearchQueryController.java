package com.helha.projetaemt_backend.controllers.search;

import com.helha.projetaemt_backend.application.search.query.SearchInput;
import com.helha.projetaemt_backend.application.search.query.SearchOutput;
import com.helha.projetaemt_backend.application.search.query.SearchQueryProcessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Controller pour la recherche Quick Search
@RestController
@RequestMapping("/search")
@Tag(name = "Search", description = "Recherche rapide")
public class SearchQueryController {

    private final SearchQueryProcessor searchQueryProcessor;

    public SearchQueryController(SearchQueryProcessor searchQueryProcessor) {
        this.searchQueryProcessor = searchQueryProcessor;
    }

    @Operation(
            summary = "Recherche fuzzy",
            description = """
            Recherche rapide (fuzzy) dans les dossiers et les notes d’un utilisateur.

            Comportement :
            - La recherche est insensible à la casse
            - Une query vide retourne une liste vide
            - Les résultats sont triés par pertinence décroissante
            - Le nombre de résultats est limité par le paramètre `limit`

            Exemple :
            - 'chn' peut trouver 'chien'
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Résultats de la recherche (liste possiblement vide)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SearchOutput.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    // GET /search?q=texte&userId=1&limit=20
    @GetMapping
    public ResponseEntity<SearchOutput> search(
            @RequestParam("q") String query,
            @RequestParam int userId,
            @RequestParam(defaultValue = "20") int limit
    ) {
        SearchInput input = new SearchInput();
        input.query = query;
        input.userId = userId;
        input.limit = limit;

        return ResponseEntity.ok(searchQueryProcessor.searchHandler.handle(input));
    }
}
