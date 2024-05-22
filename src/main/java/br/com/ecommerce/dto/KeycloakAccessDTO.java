package br.com.repassa.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KeycloakAccessDTO {

    @JsonProperty(value = "client_id", required = true)
    String idClient;

    @JsonProperty(value = "grant_type", required = true)
    String grantType;

    @JsonProperty(value = "client_secret", required = true)
    String clientSecret;

    @JsonProperty("code")
    String code;

    @JsonProperty("redirect_uri")
    String redirectUri;

    @JsonProperty("username")
    String username;

    @JsonProperty("password")
    String password;

    @JsonProperty("refresh_token")
    String refreshToken;
}
