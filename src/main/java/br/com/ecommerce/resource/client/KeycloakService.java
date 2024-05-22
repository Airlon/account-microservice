package br.com.repassa.resource.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

@Path("/realms")
@RegisterRestClient(configKey = "keycloak-resource")
public interface KeycloakService {

    @POST
    @Path("{realm}/protocol/openid-connect/token")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    Response authenticate(@PathParam("realm") String realm,
                          MultivaluedMap<String, String> bodyParam
    );

    @POST
    @Path("{realm}/protocol/openid-connect/logout")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    Response logout(@PathParam("realm") String realm,
                          MultivaluedMap<String, String> bodyParam
    );
}
