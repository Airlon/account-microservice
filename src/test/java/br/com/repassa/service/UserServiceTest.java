package br.com.repassa.service;

import br.com.backoffice_repassa_utils_lib.error.exception.RepassaException;
import br.com.repassa.dto.UserToGroupDTO;
import br.com.repassa.resource.client.KeycloakConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private KeycloakConfig keycloakConfig;

    @Mock
    RealmResource realmResourceMock;

    @Mock
    GroupsResource groupsResourceMock;

    @Mock
    GroupResource groupResourceMock;

    @Mock
    UsersResource usersResourceMock;

    @Mock
    UserResource userResourceMock;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void mustReturnStatusOkWhenFindUsersByFilter() throws JsonProcessingException, RepassaException {
        List<UserRepresentation> mockUserList = new ArrayList<>();
        UserRepresentation userRep = mockUserRepresentation();
        mockUserList.add(userRep);

        ObjectMapper mapper = new ObjectMapper();
        String usersString = mapper.writeValueAsString(mockUserList);

        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.searchByUsername(anyString(), anyBoolean())).thenReturn(mockUserList);

        Response response = userService.findUsersByFilter("filter");

        assertEquals(Response.ok(usersString).build().getEntity(), response.getEntity());
    }

    @Test
    void mustReturnRepassaExceptionWhenFindUsersByFilter() throws RepassaException {
        Assertions.assertThrows(RepassaException.class, () -> userService.findUsersByFilter("fi"));

    }

    @Test
    void mustReturnStatusNoContentWhenFindUsersByFilter() throws RepassaException {
        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);

        Response response = userService.findUsersByFilter("filter");

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    void mustReturnStatusOkWithoutWarningsWhenAddUsersToGroup() throws RepassaException {
        List<String> users = new ArrayList<>(Arrays.asList("user1", "user2"));
        UserToGroupDTO userToGroupDTO = new UserToGroupDTO(users);

        List<UserRepresentation> mockUserList = new ArrayList<>();
        UserRepresentation userRep = mockUserRepresentation();
        mockUserList.add(userRep);

        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);
        when(realmResourceMock.groups()).thenReturn(groupsResourceMock);
        when(groupsResourceMock.group(anyString())).thenReturn(groupResourceMock);
        when(groupsResourceMock.group(anyString()).toRepresentation()).thenReturn(new GroupRepresentation());
        when(groupsResourceMock.group(anyString()).members()).thenReturn(mockUserList);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.get(anyString())).thenReturn(userResourceMock);

        Response response = userService.addUsersToGroup("groupId", userToGroupDTO);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void mustReturnStatusOkWithWarningsWhenAddUsersToGroup() throws RepassaException {
        List<String> users = new ArrayList<>(Arrays.asList("12345", "user4"));
        UserToGroupDTO userToGroupDTO = new UserToGroupDTO(users);

        List<UserRepresentation> mockUserList = new ArrayList<>();
        UserRepresentation userRep = mockUserRepresentation();
        mockUserList.add(userRep);

        List<String> warnings = new ArrayList<>();
        warnings.add("Usuário Joao das Neves já pertence ao grupo");
        warnings.add("Usuário com userId: user4 não foi encontrado no keycloak");

        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);
        when(realmResourceMock.groups()).thenReturn(groupsResourceMock);
        when(groupsResourceMock.group(anyString())).thenReturn(groupResourceMock);
        when(groupsResourceMock.group(anyString()).toRepresentation()).thenReturn(new GroupRepresentation());
        when(groupsResourceMock.group(anyString()).members()).thenReturn(mockUserList);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.get(anyString())).thenThrow(NotFoundException.class);

        Response response = userService.addUsersToGroup("groupId", userToGroupDTO);

        assertEquals(Response.ok(warnings).build().getEntity(), response.getEntity());
    }

    @Test
    void mustReturnStatusOkWithoutWarningsWhenRemoveUsersFromGroup() throws RepassaException {
        List<String> users = new ArrayList<>(Arrays.asList("12345"));
        UserToGroupDTO userToGroupDTO = new UserToGroupDTO(users);

        List<UserRepresentation> mockUserList = new ArrayList<>();
        UserRepresentation userRep = mockUserRepresentation();
        mockUserList.add(userRep);

        List<String> warnings = new ArrayList<>();

        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);
        when(realmResourceMock.groups()).thenReturn(groupsResourceMock);
        when(groupsResourceMock.group(anyString())).thenReturn(groupResourceMock);
        when(groupsResourceMock.group(anyString()).toRepresentation()).thenReturn(new GroupRepresentation());
        when(groupsResourceMock.group(anyString()).members()).thenReturn(mockUserList);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.get(anyString())).thenReturn(userResourceMock);
        warnings.add("Usuário com userId: 12345 foi removido do grupo.");
        Response response = userService.removeUsersFromGroup("groupId", userToGroupDTO);

        assertEquals(Response.ok(warnings).build().getEntity(), response.getEntity());
    }

    @Test
    void mustReturnStatusOkWithWarningsWhenRemoveUsersFromGroup() throws RepassaException {
        List<String> users = new ArrayList<>(Arrays.asList("12345", "user7"));
        UserToGroupDTO userToGroupDTO = new UserToGroupDTO(users);

        List<UserRepresentation> mockUserList = new ArrayList<>();
        UserRepresentation userRep = mockUserRepresentation();
        mockUserList.add(userRep);

        List<String> warnings = new ArrayList<>();
        warnings.add("Usuário com userId: 12345 não foi encontrado");
        warnings.add("Usuário user7 não pertence ao grupo");


        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);
        when(realmResourceMock.groups()).thenReturn(groupsResourceMock);
        when(groupsResourceMock.group(anyString())).thenReturn(groupResourceMock);
        when(groupsResourceMock.group(anyString()).toRepresentation()).thenReturn(new GroupRepresentation());
        when(groupsResourceMock.group(anyString()).members()).thenReturn(mockUserList);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.get(anyString())).thenThrow(NotFoundException.class);

        Response response = userService.removeUsersFromGroup("groupId", userToGroupDTO);

        assertEquals(Response.ok(warnings).build().getEntity(), response.getEntity());
    }

    UserRepresentation mockUserRepresentation() {
        UserRepresentation userRep = new UserRepresentation();
        userRep.setId("12345");
        userRep.setUsername("joaoneves");
        userRep.setFirstName("Joao");
        userRep.setLastName("das Neves");
        userRep.setEmail("joaodasneves@gmail.com");
        return userRep;
    }
}
