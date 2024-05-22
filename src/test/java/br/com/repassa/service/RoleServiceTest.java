package br.com.repassa.service;

import br.com.backoffice_repassa_utils_lib.error.exception.RepassaException;
import br.com.repassa.dto.RoleKeycloakDTO;
import br.com.repassa.exception.AccountError;
import br.com.repassa.resource.client.KeycloakConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class RoleServiceTest {

    @InjectMocks
    RoleService roleService;

    @Mock
    private KeycloakConfig keycloakConfig;

    @Mock
    RealmResource realmResource;

    @Mock
    RolesResource rolesResource;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

    }


    @Test
    public void mustReturnTrueWhenRoleExist(){
        List<RoleRepresentation> roles = new ArrayList<>();
        RoleRepresentation role = new RoleRepresentation();
        role.setName("roleTest");
        roles.add(role);
        String realm = System.getProperty("quarkus.keycloak.realm");

        Keycloak keycloak = Mockito.mock(Keycloak.class);

        when(keycloakConfig.getKeycloak()).thenReturn(keycloak);
        when(keycloakConfig.getKeycloak().realm(realm)).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource()).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource().roles()).thenReturn(rolesResource);
        when(rolesResource.list()).thenReturn(roles);

        boolean retorno = roleService.roleExists("roleTest");

        assertTrue(retorno);

    }

    @Test
    public void mustReturnFalseWhenRoleExist(){
        List<RoleRepresentation> roles = new ArrayList<>();
        RoleRepresentation role = new RoleRepresentation();
        role.setName("roleTest");
        roles.add(role);
        String realm = System.getProperty("quarkus.keycloak.realm");

        Keycloak keycloak = Mockito.mock(Keycloak.class);

        when(keycloakConfig.getKeycloak()).thenReturn(keycloak);
        when(keycloakConfig.getKeycloak().realm(realm)).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource()).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource().roles()).thenReturn(rolesResource);
        when(rolesResource.list()).thenReturn(roles);

        boolean retorno = roleService.roleExists("roleTestInvalid");

        assertFalse(retorno);
    }

    @Test
    public void mustReturnOkWhenAddRoleRealmToGroup() throws RepassaException {
        String realm = System.getProperty("quarkus.keycloak.realm");
        String roleName = "roleTest";
        String groupId = "groupTest";

        RoleResource roleResourceMock = Mockito.mock(RoleResource.class);
        GroupResource groupResourceMock = Mockito.mock(GroupResource.class);
        GroupsResource groupsResourceMock = Mockito.mock(GroupsResource.class);
        Keycloak keycloak = Mockito.mock(Keycloak.class);

        when(keycloakConfig.getKeycloak()).thenReturn(keycloak);
        when(keycloakConfig.getKeycloak().realm(realm)).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource()).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource().roles()).thenReturn(rolesResource);
        when(rolesResource.get(roleName)).thenReturn(roleResourceMock);
        when(realmResource.groups()).thenReturn(groupsResourceMock);
        when(realmResource.groups().group(groupId)).thenReturn(groupResourceMock);

        List<RoleRepresentation> roles = new ArrayList<>();
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);
        roles.add(role);

        RoleMappingResource roleMappingResource = Mockito.mock(RoleMappingResource.class);
        RoleScopeResource roleScopeResource = Mockito.mock(RoleScopeResource.class);

        when(groupResourceMock.roles()).thenReturn(roleMappingResource);
        when(groupResourceMock.roles().realmLevel()).thenReturn(roleScopeResource);
        when(groupResourceMock.roles().realmLevel().listAll()).thenReturn(roles);
        Response response = roleService.addRoleRealmToGroup(groupId, Collections.singletonList(roleName));
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void mustReturnErrorWhenAddRoleRealmToGroupAndNotFoundRole(){
        String realm = System.getProperty("quarkus.keycloak.realm");
        String roleName = "roleTest";
        String groupId = "groupTest";

        GroupResource groupResourceMock = Mockito.mock(GroupResource.class);
        GroupsResource groupsResourceMock = Mockito.mock(GroupsResource.class);
        Keycloak keycloak = Mockito.mock(Keycloak.class);

        when(keycloakConfig.getKeycloak()).thenReturn(keycloak);
        when(keycloakConfig.getKeycloak().realm(realm)).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource()).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource().roles()).thenReturn(rolesResource);
        when(rolesResource.get(roleName)).thenReturn(null);
        when(realmResource.groups()).thenReturn(groupsResourceMock);
        when(realmResource.groups().group(groupId)).thenReturn(groupResourceMock);

        List<RoleRepresentation> roles = new ArrayList<>();
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);
        roles.add(role);

        RoleMappingResource roleMappingResource = Mockito.mock(RoleMappingResource.class);
        RoleScopeResource roleScopeResource = Mockito.mock(RoleScopeResource.class);

        when(groupResourceMock.roles()).thenReturn(roleMappingResource);
        when(groupResourceMock.roles().realmLevel()).thenReturn(roleScopeResource);
        when(groupResourceMock.roles().realmLevel().listAll()).thenReturn(roles);

        Assertions.assertThrows(RepassaException.class, () -> roleService.addRoleRealmToGroup(groupId, Collections.singletonList(roleName)));
    }

    @Test
    public void mustReturnOkWhenRemoveRoleRealmToGroup() throws RepassaException {
        String realm = System.getProperty("quarkus.keycloak.realm");
        String roleName = "roleTest";
        String groupId = "groupTest";

        RoleResource roleResourceMock = Mockito.mock(RoleResource.class);
        GroupResource groupResourceMock = Mockito.mock(GroupResource.class);
        GroupsResource groupsResourceMock = Mockito.mock(GroupsResource.class);
        Keycloak keycloak = Mockito.mock(Keycloak.class);

        when(keycloakConfig.getKeycloak()).thenReturn(keycloak);
        when(keycloakConfig.getKeycloak().realm(realm)).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource()).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource().roles()).thenReturn(rolesResource);
        when(rolesResource.get(roleName)).thenReturn(roleResourceMock);
        when(realmResource.groups()).thenReturn(groupsResourceMock);
        when(realmResource.groups().group(groupId)).thenReturn(groupResourceMock);

        List<RoleRepresentation> roles = new ArrayList<>();
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);
        roles.add(role);

        RoleMappingResource roleMappingResource = Mockito.mock(RoleMappingResource.class);
        RoleScopeResource roleScopeResource = Mockito.mock(RoleScopeResource.class);

        when(groupResourceMock.roles()).thenReturn(roleMappingResource);
        when(groupResourceMock.roles().realmLevel()).thenReturn(roleScopeResource);
        when(groupResourceMock.roles().realmLevel().listAll()).thenReturn(roles);
        Response response = roleService.removeRoleRealmToGroup(groupId, Collections.singletonList(roleName));
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void mustReturnErrorWhenRemoveRoleRealmToGroupAndNotFoundRole(){
        String realm = System.getProperty("quarkus.keycloak.realm");
        String roleName = "roleTest";
        String groupId = "groupTest";

        GroupResource groupResourceMock = Mockito.mock(GroupResource.class);
        GroupsResource groupsResourceMock = Mockito.mock(GroupsResource.class);
        Keycloak keycloak = Mockito.mock(Keycloak.class);

        when(keycloakConfig.getKeycloak()).thenReturn(keycloak);
        when(keycloakConfig.getKeycloak().realm(realm)).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource()).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource().roles()).thenReturn(rolesResource);
        when(rolesResource.get(roleName)).thenReturn(null);
        when(realmResource.groups()).thenReturn(groupsResourceMock);
        when(realmResource.groups().group(groupId)).thenReturn(groupResourceMock);

        List<RoleRepresentation> roles = new ArrayList<>();
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);
        roles.add(role);

        RoleMappingResource roleMappingResource = Mockito.mock(RoleMappingResource.class);
        RoleScopeResource roleScopeResource = Mockito.mock(RoleScopeResource.class);

        when(groupResourceMock.roles()).thenReturn(roleMappingResource);
        when(groupResourceMock.roles().realmLevel()).thenReturn(roleScopeResource);
        when(groupResourceMock.roles().realmLevel().listAll()).thenReturn(roles);

        Assertions.assertThrows(RepassaException.class, () -> roleService.removeRoleRealmToGroup(groupId, Collections.singletonList(roleName)));
    }

    @Test
    public void mustReturnOkWhenAddRoleClientToGroup() throws RepassaException {
        String realm = System.getProperty("quarkus.keycloak.realm");
        String roleName = "roleTest";
        String groupId = "groupTest";
        String clientId = "clientTest";

        RoleResource roleResourceMock = Mockito.mock(RoleResource.class);
        GroupResource groupResourceMock = Mockito.mock(GroupResource.class);
        GroupsResource groupsResourceMock = Mockito.mock(GroupsResource.class);
        Keycloak keycloak = Mockito.mock(Keycloak.class);

        when(keycloakConfig.getKeycloak()).thenReturn(keycloak);
        when(keycloakConfig.getKeycloak().realm(realm)).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource()).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource().roles()).thenReturn(rolesResource);
        when(rolesResource.get(roleName)).thenReturn(roleResourceMock);
        when(realmResource.groups()).thenReturn(groupsResourceMock);
        when(realmResource.groups().group(groupId)).thenReturn(groupResourceMock);

        List<RoleRepresentation> roles = new ArrayList<>();
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);
        roles.add(role);

        ClientsResource clientsResource = Mockito.mock(ClientsResource.class);
        ClientResource clientResource = Mockito.mock(ClientResource.class);
        RoleMappingResource roleMappingResource = Mockito.mock(RoleMappingResource.class);
        RoleScopeResource roleScopeResource = Mockito.mock(RoleScopeResource.class);
        ClientRepresentation clientRepresentation = new ClientRepresentation();
        clientRepresentation.setId(clientId);

        when(realmResource.clients()).thenReturn(clientsResource);
        when(clientsResource.get(clientId)).thenReturn(clientResource);
        when(clientResource.roles()).thenReturn(rolesResource);
        when(rolesResource.list()).thenReturn(roles);
        when(clientResource.toRepresentation()).thenReturn(clientRepresentation);
        when(groupResourceMock.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.clientLevel(clientId)).thenReturn(roleScopeResource);

        Response response = roleService.addRoleClientToGroup(groupId,clientId, Collections.singletonList(roleName));
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void mustReturnOkWhenRemoveRoleClientToGroup(){
        String realm = System.getProperty("quarkus.keycloak.realm");
        String roleName = "roleTest";
        String groupId = "groupTest";
        String clientId = "clientTest";

        RoleResource roleResourceMock = Mockito.mock(RoleResource.class);
        GroupResource groupResourceMock = Mockito.mock(GroupResource.class);
        GroupsResource groupsResourceMock = Mockito.mock(GroupsResource.class);
        Keycloak keycloak = Mockito.mock(Keycloak.class);

        when(keycloakConfig.getKeycloak()).thenReturn(keycloak);
        when(keycloakConfig.getKeycloak().realm(realm)).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource()).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource().roles()).thenReturn(rolesResource);
        when(rolesResource.get(roleName)).thenReturn(roleResourceMock);
        when(realmResource.groups()).thenReturn(groupsResourceMock);
        when(realmResource.groups().group(groupId)).thenReturn(groupResourceMock);

        List<RoleRepresentation> roles = new ArrayList<>();
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);
        roles.add(role);

        ClientsResource clientsResource = Mockito.mock(ClientsResource.class);
        ClientResource clientResource = Mockito.mock(ClientResource.class);
        RoleMappingResource roleMappingResource = Mockito.mock(RoleMappingResource.class);
        RoleScopeResource roleScopeResource = Mockito.mock(RoleScopeResource.class);
        ClientRepresentation clientRepresentation = new ClientRepresentation();
        clientRepresentation.setId(clientId);

        when(realmResource.clients()).thenReturn(clientsResource);
        when(clientsResource.get(clientId)).thenReturn(null);
        when(clientResource.roles()).thenReturn(rolesResource);
        when(rolesResource.list()).thenReturn(roles);
        when(clientResource.toRepresentation()).thenReturn(clientRepresentation);
        when(groupResourceMock.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.clientLevel(clientId)).thenReturn(roleScopeResource);

        Assertions.assertThrows(RepassaException.class, () -> roleService.addRoleClientToGroup(groupId,clientId, Collections.singletonList(roleName)));
    }

    @Test
    public void mustReturnRoleExistsWhenCreateDuplicateRole(){
        String roleName = "roleTest";
        RoleKeycloakDTO dto = new RoleKeycloakDTO();
        dto.setName(roleName);

        when(keycloakConfig.getRealmResource()).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource().roles()).thenReturn(rolesResource);

        List<RoleRepresentation> roles = new ArrayList<>();
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);
        roles.add(role);

        when(keycloakConfig.getRealmResource().roles().list()).thenReturn(roles);

        Assertions.assertThrows(RepassaException.class, () -> roleService.create(dto));
    }

    @Test
    public void mustReturnOkWhenCreateRole() throws Exception {
        String roleName = "roleTest";
        String roleNameNew = "newRoleTest";
        RoleKeycloakDTO dto = new RoleKeycloakDTO();
        dto.setName(roleNameNew);

        when(keycloakConfig.getRealmResource()).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource().roles()).thenReturn(rolesResource);

        List<RoleRepresentation> roles = new ArrayList<>();
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);
        roles.add(role);

        when(keycloakConfig.getRealmResource().roles().list()).thenReturn(roles);

        RoleRepresentation roleCreated = roleService.create(dto);
        assertEquals(roleCreated.getName(), dto.getName());
    }

    @Test
    public void mustReturnOkWhenUpdateRole() throws Exception {
        String roleName = "roleTest";
        String roleNameNew = "newRoleTest";
        String roleNameRandom = "roleRandom";
        RoleKeycloakDTO dto = new RoleKeycloakDTO();
        dto.setName(roleNameNew);

        when(keycloakConfig.getRealmResource()).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource().roles()).thenReturn(rolesResource);

        List<RoleRepresentation> roles = new ArrayList<>();
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleNameRandom);
        roles.add(role);

        when(keycloakConfig.getRealmResource().roles().list()).thenReturn(roles);

        RoleResource roleResource = Mockito.mock(RoleResource.class);
        RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setName(roleName);

        when(realmResource.roles().get(roleName)).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(roleRepresentation);

        RoleRepresentation roleUpdated = roleService.update(roleName, dto);
        assertEquals(roleUpdated.getName(), dto.getName());

    }

    @Test
    public void mustReturnNotFoundWhenUpdateRole() throws Exception {
        String roleName = "roleTest";
        String roleNameNew = "newRoleTest";
        String roleNameRandom = "roleRandom";
        RoleKeycloakDTO dto = new RoleKeycloakDTO();
        dto.setName(roleNameNew);

        when(keycloakConfig.getRealmResource()).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource().roles()).thenReturn(rolesResource);

        List<RoleRepresentation> roles = new ArrayList<>();
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleNameRandom);
        roles.add(role);

        when(keycloakConfig.getRealmResource().roles().list()).thenReturn(roles);

        RoleResource roleResource = Mockito.mock(RoleResource.class);
        RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setName(roleName);

        when(realmResource.roles().get(roleName)).thenReturn(null);
        when(roleResource.toRepresentation()).thenReturn(roleRepresentation);

        Assertions.assertThrows(RepassaException.class, () -> roleService.update(roleName, dto));

    }

    @Test
    public void mustReturnDuplicateExceptionWhenUpdateRole() {
        String roleName = "roleTest";
        String roleNameNew = "newRoleTest";
        RoleKeycloakDTO dto = new RoleKeycloakDTO();
        dto.setName(roleNameNew);

        when(keycloakConfig.getRealmResource()).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource().roles()).thenReturn(rolesResource);

        List<RoleRepresentation> roles = new ArrayList<>();
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleNameNew);
        roles.add(role);

        when(keycloakConfig.getRealmResource().roles().list()).thenReturn(roles);

        RoleResource roleResource = Mockito.mock(RoleResource.class);
        RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setName(roleName);

        when(realmResource.roles().get(roleName)).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(roleRepresentation);

        Assertions.assertThrows(RepassaException.class, () -> roleService.update(roleName, dto));

    }

    @Test
    public void mustReturnExceptionWhenUpdateRoleWithOldNameAndNewNameEquals() {
        String roleName = "roleTest";
        String roleNameNew = "newRoleTest";
        RoleKeycloakDTO dto = new RoleKeycloakDTO();
        dto.setName(roleNameNew);

        when(keycloakConfig.getRealmResource()).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource().roles()).thenReturn(rolesResource);

        List<RoleRepresentation> roles = new ArrayList<>();
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);
        roles.add(role);

        when(keycloakConfig.getRealmResource().roles().list()).thenReturn(roles);

        RoleResource roleResource = Mockito.mock(RoleResource.class);
        RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setName(roleName);

        when(realmResource.roles().get(roleName)).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(roleRepresentation);

        Assertions.assertThrows(RepassaException.class, () -> roleService.update(roleNameNew, dto));

    }

    @Test
    public void mustReturnOkWhenDeleteRole() throws Exception {
        String roleName = "roleTest";

        when(keycloakConfig.getRealmResource()).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource().roles()).thenReturn(rolesResource);

        RoleResource roleResource = Mockito.mock(RoleResource.class);
        when(realmResource.roles().get(roleName)).thenReturn(roleResource);

        RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setName(roleName);
        when(roleResource.toRepresentation()).thenReturn(roleRepresentation);

        Response roleDeleted = roleService.delete(roleName);
        assertEquals(roleDeleted.getStatus(), Response.Status.OK.getStatusCode());

    }

    @Test
    public void mustReturnNotFoundWhenDeleteRole() throws Exception {
        String roleName = "roleTest";

        when(keycloakConfig.getRealmResource()).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource().roles()).thenReturn(rolesResource);

        RoleResource roleResource = Mockito.mock(RoleResource.class);
        when(realmResource.roles().get(roleName)).thenReturn(null);

        RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setName(roleName);
        when(roleResource.toRepresentation()).thenReturn(roleRepresentation);

        Assertions.assertThrows(RepassaException.class, () -> roleService.delete(roleName));

    }

    @Test
    public void mustReturnOkWhenGetRoleById() throws JsonProcessingException, RepassaException {
        String roleName = "roleTest";

        when(keycloakConfig.getRealmResource()).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource().roles()).thenReturn(rolesResource);

        RoleResource roleResource = Mockito.mock(RoleResource.class);
        when(realmResource.roles().get(roleName)).thenReturn(roleResource);

        List<RoleRepresentation> roles = new ArrayList<>();
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);
        roles.add(role);

        when(keycloakConfig.getRealmResource().roles().list()).thenReturn(roles);

        Response roleFind = roleService.getById(roleName);

        assertEquals(roleFind.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void mustReturnNotFoundWhenGetRoleById() throws JsonProcessingException {
        String roleName = "roleTest";

        when(keycloakConfig.getRealmResource()).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource().roles()).thenReturn(rolesResource);
        when(realmResource.roles().get(roleName)).thenReturn(null);

        List<RoleRepresentation> roles = new ArrayList<>();
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);
        roles.add(role);

        when(keycloakConfig.getRealmResource().roles().list()).thenReturn(roles);

        Assertions.assertThrows(RepassaException.class, () -> roleService.getById(roleName));
    }

    @Test
    public void mustReturnNoContentWhenGetRoleById() throws JsonProcessingException, RepassaException {
        String roleName = "roleTest";

        when(keycloakConfig.getRealmResource()).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource().roles()).thenReturn(rolesResource);

        RoleResource roleResource = Mockito.mock(RoleResource.class);
        when(realmResource.roles().get(roleName)).thenReturn(roleResource);

        List<RoleRepresentation> roles = new ArrayList<>();

        when(keycloakConfig.getRealmResource().roles().list()).thenReturn(roles);

        Response roleFind = roleService.getById(roleName);

        assertEquals(roleFind.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    public void mustReturnOkWhenGetAllRoles() throws JsonProcessingException {
        String roleName = "roleTest";

        when(keycloakConfig.getRealmResource()).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource().roles()).thenReturn(rolesResource);

        List<RoleRepresentation> roles = new ArrayList<>();
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);
        roles.add(role);

        when(keycloakConfig.getRealmResource().roles().list()).thenReturn(roles);

        Response roleFind = roleService.getAll();

        assertEquals(roleFind.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void mustReturnNoContentWhenGetAllRoles() throws JsonProcessingException {
        when(keycloakConfig.getRealmResource()).thenReturn(realmResource);
        when(keycloakConfig.getRealmResource().roles()).thenReturn(rolesResource);

        List<RoleRepresentation> roles = new ArrayList<>();

        when(keycloakConfig.getRealmResource().roles().list()).thenReturn(roles);

        Response roleFind = roleService.getAll();

        assertEquals(roleFind.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
    }


}
