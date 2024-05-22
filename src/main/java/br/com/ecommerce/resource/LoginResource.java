package br.com.repassa.resource;

import br.com.repassa.dto.KeycloakAccessDTO;
import br.com.repassa.resource.client.KeycloakService;
import br.com.repassa.service.LoginService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("/api/v1")
@Tag(name = "API Keycloak", description = "Necessário acesso de admin")
public class LoginResource {

    @RestClient
    KeycloakService keycloakService;

    @Inject
    LoginService loginService;

    @POST
    @Path("/{realm}/token")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Endpoint de obtenção do token de acesso", description = "Obter token de acesso do código passado.")
    public Response authenticates(@PathParam("realm") String realm, KeycloakAccessDTO keycloakAccessDTO) {
        MultivaluedMap<String, String> bodyParam = new MultivaluedHashMap<>();

        bodyParam.add("client_id", keycloakAccessDTO.getIdClient());
        bodyParam.add("grant_type", keycloakAccessDTO.getGrantType());
        bodyParam.add("client_secret", keycloakAccessDTO.getClientSecret());
        bodyParam.add("code", keycloakAccessDTO.getCode());
        bodyParam.add("redirect_uri", keycloakAccessDTO.getRedirectUri());
        bodyParam.add("username", keycloakAccessDTO.getUsername());
        bodyParam.add("password", keycloakAccessDTO.getPassword());
        bodyParam.add("refresh_token", keycloakAccessDTO.getRefreshToken());

        try {
            String username = keycloakAccessDTO.getUsername();
            List<String> userPermission = loginService.findUserPermission(realm,username);

            Response authenticationResponse = keycloakService.authenticate(realm, bodyParam);

            if (authenticationResponse.getStatus() == Response.Status.OK.getStatusCode()) {
                Map<String, Object> responseBody = authenticationResponse.readEntity(new GenericType<Map<String, Object>>() {
                });

                if (userPermission != null) {
                    responseBody.put("user_permission", userPermission);
                } else {
                    responseBody.put("user_permission", null);
                }

                return Response.status(Response.Status.OK).entity(responseBody).build();
            } else {
                return authenticationResponse;
            }
        } catch (WebApplicationException webApplicationException) {
            return Response.status(webApplicationException.getResponse().getStatus()).build();
        }
    }

    @POST
    @Path("/{realm}/logout")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Endpoint de invalidação do token de acesso", description = "Realizar o logout do usuário logado através do refresh_token informado.")
    public Response logout(@PathParam("realm") String realm, KeycloakAccessDTO keycloakAccessDTO) {

        MultivaluedMap<String, String> bodyParam = new MultivaluedHashMap<>();

        bodyParam.add("client_id",keycloakAccessDTO.getIdClient());
        bodyParam.add("client_secret",keycloakAccessDTO.getClientSecret());
        bodyParam.add("refresh_token",keycloakAccessDTO.getRefreshToken());

        try {
            return keycloakService.logout(
                    realm,
                    bodyParam);
        }
        catch (WebApplicationException webApplicationException){
            return Response.status(webApplicationException.getResponse().getStatus()).build();
        }
    }
}