package br.com.repassa.resource;

import br.com.backoffice_repassa_utils_lib.error.exception.RepassaException;
import br.com.repassa.dto.UserToGroupDTO;
import br.com.repassa.service.UserService;
import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.security.*;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/v1/realm/users")
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
@Tag(name = "API Keycloak - user", description = "Necessário acesso de admin")
public class UserResource {
    @Inject
    UserService userService;

    @GET
    @Path("outgroup/{groupId}")
    @RolesAllowed({"admin"})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Buscar todos os usuários e o grupo por ID e validar quais nao se encontram no grupo",
            description = "Obter a lista dos os usuários que nao estao em um determinado grupo ")
    @SecurityRequirement(name = "adminAuth", scopes = {"admin"})
    public Response getUsersOutOfGroup(String groupId) {
        return userService.findUsersOutOfGroup(groupId);
    }
    @GET
    @Path("/{filter}")
    @RolesAllowed({"admin"})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Buscar usuários por filtro", description = "Obter a lista de usuários por filtro (tipo like) " +
            "de Username, First name, Last name ou Email do Keycloak")
    @SecurityRequirement(name = "adminAuth", scopes = {"admin"})
    public Response getUsersByFilter(@PathParam("filter") String filter) throws RepassaException {
        return userService.findUsersByFilter(filter);
    }

    @PUT
    @Path("/{groupId}")
    @RolesAllowed({"admin"})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Adicionar usuário(s) de um grupo", description = "Atualização para adicionar um ou mais " +
            "usuários em um grupo")
    @SecurityRequirement(name = "adminAuth", scopes = {"admin"})
    public Response addUserToGroup(@PathParam("groupId") String id, @Valid @RequestBody UserToGroupDTO user) throws RepassaException {
        return userService.addUsersToGroup(id, user);
    }

    @PUT
    @Path("/removegroup/{groupId}")
    @RolesAllowed({"admin"})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Remover usuário(s) de um grupo", description = "Atualização para remover um ou mais " +
            "usuários de um grupo")
    @SecurityRequirement(name = "adminAuth", scopes = {"admin"})
    public Response removeUserFromGroup(@PathParam("groupId") String id, @Valid @RequestBody UserToGroupDTO user) throws RepassaException {
        return userService.removeUsersFromGroup(id, user);
    }
}
