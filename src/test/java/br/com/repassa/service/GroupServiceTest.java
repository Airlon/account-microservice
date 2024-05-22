package br.com.repassa.service;

import br.com.backoffice_repassa_utils_lib.error.exception.RepassaException;
import br.com.repassa.dto.CategoryPermissionDTO;
import br.com.repassa.dto.GroupDto;

import br.com.repassa.dto.PermissionDTO;
import br.com.repassa.exception.AccountError;
import br.com.repassa.resource.client.KeycloakConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class GroupServiceTest {
    @Mock
    private KeycloakConfig keycloakConfig;

    @Mock
    RealmResource realmResourceMock;

    @Mock
    GroupsResource groupsResourceMock;

    @Mock
    GroupResource groupResourceMock;

    @Mock
    RolesResource rolesResource;

    @Mock
    AccountError accountError;

    @Mock
    GroupRepresentation groupRepresentation;

    @InjectMocks
    GroupService groupService;

    @Mock
    private ClientService clientServiceMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByGroupNotFoundException() {
        String groupId = "nonExistentGroupId";

        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);

        GroupsResource groupsResourceMock = mock(GroupsResource.class);
        when(realmResourceMock.groups()).thenReturn(groupsResourceMock);

        GroupResource groupResourceMock = mock(GroupResource.class);
        when(groupsResourceMock.group(groupId)).thenReturn(groupResourceMock);

        when(groupResourceMock.toRepresentation()).thenThrow(NotFoundException.class);

        RepassaException exception = assertThrows(RepassaException.class, () -> {
            groupService.findByGroup(groupId);
        });

        assertEquals(AccountError.GRUPO_NAO_ENCONTRADO, exception.getRepassaUtilError());
    }

    @Test
    void testGetRolesClients() {
        Map<String, String> clients = Collections.singletonMap("clientId", "clientName");
        when(clientServiceMock.getClientsIdsNames()).thenReturn(clients);

        RoleMappingResource roleMappingResourceMock = mock(RoleMappingResource.class);
        when(groupResourceMock.roles()).thenReturn(roleMappingResourceMock);

        RoleScopeResource roleScopeResourceMock = mock(RoleScopeResource.class);
        when(roleMappingResourceMock.clientLevel(anyString())).thenReturn(roleScopeResourceMock);

        List<RoleRepresentation> rolesList = Collections.singletonList(new RoleRepresentation());
        when(roleScopeResourceMock.listAll()).thenReturn(rolesList);

        Map<String, List<String>> rolesClients = groupService.getRolesClients(groupResourceMock);

        assertFalse(rolesClients.isEmpty());
        assertTrue(rolesClients.containsKey("clientName"));
        assertEquals(1, rolesClients.get("clientName").size());
    }

    @Test
    void testGetUserByGroup() throws RepassaException {

        RealmResource realmResourceMock = mock(RealmResource.class);

        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);

        GroupsResource groupsResourceMock = mock(GroupsResource.class);

        when(realmResourceMock.groups()).thenReturn(groupsResourceMock);

        GroupResource groupResourceMock = mock(GroupResource.class);
        when(groupsResourceMock.group(anyString())).thenReturn(groupResourceMock);

        List<UserRepresentation> members = Collections.singletonList(new UserRepresentation());
        when(groupResourceMock.members()).thenReturn(members);
        Response response = groupService.getUserByGroup("groupId");

        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void testFindGroupStatistics() throws RepassaException {
        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);

        GroupsResource groupsResourceMock = mock(GroupsResource.class);
        when(realmResourceMock.groups()).thenReturn(groupsResourceMock);

        List<GroupRepresentation> groupRepresentationList = Collections.singletonList(new GroupRepresentation());
        when(groupsResourceMock.groups()).thenReturn(groupRepresentationList);

        GroupResource groupResourceMock = mock(GroupResource.class);
        when(groupsResourceMock.group(anyString())).thenReturn(groupResourceMock);
        when(groupResourceMock.toRepresentation()).thenReturn(new GroupRepresentation());

        List<UserRepresentation> members = Collections.singletonList(new UserRepresentation());
        when(groupResourceMock.members()).thenReturn(members);

        String groupId = "d4362349-3834-4b77-b467-85deafb694ae";

        String groupName = "TestGroup";
        GroupRepresentation group = new GroupRepresentation();
        group.setName(groupName);
        group.setId(groupId);

        List<GroupRepresentation> groups = Collections.singletonList(group);
        when(groupsResourceMock.groups()).thenReturn(groups);

        GroupRepresentation groupRepresentationMock = mock(GroupRepresentation.class);

        GroupResource groupResourcseMock = mock(GroupResource.class);
        when(groupsResourceMock.group(anyString())).thenReturn(groupResourcseMock);

        when(groupResourcseMock.toRepresentation()).thenReturn(groupRepresentationMock);

        when(groupRepresentationMock.getId()).thenReturn("groupId");
        when(groupRepresentationMock.getName()).thenReturn("TestGroup");
        when(groupRepresentationMock.getAttributes()).thenReturn(Map.of("description", List.of("Test Description")));

        Response response = groupService.findGroupStatistics();

        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void testCreateGroupCatch() {
        GroupDto groupDto = new GroupDto();
        groupDto.setGroup("TestGroup");
        groupDto.setDescription("Test Description");

        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);

        GroupsResource groupsResourceMock = mock(GroupsResource.class);
        when(realmResourceMock.groups()).thenReturn(groupsResourceMock);

        when(groupsResourceMock.add(any(GroupRepresentation.class))).thenReturn(Response.serverError().build());

        RepassaException exception = assertThrows(RepassaException.class, () -> {
            groupService.createGroup(groupDto);
        });

        assertEquals(AccountError.ERRO_CRIAR_GRUPO, exception.getRepassaUtilError());

    }
    @Test
    void testUpdateGroup() throws RepassaException {
        String groupId = "d4362349-3834-4b77-b467-85deafb694ae";
        GroupDto groupDto = new GroupDto();
        groupDto.setGroup("UpdatedGroup");
        groupDto.setDescription("Updated Description");

        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);

        GroupsResource groupsResourceMock = mock(GroupsResource.class);
        when(realmResourceMock.groups()).thenReturn(groupsResourceMock);

        GroupResource groupResourceMock = mock(GroupResource.class);
        when(groupsResourceMock.group(anyString())).thenReturn(groupResourceMock);

        GroupRepresentation groupRepresentationMock = mock(GroupRepresentation.class);
        when(groupResourceMock.toRepresentation()).thenReturn(groupRepresentationMock);

        Response response = groupService.updateGroup(groupId, groupDto);

        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void testDeleteGroup() throws RepassaException {

        String idGroup = "d4362349-3834-4b77-b467-85deafb694ae";
        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);

        GroupsResource groupsResourceMock = mock(GroupsResource.class);
        when(realmResourceMock.groups()).thenReturn(groupsResourceMock);

        GroupResource groupResourceMock = mock(GroupResource.class);
        when(groupsResourceMock.group(anyString())).thenReturn(groupResourceMock);

        Response response = groupService.deleteGroup(idGroup);

        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void testDeleteGroupNotFoundException() {
        String idGroup = "d4362349-3834-4b77-b467-85deafb694ae";
        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);

        GroupsResource groupsResourceMock = mock(GroupsResource.class);
        when(realmResourceMock.groups()).thenReturn(groupsResourceMock);

        GroupResource groupResourceMock = mock(GroupResource.class);
        when(groupsResourceMock.group(anyString())).thenReturn(groupResourceMock);

        doThrow(new NotFoundException("Group not found")).when(groupResourceMock).remove();

        RepassaException exception = assertThrows(RepassaException.class, () -> {
            groupService.deleteGroup(idGroup);
        });

        assertEquals(AccountError.GRUPO_NAO_ENCONTRADO, exception.getRepassaUtilError());

    }

    @Test
    void testIsExists() {
        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);

        GroupsResource groupsResourceMock = mock(GroupsResource.class);
        when(realmResourceMock.groups()).thenReturn(groupsResourceMock);

        String groupName = "TestGroup";
        GroupRepresentation group = new GroupRepresentation();
        group.setName(groupName);

        List<GroupRepresentation> groups = Collections.singletonList(group);
        when(groupsResourceMock.groups()).thenReturn(groups);

       Assertions.assertTrue(groupService.isExists("TestGroup"));
    }

    @Test
    public void testIsExistsObject() {

        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);

        GroupsResource groupsResourceMock = mock(GroupsResource.class);
        when(realmResourceMock.groups()).thenReturn(groupsResourceMock);

        String groupName = "TestGroup";
        GroupRepresentation group = new GroupRepresentation();
        group.setName(groupName);

        List<GroupRepresentation> groups = Collections.singletonList(group);
        when(groupsResourceMock.groups()).thenReturn(groups);
        assertNotNull(groupService.isExistsObject("TestGroup"));

    }

    @Test
    void testValidGroupStrings() {
        GroupDto groupDto = new GroupDto();
        groupDto.setGroup("TestGroup");
        groupDto.setDescription("Test Description");

        assertDoesNotThrow(() -> groupService.validGroupStrings(groupDto));

        groupDto.setGroup("A".repeat(256));
        RepassaException exceptionNomeGrupo = assertThrows(RepassaException.class, () -> groupService.validGroupStrings(groupDto));
        assertEquals(AccountError.NOME_GRUPO_INVALIDO, exceptionNomeGrupo.getRepassaUtilError());

        groupDto.setGroup("");
        RepassaException exceptionNomeGrupoCurto = assertThrows(RepassaException.class, () -> groupService.validGroupStrings(groupDto));
        assertEquals(AccountError.NOME_GRUPO_INVALIDO, exceptionNomeGrupoCurto.getRepassaUtilError());


        groupDto.setGroup("TestGroup");
        groupDto.setDescription("A".repeat(256));
        RepassaException exceptionDescricaoGrupo = assertThrows(RepassaException.class, () -> groupService.validGroupStrings(groupDto));
        assertEquals(AccountError.DESCRICAO_GRUPO_INVALIDO, exceptionDescricaoGrupo.getRepassaUtilError());
    }

    @Test
    void testValidGroupNew() {

        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);

        GroupsResource groupsResourceMock = mock(GroupsResource.class);
        when(realmResourceMock.groups()).thenReturn(groupsResourceMock);

        GroupDto groupDto = new GroupDto();
        groupDto.setGroup("TestGroup");

        assertDoesNotThrow(() -> groupService.validGroupNew(groupDto));

        groupDto.setGroup(null);
        RepassaException exceptionNomeGrupoNulo = assertThrows(RepassaException.class, () -> groupService.validGroupNew(groupDto));
        assertEquals(AccountError.NOME_GRUPO_NULO, exceptionNomeGrupoNulo.getRepassaUtilError());

    }

    @Test
    public void mustFindPermissionGroup() throws RepassaException {
        String idGroup = "d4362349-3834-4b77-b467-85deafb694ae";

        PermissionDTO permissionDTO = PermissionDTO.builder()
                .name("RECEBIMENTO.CONSULTAR_SACOLAS_RECEBIDAS").description("Consultar sacolas recebidas").permission(false).build();

        List<String> groupRolesList = List.of(permissionDTO.getName());
        if(groupRolesList.get(0).equals(permissionDTO.getName()))
            permissionDTO.setPermission(true);

        RoleRepresentation roles = new RoleRepresentation();
        roles.setName(permissionDTO.getName());
        roles.setDescription(permissionDTO.getDescription());
        List<RoleRepresentation> rolesList = new ArrayList<>();
        rolesList.add(roles);

        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);
        when(realmResourceMock.groups()).thenReturn(groupsResourceMock);
        when(groupsResourceMock.group(anyString())).thenReturn(groupResourceMock);
        when(groupsResourceMock.group(anyString()).toRepresentation()).thenReturn(groupRepresentation);
        when(keycloakConfig.getRealmResource().roles()).thenReturn(rolesResource);
        when(rolesResource.list()).thenReturn(rolesList);
        when(groupRepresentation.getRealmRoles()).thenReturn(groupRolesList);

        List< CategoryPermissionDTO> categoryList = groupService.getPermissionGroup(idGroup);

        assertEquals(rolesList.get(0).getName(), categoryList.get(0).getPermissions().get(0).getName());
        assertEquals(rolesList.get(0).getDescription(), categoryList.get(0).getPermissions().get(0).getDescription());
        assertEquals(true, permissionDTO.getPermission());
    }

}
