package br.com.repassa.service.healthcheck;

import br.com.repassa.resource.client.KeycloakConfig;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.PersistenceException;

@ApplicationScoped
@Slf4j
public class HealthCheckService implements HealthCheck {

    @Inject
    KeycloakConfig keycloakConfig;

    @Override
    public HealthCheckResponse call() {
        try {
            checkConnection();
            log.info("O servico esta ativo");
            return HealthCheckResponse.named("O serviço está online").up().build();
        } catch (PersistenceException e) {
            log.info("O servico fora do ar");
            return HealthCheckResponse.named("O serviço está offline").down().build();
        }
    }

    private void checkConnection() {
        log.info("Verificando se o keycloak esta ativo");
        keycloakConfig.getRealmResource().roles();
        log.info("O keycloak esta ativo");
    }

}
