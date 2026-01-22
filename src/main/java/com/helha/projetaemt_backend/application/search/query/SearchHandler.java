package com.helha.projetaemt_backend.application.search.query;

import com.helha.projetaemt_backend.application.utils.IQueryHandler;
import com.helha.projetaemt_backend.infrastructure.dossier.DbFolder;
import com.helha.projetaemt_backend.infrastructure.dossier.IFolderRepository;
import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
import com.helha.projetaemt_backend.infrastructure.user.IUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SearchHandler implements IQueryHandler<SearchInput, SearchOutput> {

    private final IUserRepository userRepository;
    private final INoteRepository noteRepository;
    private final IFolderRepository folderRepository;

    public SearchHandler(IUserRepository userRepository,
                         INoteRepository noteRepository, IFolderRepository folderRepository) {
        this.userRepository = userRepository;
        this.noteRepository = noteRepository;
        this.folderRepository = folderRepository;
    }

    @Override
    public SearchOutput handle(SearchInput input) {
        if (!userRepository.existsById(input.userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "L'utilisateur introuvable"
            );
        }

        SearchOutput output = new SearchOutput();

        if (input.query == null || input.query.trim().isEmpty()) {
            return output;
        }

        String q = input.query.toLowerCase();
        List<SearchOutput.SearchResultItem> results = new ArrayList<>();

        for (DbFolder folder : folderRepository.findAllByUser_Id(input.userId)) {
            double score = fuzzyScore(q, folder.title.toLowerCase());
            if (score > 0) {
                SearchOutput.SearchResultItem item = new SearchOutput.SearchResultItem();
                item.type = "FOLDER";
                item.id = folder.id;
                item.title = folder.title;
                item.score = score;
                results.add(item);
            }
        }

        for (DbNote note : noteRepository.findAllByUser_Id(input.userId)) {
            double titleScore = fuzzyScore(q, note.title != null ? note.title.toLowerCase() : "");
            if (titleScore > 0) {
                SearchOutput.SearchResultItem item = new SearchOutput.SearchResultItem();
                item.type = "NOTE";
                item.id = note.id;
                item.folderId = note.folder != null ? note.folder.id : null;
                item.title = note.title;
                item.score = titleScore * 1.5;
                results.add(item);
            }

            if (note.content != null) {
                String content = note.content.toLowerCase();
                double contentScore = fuzzyScore(q, content);
                if (contentScore > 0) {
                    SearchOutput.SearchResultItem item = new SearchOutput.SearchResultItem();
                    item.type = "NOTE_CONTENT";
                    item.id = note.id;
                    item.folderId = note.folder != null ? note.folder.id : null;
                    item.title = note.title;
                    item.snippet = extractSnippet(note.content, q);
                    item.line = getLineNumber(note.content, content.indexOf(q.charAt(0)));
                    item.score = contentScore;
                    results.add(item);
                }
            }
        }

        results.sort((a, b) -> Double.compare(b.score, a.score));
        output.results = results.subList(0, Math.min(input.limit, results.size()));

        return output;
    }

    private double fuzzyScore(String query, String text) {
        if (text.contains(query)) {
            return 1.0;
        }

        int textIndex = 0;
        for (char c : query.toCharArray()) {
            textIndex = text.indexOf(c, textIndex);
            if (textIndex < 0) {
                return 0;
            }
            textIndex++;
        }

        return 0.5;
    }

    private String extractSnippet(String text, String query) {
        int idx = text.toLowerCase().indexOf(query.charAt(0));
        if (idx < 0) idx = 0;

        int start = Math.max(0, idx - 20);
        int end = Math.min(text.length(), idx + 30);

        String snippet = text.substring(start, end);
        if (start > 0) snippet = "..." + snippet;
        if (end < text.length()) snippet = snippet + "...";

        return snippet;
    }

    private int getLineNumber(String text, int position) {
        int line = 1;
        for (int i = 0; i < Math.min(position, text.length()); i++) {
            if (text.charAt(i) == '\n') line++;
        }
        return line;
    }
}
