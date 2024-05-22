package br.com.repassa.resource;

import br.com.backoffice_repassa_utils_lib.error.exception.RepassaException;
import br.com.repassa.dto.GroupDto;
import br.com.repassa.dto.KeycloakAssociacaoDTO;
import br.com.repassa.exception.AccountError;
import br.com.repassa.service.GroupService;
import br.com.repassa.service.RoleService;
import io.quarkus.security.Authenticated;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlow;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlows;
import org.eclipse.microprofile.openapi.annotations.security.OAuthScope;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/api/v1/realm/groups")
@Authenticated
@SecurityScheme(
        securitySchemeName = "adminAuth",
        type = SecuritySchemeType.OAUTH2,
        flows = @OAuthFlows(
                authorizationCode = @OAuthFlow(
                        authorizationUrl = "http://localhost:8180/auth/realms/quarkus/protocol/openid-connect/auth",
                        tokenUrl = "http://localhost:8180/auth/realms/quarkus/protocol/openid-connect/token",
                        scopes = {
                                @OAuthScope(name = "admin", description = "admin access"),
                                @OAuthScope(name = "openid", description = "openid scope")
                        }
                )
        )
)
@Tag(name = "API Keycloak - group", description = "Necessário acesso de admin")
@Slf4j
public class GroupResource {

    @Inject
    GroupService groupService;

    @Inject
    RoleService roleService;

    @GET
    @RolesAllowed({"admin"})
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Grupo no keycloak", description = "Obter um determinado grupo.")
    @SecurityRequirement(name = "adminAuth", scopes = {"admin"})
    public Response getFindByGroup(@PathParam("id") String idGroup) throws RepassaException {
        log.info("Buscando grupo no keycloak por ID:{}", idGroup);
        return groupService.findByGroup(idGroup);
    }

    @GET
    @RolesAllowed({"admin"})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Todos os grupos do keycloak", description = "Obter a lista com todos os grupos.")
    @SecurityRequirement(name = "adminAuth", scopes = {"admin"})
    public Response getAllGroups() {
        log.info("Buscando todos os grupos no keycloak");
        return groupService.findAllGroups();
    }

    @GET
    @RolesAllowed({"admin"})
    @Path("/statistics")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Estatisticas dos grupos do keycloak", description = "Obter a lista com todos os grupos e suas estatisticas.")
    @SecurityRequirement(name = "adminAuth", scopes = {"admin"})
    public Response getStatisticsGroups() throws RepassaException {
        log.info("Buscando todos os grupos e suas estatisticas no keycloak");
        return groupService.findGroupStatistics();
    }

    @DELETE
    @RolesAllowed({"admin"})
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Remover um grupo no keycloak", description = "Remover um determinado grupo.")
    @SecurityRequirement(name = "adminAuth", scopes = {"admin"})
    public Response removeGroup(@PathParam("id") String idGroup) throws RepassaException {
        log.info("Removendo grupo pelo ID:{} no keycloak", idGroup);
        return groupService.deleteGroup(idGroup);
    }


    @POST
    @RolesAllowed({"admin"})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Criar um grupo no keycloak", description = "Criar um determinado grupo.")
    @SecurityRequirement(name = "adminAuth", scopes = {"admin"})
    public Response createGroup(@RequestBody GroupDto dto) throws RepassaException {
        validateGroup(dto);
        return groupService.createGroup(dto);
    }

    @PUT
    @RolesAllowed({"admin"})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @Operation(summary = "Atualizar um grupo no keycloak", description = "Atualizar um determinado grupo.")
    @SecurityRequirement(name = "adminAuth", scopes = {"admin"})
    public Response updateGroup(@PathParam("id") String idGroup, @RequestBody GroupDto dto) throws RepassaException {
        log.info("Atualizando grupo no keycloak. groupID:{}", idGroup);
        validateGroup(dto);
        return groupService.updateGroup(idGroup, dto);
    }

