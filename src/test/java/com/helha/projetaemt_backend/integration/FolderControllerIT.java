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
import static org.hamcrest.Matchers.greaterThan;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for:
 * - FolderCommandController + FolderQueryController
 * - NoteCommandController + NoteQueryController
 *
 * Everything in one file as requested.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class FolderAndNoteControllerIT {

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

        // Users
        jdbc.update("INSERT INTO users (id, user_name, hash_password) VALUES (1, 'alice', 'x')");
        jdbc.update("INSERT INTO users (id, user_name, hash_password) VALUES (2, 'bob', 'x')");

        // Root folders (IMPORTANT because your CreateFolderHandler expects a root folder to exist)
        jdbc.update("""
            INSERT INTO folder (id, id_user, id_parent_folder, title, created_at)
            VALUES (10, 1, NULL, 'RootAlice', NOW())
        """);
        jdbc.update("""
            INSERT INTO folder (id, id_user, id_parent_folder, title, created_at)
            VALUES (20, 2, NULL, 'RootBob', NOW())
        """);

        // Alice subfolder we can safely rename/delete etc.
        jdbc.update("""
            INSERT INTO folder (id, id_user, id_parent_folder, title, created_at)
            VALUES (11, 1, 10, 'Projets', NOW())
        """);

        // Existing subfolder to test 409 conflict under same parent
        jdbc.update("""
            INSERT INTO folder (id, id_user, id_parent_folder, title, created_at)
            VALUES (12, 1, 10, 'DejaLa', NOW())
        """);
    }

    // ------------------------
    // FOLDERS - COMMAND
    // ------------------------
    @Nested
    @DisplayName("Folders - Command")
    class FolderCommandTests {

        @Test
        @DisplayName("POST /folders - 201 Création réussie d'un sous-dossier")
        void folder_create_success() throws Exception {
            // parentFolderId = 11 => create inside "Projets"
            String body = """
                {
                  "userId": 1,
                  "idUser": 1,
                  "parentFolderId": 11,
                  "title": "SousProjetA"
                }
            """;

            mockMvc.perform(post("/folders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/folders/")))
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.userId", is(1)))
                    .andExpect(jsonPath("$.title", is("SousProjetA")))
                    .andExpect(jsonPath("$.parentFolderId", is(11)));
        }

        @Test
        @DisplayName("POST /folders - 404 Utilisateur inexistant")
        void folder_create_userNotFound() throws Exception {
            String body = """
                {
                  "userId": 999,
                  "idUser": 999,
                  "parentFolderId": 10,
                  "title": "X"
                }
            """;

            mockMvc.perform(post("/folders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /folders - 400 Parent n'appartient pas à l'utilisateur")
        void folder_create_parentWrongOwner() throws Exception {
            // Bob tries to create into Alice folder 11
            String body = """
                {
                  "userId": 2,
                  "idUser": 2,
                  "parentFolderId": 11,
                  "title": "Tentative"
                }
            """;

            mockMvc.perform(post("/folders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Parent folder does not belong")));
        }

        @Test
        @DisplayName("POST /folders - 409 Dossier existe déjà (même parent, même user, case-insensitive)")
        void folder_create_conflict() throws Exception {
            // We already have folder 12 under parent 10 named "DejaLa"
            String body = """
                {
                  "userId": 1,
                  "idUser": 1,
                  "parentFolderId": 10,
                  "title": "dejala"
                }
            """;

            mockMvc.perform(post("/folders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("PUT /folders - 204 Renommage réussi")
        void folder_update_success() throws Exception {
            // 1. On vérifie d'abord en DB quel est l'ID de Alice pour être sûr
            Integer aliceId = jdbc.queryForObject("SELECT id FROM users WHERE user_name = 'alice'", Integer.class);
            // 2. On récupère l'ID du dossier 'Projets' qui appartient à Alice
            Integer folderId = jdbc.queryForObject("SELECT id FROM folder WHERE title = 'Projets' AND id_user = ?",
                    Integer.class, aliceId);

            // 3. On construit le JSON avec les vrais IDs de la DB
            String body = String.format("""
        {
          "id": %d,
          "userId": %d,
          "title": "NouveauNom"
        }
        """, folderId, aliceId);

            mockMvc.perform(put("/folders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNoContent());

            // Optionnel : vérifier que le nom a bien changé en base
            String updatedTitle = jdbc.queryForObject("SELECT title FROM folder WHERE id = ?", String.class, folderId);
            assertThat(updatedTitle, is("NouveauNom"));
        }


        @Test
        @DisplayName("PUT /folders - 404 Dossier non trouvé")
        void folder_update_notFound() throws Exception {
            String body = """
                {
                  "id": 999,
                  "userId": 1,
                  "idUser": 1,
                  "title": "Rename"
                }
            """;

            mockMvc.perform(put("/folders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT /folders - 400 Le dossier n'appartient pas à l'user")
        void folder_update_wrongOwner() throws Exception {
            // Bob tries rename Alice folder 11
            String body = """
                {
                  "id": 11,
                  "userId": 2,
                  "idUser": 2,
                  "title": "Interdit"
                }
            """;

            mockMvc.perform(put("/folders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("DELETE /folders/{id} - 204 Suppression réussie")
        void folder_delete_success() throws Exception {
            mockMvc.perform(delete("/folders/{id}", 11))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("DELETE /folders/{id} - 404 Dossier inexistant")
        void folder_delete_notFound() throws Exception {
            mockMvc.perform(delete("/folders/{id}", 888))
                    .andExpect(status().isNotFound());
        }
    }

    // ------------------------
    // FOLDERS - QUERY
    // ------------------------
    @Nested
    @DisplayName("Folders - Query")
    class FolderQueryTests {

        @Test
        @DisplayName("GET /folders/all/{userId} - 200 Récupération data Alice")
        void folder_getAll_success() throws Exception {
            // Create a note via API so the test does not depend on notes table columns (nullable/non-nullable)
            String noteBody = """
                {
                  "userId": 1,
                  "idUser": 1,
                  "idFolder": 11,
                  "folderId": 11,
                  "title": "Note Alice",
                  "content": "Contenu"
                }
            """;
            mockMvc.perform(post("/notes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(noteBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists());

            mockMvc.perform(get("/folders/all/{userId}", 1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.folders", notNullValue()))
                    .andExpect(jsonPath("$.notes", notNullValue()))
                    .andExpect(jsonPath("$.folders", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$.notes", hasSize(greaterThanOrEqualTo(1))));
        }

        @Test
        @DisplayName("GET /folders/all/{userId} - 404 User n'existe pas")
        void folder_getAll_userNotFound() throws Exception {
            mockMvc.perform(get("/folders/all/{userId}", 999))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /folders/{id}/export-zip - 200 ZIP généré")
        void folder_exportZip_success() throws Exception {
            mockMvc.perform(get("/folders/{id}/export-zip", 11))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", containsString("folder_11")))
                    .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
            mockMvc.perform(get("/folders/{id}/export-zip", 11))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", containsString("folder_11")))
                    .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                    .andExpect(result -> {
                        byte[] bytes = result.getResponse().getContentAsByteArray();
                        assertThat(bytes.length, greaterThan(0));
                    });
        }

        @Test
        @DisplayName("GET /folders/{id}/export-zip - 404 Folder not found")
        void folder_exportZip_notFound() throws Exception {
            mockMvc.perform(get("/folders/{id}/export-zip", 999))
                    .andExpect(status().isNotFound());
        }
    }

    // ------------------------
    // NOTES - COMMAND + QUERY
    // ------------------------
    @Nested
    @DisplayName("Notes - Command + Query")
    class NoteTests {

        private int createNoteAndReturnId() throws Exception {
            String body = """
                {
                  "userId": 1,
                  "idUser": 1,
                  "idFolder": 11,
                  "folderId": 11,
                  "title": "MaNote",
                  "content": "Hello"
                }
            """;

            String response = mockMvc.perform(post("/notes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            return objectMapper.readTree(response).get("id").asInt();
        }

        @Test
        @DisplayName("POST /notes - 201 Note créée")
        void note_create_success() throws Exception {
            createNoteAndReturnId();
        }

        @Test
        @DisplayName("POST /notes - 404 User ou folder not found")
        void note_create_notFound() throws Exception {
            String body = """
                {
                  "userId": 999,
                  "idUser": 999,
                  "idFolder": 11,
                  "folderId": 11,
                  "title": "X",
                  "content": "Y"
                }
            """;

            mockMvc.perform(post("/notes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT /notes - 204 Note mise à jour")
        void note_update_success() throws Exception {
            int noteId = createNoteAndReturnId();

            String body = """
                {
                  "id": %d,
                  "title": "TitreModifie",
                  "content": "ContenuModifie"
                }
            """.formatted(noteId);

            mockMvc.perform(put("/notes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("PUT /notes - 404 Note not found")
        void note_update_notFound() throws Exception {
            String body = """
                {
                  "id": 99999,
                  "title": "Titre",
                  "content": "Contenu"
                }
            """;

            mockMvc.perform(put("/notes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /notes/{idNote} - 200 Note trouvée")
        void note_getById_success() throws Exception {
            int noteId = createNoteAndReturnId();

            mockMvc.perform(get("/notes/{idNote}", noteId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(noteId)));
        }

        @Test
        @DisplayName("GET /notes/{idNote} - 404 Note not found")
        void note_getById_notFound() throws Exception {
            mockMvc.perform(get("/notes/{idNote}", 99999))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /notes/folders/{idFolder} - 200 Liste des notes du dossier")
        void note_getByFolder_success() throws Exception {
            createNoteAndReturnId();

            mockMvc.perform(get("/notes/folders/{idFolder}", 11))
                    .andExpect(status().isOk());
            // On ne force pas la structure exacte (GetByIdFolderNoteOutput),
            // mais si tu me donnes la classe output je te fais les jsonPath précis.
        }

        @Test
        @DisplayName("GET /notes/folders/{idFolder} - 404 Folder not found")
        void note_getByFolder_notFound() throws Exception {
            mockMvc.perform(get("/notes/folders/{idFolder}", 99999))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /notes/{id}/export-pdf - 200 PDF généré")
        void note_exportPdf_success() throws Exception {
            int noteId = createNoteAndReturnId();

            mockMvc.perform(get("/notes/{id}/export-pdf", noteId))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", containsString("note_" + noteId)))
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF));
        }

        @Test
        @DisplayName("GET /notes/{id}/export-pdf - 404 Note not found")
        void note_exportPdf_notFound() throws Exception {
            mockMvc.perform(get("/notes/{id}/export-pdf", 99999))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE /notes/{id} - 204 Suppression réussie")
        void note_delete_success() throws Exception {
            int noteId = createNoteAndReturnId();

            mockMvc.perform(delete("/notes/{id}", noteId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("DELETE /notes/{id} - 404 Note not found")
        void note_delete_notFound() throws Exception {
            mockMvc.perform(delete("/notes/{id}", 99999))
                    .andExpect(status().isNotFound());
        }
    }
}
