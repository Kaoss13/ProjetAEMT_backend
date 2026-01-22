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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class SearchControllerIT {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void cleanAndPopulate() {
        jdbc.execute("SET FOREIGN_KEY_CHECKS = 0");

        // tables
        jdbc.update("DELETE FROM notes");
        jdbc.update("DELETE FROM folder");
        jdbc.update("DELETE FROM users");

        jdbc.execute("SET FOREIGN_KEY_CHECKS = 1");

        // users
        jdbc.update("INSERT INTO users (id, user_name, hash_password) VALUES (1, 'alice', 'x')");
        jdbc.update("INSERT INTO users (id, user_name, hash_password) VALUES (2, 'bob', 'x')");

        // root folder alice
        jdbc.update("INSERT INTO folder (id, id_user, id_parent_folder, title, created_at) VALUES (10, 1, NULL, 'Projets', NOW())");

        // other folders alice
        jdbc.update("INSERT INTO folder (id, id_user, id_parent_folder, title, created_at) VALUES (11, 1, 10, 'Chien', NOW())");
        jdbc.update("INSERT INTO folder (id, id_user, id_parent_folder, title, created_at) VALUES (12, 1, 10, 'Chats', NOW())");

        // notes alice (⚠️ adapte si ta table notes a des NOT NULL supplémentaires)
        // Si ta table notes exige size_bytes/line_count/... alors tu dois aussi les remplir ici.
        jdbc.update("""
            INSERT INTO notes (id, id_user, id_folder, title, content, created_at, updated_at)
            VALUES (100, 1, 10, 'Ma note chien', 'contenu sur un chien', NOW(), NOW())
        """);
        jdbc.update("""
            INSERT INTO notes (id, id_user, id_folder, title, content, created_at, updated_at)
            VALUES (101, 1, 10, 'Random', 'un texte qui parle de chn', NOW(), NOW())
        """);
        jdbc.update("""
            INSERT INTO notes (id, id_user, id_folder, title, content, created_at, updated_at)
            VALUES (102, 1, 10, 'Autre', 'rien à voir', NOW(), NOW())
        """);
    }

    @Nested
    @DisplayName("GET /search")
    class SearchTests {

        @Test
        @DisplayName("200 - Query vide => results vide")
        void search_shouldReturn200_withEmptyResults_whenQueryBlank() throws Exception {
            mockMvc.perform(
                            get("/search")
                                    .param("q", "   ")
                                    .param("userId", "1")
                                    .param("limit", "20")
                    )
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.results", is(empty())));
        }

        @Test
        @DisplayName("404 - User not found")
        void search_shouldReturn404_whenUserNotFound() throws Exception {
            mockMvc.perform(
                            get("/search")
                                    .param("q", "chien")
                                    .param("userId", "999")
                                    .param("limit", "20")
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf("application/problem+json")))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.detail", containsString("User not found")));
        }

        @Test
        @DisplayName("200 - Retourne des résultats et tri par score décroissant")
        void search_shouldReturn200_withResults_sortedByScore() throws Exception {
            var result = mockMvc.perform(
                            get("/search")
                                    .param("q", "chn")
                                    .param("userId", "1")
                                    .param("limit", "20")
                    )
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.results", is(not(empty()))))
                    .andExpect(jsonPath("$.results[*].id", everyItem(notNullValue())))
                    .andExpect(jsonPath("$.results[*].type", everyItem(notNullValue())))
                    .andReturn();

            // Parse JSON -> compare scores
            String json = result.getResponse().getContentAsString();

            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(json);
            com.fasterxml.jackson.databind.JsonNode results = root.get("results");

            for (int i = 0; i < results.size() - 1; i++) {
                double s1 = results.get(i).get("score").asDouble();
                double s2 = results.get(i + 1).get("score").asDouble();
                Assertions.assertTrue(
                        s1 >= s2,
                        "Résultats non triés: score[" + i + "]=" + s1 + " < score[" + (i + 1) + "]=" + s2
                );
            }
        }

        @Test
        @DisplayName("200 - Respecte limit")
        void search_shouldReturn200_andRespectLimit() throws Exception {
            mockMvc.perform(
                            get("/search")
                                    .param("q", "e") // renvoie beaucoup de choses en général
                                    .param("userId", "1")
                                    .param("limit", "2")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.results", hasSize(lessThanOrEqualTo(2))));
        }
    }

    /**
     * Petit helper pour comparer un jsonPath à un nombre sans reparser toute la réponse.
     * Ici on “force” juste une valeur pour matcher l’idée score0 >= score1.
     * Si tu préfères, je te donne la version "on récupère la réponse et on parse en Java".
     */
    private static org.hamcrest.Matcher<Number> jsonPathNumber(String unused) {
        // Hack simple : on ne peut pas injecter facilement $.results[1].score dans un matcher direct.
        // Donc ici: on ne l'utilise pas. Garde juste le test "results not empty" si tu veux éviter ça.
        return any(Number.class);
    }
}
