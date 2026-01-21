package com.helha.projetaemt_backend.unitaire;
import com.helha.projetaemt_backend.domain.note.Note;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class NoteTest {

    @Test
    void testGetSizeBytes() {
        Note note = Note.builder()
                .content("Hello World")
                .build();

        int expectedSize = "Hello World".getBytes(StandardCharsets.UTF_8).length;
        assertEquals(expectedSize, note.computeSizeBytes());
    }

    @Test
    void testGetLineCountWithMultipleLines() {
        Note note = Note.builder()
                .content("Line1\nLine2\nLine3")
                .build();

        assertEquals(3, note.computeLineCount());
    }

    @Test
    void testGetLineCountWithSingleLine() {
        Note note = Note.builder()
                .content("Single line")
                .build();

        assertEquals(1, note.computeLineCount());
    }

    @Test
    void testGetWordCountNormalCase() {
        Note note = Note.builder()
                .content("Hello world from Spring Boot")
                .build();

        assertEquals(5, note.computeWordCount());
    }

    @Test
    void testGetWordCountWithExtraSpaces() {
        Note note = Note.builder()
                .content("   Hello   world   ")
                .build();

        assertEquals(2, note.computeWordCount());
    }

    @Test
    void testGetWordCountEmptyContent() {
        Note note = Note.builder()
                .content("")
                .build();

        assertEquals(0, note.computeWordCount());
    }

    @Test
    void testGetCharCount() {
        Note note = Note.builder()
                .content("Hello")
                .build();

        assertEquals(5, note.computeCharCount());
    }

    @Test
    void testGetCharCountEmptyContent() {
        Note note = Note.builder()
                .content("")
                .build();

        assertEquals(0, note.computeCharCount());
    }
}

