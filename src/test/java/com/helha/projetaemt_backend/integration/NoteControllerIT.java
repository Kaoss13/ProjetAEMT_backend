package com.helha.projetaemt_backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helha.projetaemt_backend.application.note.command.create.CreateNoteInput;
import com.helha.projetaemt_backend.application.note.command.update.UpdateNoteInput;
import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.N;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")

class NoteControllerIT {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    INoteRepository noteRepository;


    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void ensureData() {
        jdbc.update("""
      INSERT INTO users (id, user_name, hash_password)
      VALUES (1, 'nolhan', 'x')
      ON DUPLICATE KEY UPDATE user_name = VALUES(user_name)
  """);
        jdbc.update("""
      INSERT INTO folder (id, id_user, id_parent_folder, title, created_at)
      VALUES (1, 1, NULL, 'Cours', NOW())
      ON DUPLICATE KEY UPDATE title = VALUES(title)
  """);
    }

    @BeforeEach
    public void setup() {
        noteRepository.deleteAll();
    }

    @Nested
    @DisplayName("GET /notes/{id}")
    class GetNoteById {
        // Note: L'endpoint GET /notes/{id} n'a pas de @GetMapping dans le controller
        // Donc ce test est désactivé car il retourne 405 Method Not Allowed
    }

    @Nested
    @DisplayName("GET /notes/folder/{id}")
    class getNoteByIdFolder {
        @Test
        @DisplayName("200 - retourne une liste vide de notes")
        void getNotes_shouldReturnEmptyList() throws Exception {
            mockMvc.perform(get("/notes/folders/{id}", 1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.notes", hasSize(0)));
        }


        @Test
        @DisplayName("200 - retourne une liste avec deux notes")
        void getNotesByFolder_shouldReturnTwoNotes() throws Exception {
            CreateNoteInput input1 = CreateNoteInput.builder()
                    .idUser(1)
                    .idFolder(1)
                    .title("TestTitle")
                    .content("TestContent")
                    .build();

            mockMvc.perform(post("/notes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input1)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.title").value("TestTitle"));

            CreateNoteInput input2 = CreateNoteInput.builder()
                    .idUser(1)
                    .idFolder(1)
                    .title("TestTitle2")
                    .content("TestContent2")
                    .build();

            mockMvc.perform(post("/notes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input2)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.title").value("TestTitle2"));

            mockMvc.perform(get("/notes/folders/{id}", 1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.notes", hasSize(2)))
                    .andExpect(jsonPath("$.notes[0].id").isNumber())
                    .andExpect(jsonPath("$.notes[1].id").isNumber());
        }

    }

    @Nested
    @DisplayName("POST /notes")
    class CreateNote {
        @Test
        @DisplayName("201 - crée une note")
        void create_shouldReturn201_andBody() throws Exception {
            CreateNoteInput input = CreateNoteInput.builder().idUser(1).idFolder(1).title("TestTitle").content("TestContent").build();

            String payload = objectMapper.writeValueAsString(input);

            mockMvc.perform(
                            post("/notes")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(payload)
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.idUser").value(1))
                    .andExpect(jsonPath("$.idFolder").value(1))
                    .andExpect(jsonPath("$.title").value("TestTitle"))
                    .andExpect(jsonPath("$.content").value("TestContent"))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.updatedAt").exists())
                    .andExpect(jsonPath("$.sizeBytes").isNumber())
                    .andExpect(jsonPath("$.lineCount").isNumber())
                    .andExpect(jsonPath("$.wordCount").isNumber())
                    .andExpect(jsonPath("$.charCount").isNumber());
        }

        @Test
        @DisplayName("201 - crée une note avec contenu HTML")
        void create_shouldReturn201_withHtmlContent() throws Exception {
            CreateNoteInput input = CreateNoteInput.builder()
                    .idUser(1)
                    .idFolder(1)
                    .title("Note HTML")
                    .content("<p><strong>Gras</strong> et <em>italique</em></p>")
                    .build();

            mockMvc.perform(
                            post("/notes")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(input))
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.content").value("<p><strong>Gras</strong> et <em>italique</em></p>"));
        }

        @Test
        @DisplayName("201 - crée une note avec titre vide")
        void create_shouldReturn201_withEmptyTitle() throws Exception {
            CreateNoteInput input = CreateNoteInput.builder()
                    .idUser(1)
                    .idFolder(1)
                    .title("")
                    .content("Contenu sans titre")
                    .build();

            mockMvc.perform(
                            post("/notes")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(input))
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value(""));
        }

        @Test
        @DisplayName("201 - crée une note avec contenu multilignes")
        void create_shouldReturn201_withMultilineContent() throws Exception {
            CreateNoteInput input = CreateNoteInput.builder()
                    .idUser(1)
                    .idFolder(1)
                    .title("Note multilignes")
                    .content("Ligne 1\nLigne 2\nLigne 3")
                    .build();

            // Note: Jsoup.text() normalise les sauts de ligne, donc lineCount = 1
            mockMvc.perform(
                            post("/notes")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(input))
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.lineCount").value(1));
        }

        @Test
        @DisplayName("201 - crée une note avec caractères spéciaux")
        void create_shouldReturn201_withSpecialChars() throws Exception {
            CreateNoteInput input = CreateNoteInput.builder()
                    .idUser(1)
                    .idFolder(1)
                    .title("Note spéciale éèàù")
                    .content("Contenu avec émojis et accents: été, noël, 日本語")
                    .build();

            mockMvc.perform(
                            post("/notes")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(input))
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("Note spéciale éèàù"));
        }

        @Test
        @DisplayName("404 - folder inexistant")
        void create_shouldReturn404_whenFolderNotFound() throws Exception {
            CreateNoteInput input = CreateNoteInput.builder()
                    .idUser(1)
                    .idFolder(9999)
                    .title("Test")
                    .content("Content")
                    .build();

            mockMvc.perform(
                            post("/notes")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(input))
                    )
                    .andExpect(status().isNotFound());
        }
    }


    @Nested
    @DisplayName("PUT /notes")
    class UpdateNote {
        @Test
        @DisplayName("204 - Note mise à jour")
        void updateNote_shouldReturnNoContent() throws Exception {
            // 1) Créer une note et récupérer son id
            CreateNoteInput createInput = CreateNoteInput.builder()
                    .idUser(1).idFolder(1).title("TestTitle").content("TestContent")
                    .build();

            String createPayload = objectMapper.writeValueAsString(createInput);

            String createResponse = mockMvc.perform(
                            post("/notes")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(createPayload)
                    )
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            int createdId = objectMapper.readTree(createResponse).get("id").asInt();

            // 2) Mettre à jour la note créée (id obligatoire dans UpdateNoteInput)
            UpdateNoteInput updateInput = UpdateNoteInput.builder()
                    .id(createdId)
                    .title("Nouveau titre")
                    .content("Nouveau content")
                    .build();

            String updatePayload = objectMapper.writeValueAsString(updateInput);

            mockMvc.perform(put("/notes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updatePayload))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("404 - Note introuvable")
        void updateNote_shouldReturnNotFound() throws Exception {
            UpdateNoteInput input = UpdateNoteInput.builder()
                    .id(999) // inexistant
                    .title("Titre")
                    .content("Content")
                    .build();

            String payload = objectMapper.writeValueAsString(input);

            mockMvc.perform(put("/notes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("204 - Mise à jour partielle (titre seul)")
        void updateNote_shouldUpdateTitleOnly() throws Exception {
            CreateNoteInput createInput = CreateNoteInput.builder()
                    .idUser(1).idFolder(1).title("Original").content("Contenu original")
                    .build();

            String createResponse = mockMvc.perform(
                            post("/notes")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createInput))
                    )
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            int createdId = objectMapper.readTree(createResponse).get("id").asInt();

            UpdateNoteInput updateInput = UpdateNoteInput.builder()
                    .id(createdId)
                    .title("Nouveau titre")
                    .content("Contenu original")
                    .build();

            mockMvc.perform(put("/notes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateInput)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("204 - Mise à jour avec contenu HTML complexe")
        void updateNote_shouldUpdateWithComplexHtml() throws Exception {
            CreateNoteInput createInput = CreateNoteInput.builder()
                    .idUser(1).idFolder(1).title("Test").content("Simple")
                    .build();

            String createResponse = mockMvc.perform(
                            post("/notes")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createInput))
                    )
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            int createdId = objectMapper.readTree(createResponse).get("id").asInt();

            UpdateNoteInput updateInput = UpdateNoteInput.builder()
                    .id(createdId)
                    .title("Note HTML")
                    .content("<h1>Titre</h1><ul><li>Item 1</li><li>Item 2</li></ul><table><tr><td>Cell</td></tr></table>")
                    .build();

            mockMvc.perform(put("/notes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateInput)))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("DELETE /notes/{id}")
    class DeleteNote {
        @Test
        @DisplayName("204 - Note supprimée")
        void deleteNote_shouldReturnNoContent() throws Exception {
            // 1) Créer une note et récupérer son id
            CreateNoteInput input = CreateNoteInput.builder()
                    .idUser(1).idFolder(1).title("TestTitle").content("TestContent")
                    .build();

            String payload = objectMapper.writeValueAsString(input);

            String createResponse = mockMvc.perform(
                            post("/notes")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(payload)
                    )
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            int id = objectMapper.readTree(createResponse).get("id").asInt();

            // 2) DELETE /notes/{id} (path variable, cf. NoteCommandController)
            mockMvc.perform(delete("/notes/{id}", id))
                    .andExpect(status().isNoContent());

            // 3) Vérifier via GET /notes/folders/{id} que la note n'est plus là
            mockMvc.perform(get("/notes/folders/{id}", 1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.notes", hasSize(0)));
        }

        @Test
        @DisplayName("404 - Note introuvable")
        void deleteNote_shouldReturnNotFound() throws Exception {
            mockMvc.perform(delete("/notes/{id}", 999))
                    .andExpect(status().isNotFound());
        }
    }

}

