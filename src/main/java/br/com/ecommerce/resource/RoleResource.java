package br.com.repassa.resource;

import br.com.backoffice_repassa_utils_lib.error.exception.RepassaException;
import br.com.repassa.dto.RoleKeycloakDTO;
import br.com.repassa.service.RoleService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.security.*;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

@Path("/api/v1/realm/roles")
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
@Tag(name = "API Keycloak - roles", description = "Necessário acesso de admin")
public class RoleResource {

    @Inject
    RoleService roleService;

    @GET
    @RolesAllowed({"admin"})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Buscar todas as roles do realm", description = "Obter a lista com todos as roles.")
    @SecurityRequirement(name = "adminAuth", scopes = {"admin"})
    public Response getRoles() throws JsonProcessingException {
        return roleService.getAll();
    }

    @GET
    @RolesAllowed({"admin"})
    @Path("/{roleName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Buscar roles do realm a partir de um nome.", description = "Obter a role filtrada por um nome.")
    @SecurityRequirement(name = "adminAuth", scopes = {"admin"})
    public Response getRoleByname(@PathParam("roleName") String roleName) throws RepassaException, JsonProcessingException {
        return roleService.getById(roleName);
    }

    @POST
    @RolesAllowed({"admin"})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Criar role de um realm", description = "Passar nome e descrição, assim será criada uma role nova.")
    @SecurityRequirement(name = "adminAuth", scopes = {"admin"})
    public Response newRole(RoleKeycloakDTO roleKeycloakDTO) throws RepassaException {
        URI uri = UriBuilder.fromPath("/{name}").build(roleService.create(roleKeycloakDTO).getName());
        return Response.created(uri).build();
    }

    @PUT
    @RolesAllowed({"admin"})
    @Path("/{roleName}")
    @Operation(summary = "Atualizar role de um realm", description = "Atualizar uma role.")
    @SecurityRequirement(name = "adminAuth", scopes = {"admin"})
    public Response updateRole(@PathParam("roleName") String roleName, RoleKeycloakDTO roleDTO) throws RepassaException {
        return Response.ok(roleService.update(roleName, roleDTO)).build();
    }

    @DELETE
    @RolesAllowed({"admin"})
    @Path("/{roleName}")
    @Operation(summary = "Remover role de um realm", description = "Passar o nome e essa role será entao deletada.")
    @SecurityRequirement(name = "adminAuth", scopes = {"admin"})
    public Response deleteRole(@PathParam("roleName") String nome) throws RepassaException {
        roleService.delete(nome);
        return Response.ok().build();
    }

}
