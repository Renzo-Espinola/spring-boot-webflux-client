package com.renzo.springboot.webflux.client.app.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;

import static org.springframework.http.MediaType.*;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.renzo.springboot.webflux.client.app.models.Producto;
import com.renzo.springboot.webflux.client.app.models.service.ProductoService;

import reactor.core.publisher.Mono;

@Component
public class ProductoHandler {

	@Autowired
	private ProductoService service;

	public Mono<ServerResponse> listar(ServerRequest request) {
		return ServerResponse.ok().contentType(APPLICATION_JSON).body(service.findAll(), Producto.class);
	}

	public Mono<ServerResponse> ver(ServerRequest request) {
		String id = request.pathVariable("id");
		return errorHandler(service
				.findById(id)
				.flatMap(p -> ServerResponse
						.ok()
						.contentType(APPLICATION_JSON)
						.bodyValue(p)
						.switchIfEmpty(ServerResponse
								.notFound()
								.build())));
	}
	
	public Mono<ServerResponse> crear(ServerRequest request){
		Mono<Producto> producto = request.bodyToMono(Producto.class);
		return producto.flatMap(p-> {
					if(p.getCreateAt()==null){
						p.setCreateAt(new Date());
					}
					return service.save(p);})
				.flatMap(p->{
					if (p.getId() == null || p.getId().isEmpty()) {
	                    return ServerResponse.badRequest().build();
	                }
				return ServerResponse
				.created(URI.create("/api/client/".concat(p.getId())))
				.contentType(APPLICATION_JSON)
				.bodyValue(p);})
				.onErrorResume(error -> {
					WebClientResponseException errorResponse = (WebClientResponseException) error;
					if(HttpStatus.BAD_REQUEST == errorResponse.getStatusCode()) {
						return ServerResponse
								.badRequest()
								.contentType(MediaType.APPLICATION_JSON)
								.bodyValue(errorResponse.getResponseBodyAsString());
					}
					return Mono.error(errorResponse);
				});
	}
	
	public Mono<ServerResponse> editar(ServerRequest request){
		Mono<Producto> producto = request.bodyToMono(Producto.class);
		String id = request.pathVariable("id");
		return errorHandler(producto
				.flatMap(p-> service.update(p, id))
				.flatMap(p->ServerResponse
				.created(URI.create("/api/client/".concat(p.getId())))
				.contentType(APPLICATION_JSON)
				.bodyValue(p)));	
	}
	
	public Mono<ServerResponse> eliminar(ServerRequest request){
		String id = request.pathVariable("id");
		return errorHandler(service
				.delete(id)
				.then(ServerResponse.noContent().build()));
	}
	
	public Mono<ServerResponse> upload(ServerRequest request){
		String id = request.pathVariable("id");
		return errorHandler(request.multipartData().map(multipart->multipart.toSingleValueMap().get("file"))
				.cast(FilePart.class)
				.flatMap(file -> service.upload(file, id))
				.flatMap(p->ServerResponse
						.created(URI.create("/api/client/".concat(p.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(p)));
	}
	
	private Mono<ServerResponse> errorHandler(Mono<ServerResponse> response){
		return response.onErrorResume(error -> {
			WebClientResponseException errorResponse = (WebClientResponseException) error;
			if(HttpStatus.NOT_FOUND == errorResponse.getStatusCode()) {
				Map<String,Object> mErrorResponse = new HashMap<>();
				mErrorResponse.put("error", "No existe el producto: ".concat(errorResponse.getMessage()));
				mErrorResponse.put("timestamp", new Date());
				mErrorResponse.put("status", errorResponse.getStatusCode().value());
				return ServerResponse
						.status(HttpStatus.NOT_FOUND)
						.bodyValue(mErrorResponse);
			}
			return Mono.error(errorResponse);
		});
	}

}
