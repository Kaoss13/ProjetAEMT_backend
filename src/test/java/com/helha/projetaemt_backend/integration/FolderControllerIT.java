package com.helha.projetaemt_backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helha.projetaemt_backend.application.folder.command.create.CreateFolderInput;
import com.helha.projetaemt_backend.application.folder.command.update.UpdateFolderInput;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class FolderControllerIT {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void cleanAndPopulate() {
        // 1. Désactiver les contraintes de clés étrangères
        jdbc.execute("SET FOREIGN_KEY_CHECKS = 0");

        // 2. Nettoyer les tables (l'ordre n'importe plus ici)
        jdbc.update("DELETE FROM notes");
        jdbc.update("DELETE FROM folder");
        jdbc.update("DELETE FROM users");

        // 3. Réactiver les contraintes
        jdbc.execute("SET FOREIGN_KEY_CHECKS = 1");

        // 4. Recréer les données de base pour les tests
        jdbc.update("INSERT INTO users (id, user_name, hash_password) VALUES (1, 'alice', 'x')");
        jdbc.update("INSERT INTO users (id, user_name, hash_password) VALUES (2, 'bob', 'x')");

        // Dossier racine pour Alice (ID 10)
        jdbc.update("""
    INSERT INTO folder (id, id_user, id_parent_folder, title, created_at)
    VALUES (10, 1, NULL, 'Projets', NOW())
""");

    }

    // --- TESTS COMMAND: CREATE ---
    @Nested
    @DisplayName("POST /folders")
    class CreateFolderTests {

        @Test
        @DisplayName("201 - Création réussie d'un sous-dossier")
        void create_Success() throws Exception {
            CreateFolderInput input = new CreateFolderInput();
            input.userId = 1;
            input.parentFolderId = 10;
            input.title = "Sous-Projet A";

            mockMvc.perform(post("/folders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists());
        }

        @Test
        @DisplayName("404 - Utilisateur inexistant")
        void create_UserNotFound() throws Exception {
            CreateFolderInput input = new CreateFolderInput();
            input.userId = 999;
            input.title = "Inconnu";

            mockMvc.perform(post("/folders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("400 - Parent n'appartient pas à l'utilisateur")
        void create_ParentWrongOwner() throws Exception {
            CreateFolderInput input = new CreateFolderInput();
            input.userId = 2; // Bob essaie d'écrire dans le dossier d'Alice (10)
            input.parentFolderId = 10;
            input.title = "Tentative";

            mockMvc.perform(post("/folders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("409 - Dossier existe déjà")
        void create_Conflict() throws Exception {
            CreateFolderInput input = new CreateFolderInput();
            input.userId = 1;
            input.title = "Projets"; // Titre déjà utilisé par le dossier 10
            input.parentFolderId = null;

            mockMvc.perform(post("/folders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isConflict());
        }
    }

    // --- TESTS COMMAND: UPDATE ---
    @Nested
    @DisplayName("PUT /folders")
    class UpdateFolderTests {

        @Test
        @DisplayName("204 - Renommage réussi")
        void update_Success() throws Exception {
            UpdateFolderInput input = new UpdateFolderInput();
            input.id = 10;
            input.userId = 1;
            input.title = "NouveauNom";

            mockMvc.perform(put("/folders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("404 - Dossier non trouvé")
        void update_NotFound() throws Exception {
            UpdateFolderInput input = new UpdateFolderInput();
            input.id = 999;
            input.userId = 1;
            input.title = "Rename";

            mockMvc.perform(put("/folders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("400 - Le dossier n'appartient pas à l'user")
        void update_WrongOwner() throws Exception {
            UpdateFolderInput input = new UpdateFolderInput();
            input.id = 10; // Folder d'Alice
            input.userId = 2; // Bob essaie de modifier
            input.title = "Interdit";

            mockMvc.perform(put("/folders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isBadRequest());
        }
    }

    // --- TESTS COMMAND: DELETE ---
    @Nested
    @DisplayName("DELETE /folders/{id}")
    class DeleteFolderTests {

        @Test
        @DisplayName("204 - Suppression réussie")
        void delete_Success() throws Exception {
            mockMvc.perform(delete("/folders/{id}", 10))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("404 - Dossier inexistant")
        void delete_NotFound() throws Exception {
            mockMvc.perform(delete("/folders/{id}", 888))
                    .andExpect(status().isNotFound());
        }
    }

    // --- TESTS QUERY ---
    @Nested
    @DisplayName("GET /folders/all/{userId}")
    class QueryFolderTests {

        @Test
        @DisplayName("200 - Récupération data Alice")
        void getAll_Success() throws Exception {
            // On insère TOUTES les colonnes nécessaires pour éviter le NullPointerException d'Hibernate
            jdbc.update("""
        INSERT INTO notes (
            id_user, id_folder, title, content, 
            created_at, updated_at
        ) VALUES (?, ?, ?, ?, NOW(), NOW())
        """,
                    1, 10, "Note Alice", "Contenu de test"
            );

            mockMvc.perform(get("/folders/all/{userId}", 1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.folders", hasSize(1)))
                    .andExpect(jsonPath("$.notes", hasSize(1)))
                    .andExpect(jsonPath("$.notes[0].title").value("Note Alice"));
        }

        @Test
        @DisplayName("404 - User n'existe pas")
        void getAll_UserNotFound() throws Exception {
            mockMvc.perform(get("/folders/all/{userId}", 999))
                    .andExpect(status().isNotFound());
        }
    }
}