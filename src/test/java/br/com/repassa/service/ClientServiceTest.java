package br.com.repassa.service;

import br.com.backoffice_repassa_utils_lib.error.exception.RepassaException;
import br.com.repassa.exception.AccountError;
import br.com.repassa.resource.client.KeycloakConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ClientServiceTest {

    @Mock
    private KeycloakConfig keycloakConfig;

    @InjectMocks
    private ClientService clientService;

    @Mock
    RealmResource realmResourceMock;

    @Mock
    AccountError accountError;

    public ClientServiceTest() {
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllRolesByClient() throws Exception {
        String clientId = "yourClientId";

        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);

        ClientsResource clientsResourceMock = mock(ClientsResource.class);
        when(realmResourceMock.clients()).thenReturn(clientsResourceMock);

        ClientResource clientResource = mock(ClientResource.class);
        when(clientsResourceMock.get(anyString())).thenReturn(clientResource);

        RolesResource rolesResourceMock = mock(RolesResource.class);
        when(clientResource.roles()).thenReturn(rolesResourceMock);

        String groupName = "TestGroup";
        GroupRepresentation group = new GroupRepresentation();
        group.setName(groupName);

        List<ClientRepresentation> clientsList = Collections.singletonList(new ClientRepresentation());
        when(clientsResourceMock.findAll()).thenReturn(clientsList);

        List<RoleRepresentation> rolesList = Collections.singletonList(new RoleRepresentation());
        when(rolesResourceMock.list()).thenReturn(rolesList);

        Response response = clientService.getAllRolesByClient(clientId);

        assertEquals(200, response.getStatus());

        ObjectMapper objectMapper = new ObjectMapper();
        String expectedJson = objectMapper.writeValueAsString(rolesList);
        assertEquals(expectedJson, response.getEntity());

        verify(keycloakConfig.getRealmResource(), times(1)).clients();
        verify(clientsResourceMock, times(1)).get(clientId);
        verify(rolesResourceMock, times(1)).list();
    }

    @Test
    void testGetAllRolesByClientJsonProcessingException() throws Exception {
        String clientId = "yourClientId";

        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);

        ClientsResource clientsResourceMock = mock(ClientsResource.class);
        when(realmResourceMock.clients()).thenReturn(clientsResourceMock);

        ClientResource clientResourceMock = mock(ClientResource.class);
        when(clientsResourceMock.get(clientId)).thenReturn(clientResourceMock);

        RolesResource rolesResourceMock = mock(RolesResource.class);
        when(clientResourceMock.roles()).thenReturn(rolesResourceMock);

        doAnswer(invocation -> {
            throw new JsonProcessingException("Simulated JsonProcessingException") {};
        }).when(rolesResourceMock).list();

        RepassaException exception = assertThrows(RepassaException.class, () -> {
            clientService.getAllRolesByClient(clientId);
        });

        assertEquals(AccountError.JSON_GERADO_NAO_VALIDO, exception.getRepassaUtilError());
    }

    @Test
    void testGetClients() throws RepassaException, JsonProcessingException {

        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);

        ClientsResource clientsResourceMock = mock(ClientsResource.class);
        when(realmResourceMock.clients()).thenReturn(clientsResourceMock);

        List<ClientRepresentation> clientsList = Collections.singletonList(new ClientRepresentation());
        when(clientsResourceMock.findAll()).thenReturn(clientsList);

        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);
        when(realmResourceMock.clients()).thenReturn(clientsResourceMock);

        Response response = clientService.getClients();

        assertEquals(200, response.getStatus());

        ObjectMapper objectMapper = new ObjectMapper();
        String expectedJson = objectMapper.writeValueAsString(clientsList);
        assertEquals(expectedJson, response.getEntity());
    }

    @Test
    void testGetClientsRepassaException() throws RepassaException, IOException {
        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);

        ClientsResource clientsResourceMock = mock(ClientsResource.class);
        when(realmResourceMock.clients()).thenReturn(clientsResourceMock);

        doAnswer(invocation -> {
            throw new RepassaException(AccountError.JSON_GERADO_NAO_VALIDO);
        }).when(clientsResourceMock).findAll();

        RepassaException exception = assertThrows(RepassaException.class, () -> {
            clientService.getClients();
        });

        assertEquals(AccountError.JSON_GERADO_NAO_VALIDO, exception.getRepassaUtilError());
    }

    @Test
    void testGetClientssIdsNames() {
        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);

        ClientsResource clientsResourceMock = mock(ClientsResource.class);
        when(realmResourceMock.clients()).thenReturn(clientsResourceMock);

        List<ClientRepresentation> simulatedClients = new ArrayList<>();

        ClientRepresentation client1 = new ClientRepresentation();
        client1.setId("client1");
        client1.setClientId("Client 1");
        client1.setName("Client 1");

        ClientRepresentation client2 = new ClientRepresentation();
        client2.setId("client2");
        client2.setClientId("Client 2");
        client2.setName("client2");

        simulatedClients.add(client1);
        simulatedClients.add(client2);

        when(clientsResourceMock.findAll()).thenReturn(simulatedClients);

        Map<String, String> result = clientService.getClientsIdsNames();

        Map<String, String> expected = new HashMap<>();
        expected.put("client1", "Client 1");
        expected.put("client2", "Client 2");

        assertEquals(expected, result);
    }

    @Test
    void testGetClient() throws Exception {
        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);

        ClientsResource clientsResourceMock = mock(ClientsResource.class);
        when(realmResourceMock.clients()).thenReturn(clientsResourceMock);

        List<ClientRepresentation> clientsList = new ArrayList<>();

        ClientRepresentation client1 = new ClientRepresentation();
        client1.setId("client1");
        client1.setClientId("Client 1");

        clientsList.add(client1);

        when(clientsResourceMock.findAll()).thenReturn(clientsList);

        String validClientId = "client1";
        Response response = clientService.getClient(validClientId);

        assertEquals(200, response.getStatus());

        ObjectMapper objectMapper = new ObjectMapper();
        String expectedJson = objectMapper.writeValueAsString(client1);

        assertEquals(expectedJson, response.getEntity());

        String invalidClientId = "invalidClient";
        Response invalidResponse = clientService.getClient(invalidClientId);

        assertEquals(204, invalidResponse.getStatus());
    }

    @Test
    void testGetClientRepassaException() throws RepassaException, IOException {
        when(keycloakConfig.getRealmResource()).thenReturn(realmResourceMock);

        ClientsResource clientsResourceMock = mock(ClientsResource.class);
        when(realmResourceMock.clients()).thenReturn(clientsResourceMock);

        List<ClientRepresentation> simulatedClients = new ArrayList<>();

        ClientRepresentation client1 = new ClientRepresentation();
        client1.setId("client1");
        client1.setClientId("Client 1");
        client1.setName("Client 1");

        simulatedClients.add(client1);

        doAnswer(invocation -> {
            throw new RepassaException(AccountError.JSON_GERADO_NAO_VALIDO);
        }).when(clientsResourceMock).findAll();

        RepassaException exception = assertThrows(RepassaException.class, () -> {
            clientService.getClient("client1");
        });

        assertEquals(AccountError.JSON_GERADO_NAO_VALIDO, exception.getRepassaUtilError());
    }
}
