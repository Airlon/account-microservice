package br.com.repassa.resource.client;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class KeycloakConfig {
    private Keycloak keycloak;
    private final String realm;

    public KeycloakConfig(@ConfigProperty(name = "quarkus.keycloak.auth-server-url") String serverUrl,
                          @ConfigProperty(name = "quarkus.keycloak.realm") String realm,
                          @ConfigProperty(name = "quarkus.keycloak.client-name") String clientName,
                          @ConfigProperty(name = "quarkus.keycloak.client-secret") String clientSecret,
                          @ConfigProperty(name = "quarkus.keycloak.user-name") String userName,
                          @ConfigProperty(name = "quarkus.keycloak.user-password") String userSecret) {

        buildKeycloak(serverUrl,realm, clientName, clientSecret, userName, userSecret);

        this.realm = realm;
    }

    private void buildKeycloak(String serverUrl,String realm, String clientId, String clientSecret, String user, String password){

        ResteasyClient resteasyClient = new ResteasyClientBuilderImpl().build();

        keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .username(user)
                .password(password)
                .resteasyClient(resteasyClient)
                .build();
    }

    public Keycloak getKeycloak() {
        return keycloak;
    }

    public RealmResource getRealmResource() {
        return keycloak.realm(realm);
    }
}
