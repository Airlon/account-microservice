package br.com.repassa.resource;

import br.com.backoffice_repassa_utils_lib.error.exception.RepassaException;
import br.com.repassa.service.ClientService;
import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.security.*;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/v1/realm/clients")
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
@Tag(name = "API Keycloak - client", description = "Necessário acesso de admin")
public class ClientResource {

    @Inject
    ClientService clientService;

    @GET
    @RolesAllowed({"admin"})
    @Path("/{id}/roles")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Regras de um client keycloak", description = "Obter a lista com todas as regras de um client.")
    @SecurityRequirement(name = "adminAuth", scopes = {"admin"})
    public Response getAllRoles(@PathParam("id") String idClient) throws RepassaException {
        return clientService.getAllRolesByClient(idClient);
    }

    @GET
    @RolesAllowed({"admin"})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Clients do keycloak", description = "Obter a lista com todos os clientes.")
    @SecurityRequirement(name = "adminAuth", scopes = {"admin"})
    public Response getClient() throws RepassaException {
        return clientService.getClients();
    }

    @GET
    @RolesAllowed({"admin"})
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Client do keycloak", description = "Obter a um client filtrado pelo ID.")
    @SecurityRequirement(name = "adminAuth", scopes = {"admin"})
    public Response getClient(@PathParam("id") String idClient) throws RepassaException {
        return clientService.getClient(idClient);
    }

}