    @POST
    @RolesAllowed({"admin"})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{idGroup}/roles-realm/add")
    @Operation(summary = "Adicionar roles de nível realm a um grupo", description = "Passando uma lista de nomes de roles, elas serão adicionadas ao grupo que é identificado pelo seu ID na requisição.")
    @SecurityRequirement(name = "adminAuth", scopes = {"admin"})
    public Response addRoleRealmGroup(@PathParam("idGroup") String idGroup, @RequestBody List<String> rolesName) throws RepassaException {
        return roleService.addRoleRealmToGroup(idGroup, rolesName);
    }

    @PUT
    @RolesAllowed({"admin"})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{idGroup}/roles-realm/delete")
    @Operation(summary = "Remover roles de nível realm a um grupo", description = "Passando uma lista de nomes de roles, elas serão removidas do grupo que é identificado pelo seu ID na requisição.")
    @SecurityRequirement(name = "adminAuth", scopes = {"admin"})
    public Response removeRoleRealmGroup(@PathParam("idGroup") String idGroup, @RequestBody List<String> rolesName) throws RepassaException {
        return roleService.removeRoleRealmToGroup(idGroup, rolesName);
    }

    @POST
    @RolesAllowed({"admin"})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{idGroup}/roles-client/add")
    @Operation(summary = "Adicionar uma associação entre um grupo e roles de um client", description = "Passando um objeto com o id do client e uma lista com o nome das roles, essas roles serão adicionadas no grupo identificado pelo ID na requisição. Vale ressaltar que o valor que identifica o client é o ID e não ClientId.")
    @SecurityRequirement(name = "adminAuth", scopes = {"admin"})
    public Response addRoleClientGroup(@PathParam("idGroup") String idGroup, @RequestBody KeycloakAssociacaoDTO dto) throws RepassaException {
        return roleService.addRoleClientToGroup(idGroup, dto.getIdClient(), dto.getRoles());

    }

    @PUT
    @RolesAllowed({"admin"})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{idGroup}/roles-client/delete")
    @Operation(summary = "Remover uma associação entre um grupo e roles de um client", description = "Passando um objeto com o id do client e uma lista com o nome das roles, essas roles serão desassociadas do grupo identificado pelo ID na requisição. Vale ressaltar que o valor que identifica o client é o ID e não ClientId.")
    @SecurityRequirement(name = "adminAuth", scopes = {"admin"})
    public Response removeRoleClientGroup(@PathParam("idGroup") String idGroup, @RequestBody KeycloakAssociacaoDTO dto) throws RepassaException {
        return roleService.removeRoleClientToGroup(idGroup, dto.getIdClient(), dto.getRoles());
    }

    @GET
    @RolesAllowed({"admin"})
    @Path("/{id}/members")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Buscar os usuários por Grupo", description = "Obter os membros de um determinado grupo.")
    @SecurityRequirement(name = "adminAuth", scopes = {"admin"})
    public Response getMembersByGroup(@PathParam("id") String idGroup) throws RepassaException {
        return groupService.getUserByGroup(idGroup);
    }

    private void validateGroup(GroupDto dto) throws RepassaException {
        log.info("Validando se o grupo possui caractere especial ou nao esta nulo.");
        if (dto.getGroup() != null && !StringUtils.isAlphanumericSpace(dto.getGroup())) {
            throw new RepassaException(AccountError.GRUPO_COM_CARACTERES_INVALIDOS);
        }
    }

    @GET
    @RolesAllowed({"admin"})
    @Path("/permission/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Buscar roles do realm e de um grupo.", description = "Obter a lista de roles e verificar quais estão habilitadas/desabilitadas no grupo.")
    @SecurityRequirement(name = "adminAuth", scopes = {"admin"})
    public Response getPermissionByGroupId(@PathParam("id") String idGroup) throws RepassaException{
        return Response.ok(groupService.getPermissionGroup(idGroup)).build();
    }
}
