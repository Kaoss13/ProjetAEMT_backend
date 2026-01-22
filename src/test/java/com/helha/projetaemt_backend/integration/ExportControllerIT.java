package com.helha.projetaemt_backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour les fonctionnalités d'export (PDF et ZIP)
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ExportControllerIT {

    @Container
    @ServiceConnection
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void cleanAndPopulate() {
        jdbc.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbc.update("DELETE FROM notes");
        jdbc.update("DELETE FROM folder");
        jdbc.update("DELETE FROM users");
        jdbc.execute("SET FOREIGN_KEY_CHECKS = 1");

        // User
        jdbc.update("INSERT INTO users (id, user_name, hash_password) VALUES (1, 'exportuser', 'x')");

        // Root folder
        jdbc.update("""
            INSERT INTO folder (id, id_user, id_parent_folder, title, created_at)
            VALUES (10, 1, NULL, 'RootExport', NOW())
        """);

        // Subfolder
        jdbc.update("""
            INSERT INTO folder (id, id_user, id_parent_folder, title, created_at)
            VALUES (11, 1, 10, 'Documents', NOW())
        """);

        // Notes
        jdbc.update("""
            INSERT INTO notes (id, id_user, id_folder, title, content, created_at, updated_at)
            VALUES (100, 1, 11, 'Note PDF Test', '<p>Contenu pour PDF</p>', NOW(), NOW())
        """);
        jdbc.update("""
            INSERT INTO notes (id, id_user, id_folder, title, content, created_at, updated_at)
            VALUES (101, 1, 11, 'Note avec HTML', '<h1>Titre</h1><p><strong>Gras</strong></p>', NOW(), NOW())
        """);
    }

    @Nested
    @DisplayName("PDF Export - GET /notes/{id}/export-pdf")
    class PdfExportTests {

        @Test
        @DisplayName("200 - Export PDF réussi")
        void exportPdf_shouldReturn200_withPdfContent() throws Exception {
            mockMvc.perform(get("/notes/{id}/export-pdf", 100))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                    .andExpect(header().string("Content-Disposition", containsString("note_100")))
                    .andExpect(result -> {
                        byte[] pdfBytes = result.getResponse().getContentAsByteArray();
                        assertThat(pdfBytes.length, greaterThan(100));
                        // Vérifier signature PDF
                        assertThat(new String(pdfBytes, 0, 4), is("%PDF"));
                    });
        }

        @Test
        @DisplayName("200 - Export PDF avec contenu HTML complexe")
        void exportPdf_shouldReturn200_withHtmlContent() throws Exception {
            mockMvc.perform(get("/notes/{id}/export-pdf", 101))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                    .andExpect(result -> {
                        byte[] pdfBytes = result.getResponse().getContentAsByteArray();
                        assertThat(pdfBytes.length, greaterThan(100));
                    });
        }

        @Test
        @DisplayName("404 - Note inexistante")
        void exportPdf_shouldReturn404_whenNoteNotFound() throws Exception {
            mockMvc.perform(get("/notes/{id}/export-pdf", 9999))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("200 - Export PDF note avec contenu vide")
        void exportPdf_shouldReturn200_withEmptyContent() throws Exception {
            // Créer une note vide
            jdbc.update("""
                INSERT INTO notes (id, id_user, id_folder, title, content, created_at, updated_at)
                VALUES (102, 1, 11, 'Note vide', '', NOW(), NOW())
            """);

            mockMvc.perform(get("/notes/{id}/export-pdf", 102))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF));
        }
    }

    @Nested
    @DisplayName("ZIP Export - GET /folders/{id}/export-zip")
    class ZipExportTests {

        @Test
        @DisplayName("200 - Export ZIP réussi")
        void exportZip_shouldReturn200_withZipContent() throws Exception {
            mockMvc.perform(get("/folders/{id}/export-zip", 11))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                    .andExpect(header().string("Content-Disposition", containsString("folder_11")))
                    .andExpect(result -> {
                        byte[] zipBytes = result.getResponse().getContentAsByteArray();
                        assertThat(zipBytes.length, greaterThan(50));
                        // Vérifier signature ZIP (PK)
                        assertThat(zipBytes[0], is((byte) 0x50)); // P
                        assertThat(zipBytes[1], is((byte) 0x4B)); // K
                    });
        }

        @Test
        @DisplayName("404 - Dossier inexistant")
        void exportZip_shouldReturn404_whenFolderNotFound() throws Exception {
            mockMvc.perform(get("/folders/{id}/export-zip", 9999))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("200 - Export ZIP dossier vide")
        void exportZip_shouldReturn200_withEmptyFolder() throws Exception {
            // Créer un dossier vide
            jdbc.update("""
                INSERT INTO folder (id, id_user, id_parent_folder, title, created_at)
                VALUES (12, 1, 10, 'DossierVide', NOW())
            """);

            mockMvc.perform(get("/folders/{id}/export-zip", 12))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
        }

        @Test
        @DisplayName("200 - Export ZIP root folder")
        void exportZip_shouldReturn200_forRootFolder() throws Exception {
            mockMvc.perform(get("/folders/{id}/export-zip", 10))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
        }
    }
}
