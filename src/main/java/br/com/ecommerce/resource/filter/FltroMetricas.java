package br.com.repassa.resource.filter;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

import io.micrometer.core.instrument.MeterRegistry;

@Provider
public class FltroMetricas implements ContainerRequestFilter {

	MeterRegistry registry;

	@Inject
	FltroMetricas(MeterRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		this.registry.counter("exemplo.metricas.requests.global", "type", "requests").increment();
		String path = requestContext.getUriInfo().getPath().replace("/", ".");
		if (!requestContext.getUriInfo().getPathParameters().isEmpty()) {
			this.registry.counter("exemplo.metricas.requests".concat(".".concat(path)), "type", "requests").increment();
		}
		
		if (requestContext.getUriInfo().getPath().equals("/exemplo/hello")) {
			this.registry.counter("exemplo.metricas.requests".concat(".".concat(path)), "type", "requests").increment();
		}
	}

}
