package com.helha.projetaemt_backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helha.projetaemt_backend.application.user.command.create.CreateUserInput;
import com.helha.projetaemt_backend.application.user.command.login.LoginUserInput;
import com.helha.projetaemt_backend.infrastructure.user.IUserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
class UserControllerIT {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    IUserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /users - Register")
    class Register {

        @Test
        @DisplayName("201 - Crée un utilisateur")
        void register_shouldReturn201_andBody() throws Exception {
            CreateUserInput input = new CreateUserInput();
            input.userName = "testuser";
            input.password = "Test123!";

            String payload = objectMapper.writeValueAsString(input);

            mockMvc.perform(
                            post("/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(payload)
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.userName").value("testuser"));
        }

        @Test
        @DisplayName("400 - userName déjà existant")
        void register_shouldReturn400_whenUserExists() throws Exception {
            // Créer un premier utilisateur
            CreateUserInput input1 = new CreateUserInput();
            input1.userName = "testuser";
            input1.password = "Test123!";

            String payload1 = objectMapper.writeValueAsString(input1);

            mockMvc.perform(
                            post("/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(payload1)
                    )
                    .andExpect(status().isCreated());

            // Essayer de créer un deuxième utilisateur avec le même userName
            CreateUserInput input2 = new CreateUserInput();
            input2.userName = "testuser";
            input2.password = "AutrePassword123!";

            String payload2 = objectMapper.writeValueAsString(input2);

            mockMvc.perform(
                            post("/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(payload2)
                    )
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("POST /users/login - Login")
    class Login {

        @Test
        @DisplayName("200 - Login réussi")
        void login_shouldReturn200_andBody() throws Exception {
            // 1) Créer un utilisateur
            CreateUserInput registerInput = new CreateUserInput();
            registerInput.userName = "testuser";
            registerInput.password = "Test123!";

            String registerPayload = objectMapper.writeValueAsString(registerInput);

            mockMvc.perform(
                            post("/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(registerPayload)
                    )
                    .andExpect(status().isCreated());

            // 2) Login avec les mêmes credentials
            LoginUserInput loginInput = new LoginUserInput();
            loginInput.userName = "testuser";
            loginInput.password = "Test123!";

            String loginPayload = objectMapper.writeValueAsString(loginInput);

            mockMvc.perform(
                            post("/users/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(loginPayload)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.userName").value("testuser"))
                    .andExpect(jsonPath("$.token").isNotEmpty());
        }

        @Test
        @DisplayName("400 - userName incorrect")
        void login_shouldReturn400_whenUserNotFound() throws Exception {
            LoginUserInput input = new LoginUserInput();
            input.userName = "inexistant";
            input.password = "Test123!";

            String payload = objectMapper.writeValueAsString(input);

            mockMvc.perform(
                            post("/users/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(payload)
                    )
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("400 - Mot de passe incorrect")
        void login_shouldReturn400_whenPasswordWrong() throws Exception {
            // 1) Créer un utilisateur
            CreateUserInput registerInput = new CreateUserInput();
            registerInput.userName = "testuser";
            registerInput.password = "Test123!";

            String registerPayload = objectMapper.writeValueAsString(registerInput);

            mockMvc.perform(
                            post("/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(registerPayload)
                    )
                    .andExpect(status().isCreated());

            // 2) Login avec mauvais mot de passe
            LoginUserInput loginInput = new LoginUserInput();
            loginInput.userName = "testuser";
            loginInput.password = "MauvaisPassword!";

            String loginPayload = objectMapper.writeValueAsString(loginInput);

            mockMvc.perform(
                            post("/users/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(loginPayload)
                    )
                    .andExpect(status().isUnauthorized());
        }
    }
}
