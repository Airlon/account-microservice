package br.com.repassa.service;

import br.com.repassa.resource.client.KeycloakConfig;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
@Slf4j
public class LoginService {

    @Inject
    KeycloakConfig keycloakConfig;

    public List<String> findUserPermission(String realm, String username) {
        RealmResource realmResource = keycloakConfig.getRealmResource();

        List<UserRepresentation> userRepresentations = realmResource.users().search(username);
        String userId = findIdByUsername(userRepresentations, username);

        if (userId != null) {
            return getUserPermission(userId);
        }

        return Collections.emptyList();
    }

    public static String findIdByUsername(List<UserRepresentation> users, String username) {
        for (UserRepresentation user : users) {
            if (user.getUsername() != null && user.getUsername().equals(username)) {
                return user.getId();
            }
        }
        return null;
    }

    public List<String> getUserPermission(String userId) {
        UserResource userResource = keycloakConfig.getRealmResource().users().get(userId);

        List<RoleRepresentation> roles = userResource.roles().realmLevel().listEffective();

        return roles.stream()
                .map(RoleRepresentation::getName)
                .toList();
    }
}


