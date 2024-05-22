package br.com.repassa.service;

import br.com.backoffice_repassa_utils_lib.error.exception.RepassaException;
import br.com.repassa.exception.AccountError;
import br.com.repassa.resource.client.KeycloakConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ClientService {

    @Inject
    KeycloakConfig keycloak;

    public Response getAllRolesByClient(String id) throws RepassaException {
        ClientResource clientResource = keycloak.getRealmResource().clients().get(id);

        try{
            ObjectMapper objectMapper = new ObjectMapper();
            List<RoleRepresentation> roles = clientResource.roles().list();
            String json = objectMapper.writeValueAsString(roles);
            return Response.ok(json).build();
        }catch (JsonProcessingException exception){
            throw new RepassaException(AccountError.JSON_GERADO_NAO_VALIDO);
        }

    }

    public Response getClients() throws RepassaException {
        List<ClientRepresentation> clients = keycloak.getRealmResource().clients().findAll();

        try {
            ObjectMapper mapper = new ObjectMapper();
            String clientsString = mapper.writeValueAsString(clients);
            return Response.ok(clientsString).build();
        } catch (IOException e) {
            throw new RepassaException(AccountError.JSON_GERADO_NAO_VALIDO);
        }
        
    }

    public Map<String,String> getClientsIdsNames(){
        List<ClientRepresentation> clients = keycloak.getRealmResource().clients().findAll();
        Map<String, String> clientsIdNames = new HashMap<>();
        for(ClientRepresentation clientRepresentation : clients)
            clientsIdNames.put(clientRepresentation.getId(), clientRepresentation.getClientId());
        return clientsIdNames;
    }

    public Response getClient(String clientId) throws RepassaException {
        List<ClientRepresentation> clients = keycloak.getRealmResource().clients().findAll();

        for(ClientRepresentation clientRepresentation : clients){
            if(clientRepresentation.getId().equals(clientId)){
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    String clientsString = mapper.writeValueAsString(clientRepresentation);
                    return Response.ok(clientsString).build();
                } catch (IOException e) {
                    throw new RepassaException(AccountError.JSON_GERADO_NAO_VALIDO);
                }
            }
        }
        return Response.noContent().build();
    }

}
