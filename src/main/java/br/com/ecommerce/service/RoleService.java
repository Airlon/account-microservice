package br.com.repassa.service;

import br.com.backoffice_repassa_utils_lib.error.exception.RepassaException;
import br.com.repassa.dto.RoleKeycloakDTO;
import br.com.repassa.exception.AccountError;
import br.com.repassa.resource.client.KeycloakConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class RoleService {

    @Inject
    KeycloakConfig keycloak;

    public boolean roleExists(String roleName) {
        List<RoleRepresentation> roles = keycloak.getRealmResource().roles().list();
        for (RoleRepresentation role : roles) {
            if (role.getName().equals(roleName)) {
                return true;
            }
        }
        return false;
    }


    private RoleResource getRoleResource(String roleName) throws RepassaException {
        RoleResource roleResource = keycloak.getRealmResource().roles().get(roleName);
        if (roleResource == null) {
            throw new RepassaException(AccountError.REGRA_NAO_ENCONTRADA);
        }
        return roleResource;
    }

    private RoleRepresentation getRoleDeList(List<RoleRepresentation> roles, String roleName) throws RepassaException {
        for (RoleRepresentation role : roles) {
            if (role.getName().equals(roleName)) {
                return role;
            }
        }

        throw new RepassaException(AccountError.REGRA_NAO_ENCONTRADA);
    }

    private ClientResource getClientResource(String clientId) throws RepassaException {
        ClientResource clientResource = keycloak.getRealmResource().clients().get(clientId);

        if (clientResource == null) {
            throw new RepassaException(AccountError.CLIENTE_NAO_ENCONTRADO);
        }
        return clientResource;
    }


    public Response addRoleRealmToGroup(String groupId, List<String> roleNames) throws RepassaException {
        GroupResource groupResource = keycloak.getRealmResource().groups().group(groupId);

        List<RoleRepresentation> rolesRepresentation = groupResource.roles().realmLevel().listAll();

        for (String roleName : roleNames) {
            RoleResource roleResource = getRoleResource(roleName);
            rolesRepresentation.add(roleResource.toRepresentation());
        }

        groupResource.roles().realmLevel().add(rolesRepresentation);
        GroupRepresentation groupRepresentation = keycloak.getRealmResource().groups().group(groupId).toRepresentation();
        return Response.ok(groupRepresentation).build();
    }

    public Response removeRoleRealmToGroup(String groupId, List<String> roleNames) throws RepassaException {
        GroupResource groupResource = keycloak.getRealmResource().groups().group(groupId);
        List<RoleRepresentation> rolesRepresentation = new ArrayList<>();

        for (String roleName : roleNames) {
            RoleResource roleResource = getRoleResource(roleName);
            rolesRepresentation.add(roleResource.toRepresentation());
        }

        groupResource.roles().realmLevel().remove(rolesRepresentation);
        GroupRepresentation groupRepresentation = keycloak.getRealmResource().groups().group(groupId).toRepresentation();
        return Response.ok(groupRepresentation).build();

    }

    public Response addRoleClientToGroup(String groupId, String clientId, List<String> roleNames) throws RepassaException {
        GroupResource groupResource = keycloak.getRealmResource().groups().group(groupId);
        ClientResource clientResource = getClientResource(clientId);
        List<RoleRepresentation> rolesRepresentation = clientResource.roles().list();

        for (String roleName : roleNames) {
            rolesRepresentation.add(getRoleDeList(rolesRepresentation, roleName));
            groupResource.roles().clientLevel(clientResource.toRepresentation().getId()).add(rolesRepresentation);
        }

        GroupRepresentation groupRepresentation = keycloak.getRealmResource().groups().group(groupId).toRepresentation();
        return Response.ok(groupRepresentation).build();
    }

    public Response removeRoleClientToGroup(String groupId, String clientId, List<String> roleNames) throws RepassaException {
        GroupResource groupResource = keycloak.getRealmResource().groups().group(groupId);
        ClientResource clientResource = getClientResource(clientId);
        List<RoleRepresentation> rolesRepresentation = groupResource.roles().clientLevel(clientResource.toRepresentation().getId()).listAll();
        List<RoleRepresentation> rolesRemove = new ArrayList<>();

        for (String roleName : roleNames) {
            rolesRemove.add(getRoleDeList(rolesRepresentation, roleName));
        }

        groupResource.roles().clientLevel(clientResource.toRepresentation().getId()).remove(rolesRemove);
        GroupRepresentation groupRepresentation = keycloak.getRealmResource().groups().group(groupId).toRepresentation();
        return Response.ok(groupRepresentation).build();

    }

    public RoleRepresentation create(RoleKeycloakDTO dto) throws RepassaException {
        if (roleExists(dto.getName())) {
            throw new RepassaException(AccountError.REGRA_NOME_REPETIDO);
        }

        RoleRepresentation role = new RoleRepresentation();
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
        role.setComposite(false);

        keycloak.getRealmResource().roles().create(role);
        return role;
    }

    public RoleRepresentation update(String roleName, RoleKeycloakDTO role) throws RepassaException {
        RoleResource roleResource = keycloak.getRealmResource().roles().get(roleName);
        if (roleResource == null) {
            throw new RepassaException(AccountError.REGRA_NAO_ENCONTRADA);
        }

        if (roleExists(role.getName()) && !roleName.equals(role.getName())) {
            throw new RepassaException(AccountError.REGRA_NOME_REPETIDO);
        }

        RoleRepresentation updatedRole = roleResource.toRepresentation();
        updatedRole.setName(role.getName());
        updatedRole.setDescription(role.getDescription());
        roleResource.update(updatedRole);
        return updatedRole;
    }

    public Response delete(String roleId) throws RepassaException {
        RoleResource roleResource = keycloak.getRealmResource().roles().get(roleId);
        if (roleResource == null) {
            throw new RepassaException(AccountError.REGRA_NAO_ENCONTRADA);
        }
        String roleName = roleResource.toRepresentation().getName();
        roleResource.remove();
        return Response.ok(roleName).build();
    }

    public Response getById(String roleId) throws JsonProcessingException, RepassaException {
        RoleResource roleResource = keycloak.getRealmResource().roles().get(roleId);
        if (roleResource == null) {
            throw new RepassaException(AccountError.REGRA_NAO_ENCONTRADA);
        }
        List<RoleRepresentation> roles = keycloak.getRealmResource().roles().list().stream().filter(role -> role.getName().equals(roleId)).toList();

        if (!roles.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            return Response.ok(objectMapper.writeValueAsString(roles)).build();
        }

        return Response.noContent().build();
    }

    public Response getAll() throws JsonProcessingException {
        List<RoleRepresentation> roles = keycloak.getRealmResource().roles().list();

        if (!roles.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(roles);
            return Response.ok(json).build();
        }

        return Response.noContent().build();
    }

}
