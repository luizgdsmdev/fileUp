package com.bytebybyte.fileup.Config.Security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Test
    void shouldReturn401WhenJwtIsMissing() throws Exception {

        UUID userId = UUID.randomUUID();

        mockMvc.perform(
                        get("/api/v1/user/{userId}", userId)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenJwtIsInvalid() throws Exception {

        UUID userId = UUID.randomUUID();

        mockMvc.perform(
                        get("/api/v1/user/{userId}", userId)
                                .header(
                                        "Authorization",
                                        "Bearer invalid-token"
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenJwtIsExpired() throws Exception {

        UUID userId = UUID.randomUUID();

        String expiredToken = generateExpiredToken();

        mockMvc.perform(
                        get("/api/v1/user/{userId}", userId)
                                .header(
                                        "Authorization",
                                        "Bearer " + expiredToken
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAccessWhenJwtIsValid() throws Exception {

        UUID userId = UUID.randomUUID();

        String validToken = generateValidToken();

        mockMvc.perform(
                        get("/api/v1/user/{userId}", userId)
                                .header(
                                        "Authorization",
                                        "Bearer " + validToken
                                )
                )
                .andExpect(status().isNotFound());
    }

    private String generateExpiredToken() {

        Instant issuedAt = Instant.now().minusSeconds(120);
        Instant expiration = Instant.now().minusSeconds(60);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("fileup_backend_AuthService_login_method")
                .subject(UUID.randomUUID().toString())
                .issuedAt(issuedAt)
                .expiresAt(expiration)
                .build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(claims))
                .getTokenValue();
    }

    private String generateValidToken() {

        Instant issuedAt = Instant.now();
        Instant expiration = Instant.now().plusSeconds(300);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("fileup_backend_AuthService_login_method")
                .subject(UUID.randomUUID().toString())
                .issuedAt(issuedAt)
                .expiresAt(expiration)
                .build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(claims))
                .getTokenValue();
    }
}
