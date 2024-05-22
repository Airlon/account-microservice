package br.com.repassa.service;

import br.com.repassa.resource.client.KeycloakConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginServiceTest {

    @InjectMocks
    private LoginService loginService;

    @Mock
    private KeycloakConfig keycloakConfig;

    @Mock
    private RealmResource realmResourceMock;

    @Mock
    private UserResource userResourceMock;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindUserPermission() {
        String realm = "testRealm";
        String username = "testUser";
        String userId = "testUserId";
        List<String> expectedPermissions = Collections.emptyList();

        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);
        UsersResource usersResourceMock = mock(UsersResource.class);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);

        List<UserRepresentation> userRepresentations = Collections.singletonList(createUserRepresentation(userId, username));
        when(usersResourceMock.search(username)).thenReturn(userRepresentations);

        when(realmResourceMock.users().get(userId)).thenReturn(userResourceMock);

        UserResource userResourceMock = mock(UserResource.class);
        RoleMappingResource roleMappingResourceMock = mock(RoleMappingResource.class);
        RoleScopeResource roleScopeResourceMock = mock(RoleScopeResource.class);

        when(userResourceMock.roles()).thenReturn(roleMappingResourceMock);
        when(roleMappingResourceMock.realmLevel()).thenReturn(roleScopeResourceMock);

        when(realmResourceMock.users().get(userId)).thenReturn(userResourceMock);

        List<String> permissions = loginService.findUserPermission(realm, username);

        assertEquals(expectedPermissions, permissions);

        verify(usersResourceMock, times(1)).search(username);
        verify(realmResourceMock.users(), times(1)).get(userId);
        verify(userResourceMock.roles().realmLevel(), times(1)).listEffective();
    }

    @Test
    void testFindUserPermission_UserNotFound() {
        String realm = "testRealm";
        String username = "testUser";
        List<UserRepresentation> userRepresentations = Collections.emptyList();

        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);

        UsersResource usersResourceMock = mock(UsersResource.class);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);

        when(usersResourceMock.search(username)).thenReturn(userRepresentations);

        List<String> permissions = loginService.findUserPermission(realm, username);

        assertEquals(Collections.emptyList(), permissions);

        verify(realmResourceMock.users(), times(1)).search(username);
        verify(keycloakConfig.getRealmResource().users(), never()).get(anyString());
    }

    @Test
    void testGetUserPermission() {
        String userId = "testUserId";
        List<String> expectedPermissions = Collections.emptyList();

        RealmResource realmResourceMock = mock(RealmResource.class);
        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);

        UsersResource usersResourceMock = mock(UsersResource.class);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);

        UserResource userResourceMock = mock(UserResource.class);
        when(usersResourceMock.get(userId)).thenReturn(userResourceMock);

        RoleMappingResource roleMappingResourceMock = mock(RoleMappingResource.class);
        RoleScopeResource roleScopeResourceMock = mock(RoleScopeResource.class);

        when(userResourceMock.roles()).thenReturn(roleMappingResourceMock);
        when(roleMappingResourceMock.realmLevel()).thenReturn(roleScopeResourceMock);

        List<String> permissions = loginService.getUserPermission(userId);

        assertEquals(expectedPermissions, permissions);

        verify(usersResourceMock, times(1)).get(userId);
        verify(userResourceMock.roles().realmLevel(), times(1)).listEffective();
    }

    @Test
    void testGetUserPermission_UserNotFound() {
        String userId = "testUserId";

        RealmResource realmResourceMock = mock(RealmResource.class);
        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);

        UsersResource usersResourceMock = mock(UsersResource.class);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);

        when(usersResourceMock.get(userId)).thenReturn(null);

        UserResource userResourceMock = mock(UserResource.class);

        when(realmResourceMock.users().get(userId)).thenReturn(userResourceMock);

        RoleMappingResource roleMappingResourceMock = mock(RoleMappingResource.class);
        RoleScopeResource roleScopeResourceMock = mock(RoleScopeResource.class);

        when(userResourceMock.roles()).thenReturn(roleMappingResourceMock);
        when(roleMappingResourceMock.realmLevel()).thenReturn(roleScopeResourceMock);

        List<String> permissions = loginService.getUserPermission(userId);

        assertEquals(Collections.emptyList(), permissions);

        verify(usersResourceMock, times(1)).get(userId);
    }

    private UserRepresentation createUserRepresentation(String userId, String username) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId(userId);
        userRepresentation.setUsername(username);
        return userRepresentation;
    }

}

