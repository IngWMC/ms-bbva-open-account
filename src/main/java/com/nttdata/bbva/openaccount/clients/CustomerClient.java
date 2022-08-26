package com.nttdata.bbva.openaccount.clients;

import com.nttdata.bbva.openaccount.documents.Customer;
import com.nttdata.bbva.openaccount.exceptions.ModelNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class CustomerClient {
    private static final Logger logger = LoggerFactory.getLogger(CustomerClient.class);
    private final WebClient webClient;

    public CustomerClient(WebClient.Builder webClientBuilder){
        this.webClient = webClientBuilder.baseUrl("http://localhost:7071/api/1.0.0/customers").build();
    }

    @CircuitBreaker(name = "customer", fallbackMethod = "fallBackFindById")
    public Mono<Customer> findById(String id){
        logger.info("Inicio CustomerClient ::: findById ::: " + id);
        return this.webClient.get()
                .uri("/{id}", id)
                .retrieve()
                .bodyToMono(Customer.class)
                .doOnNext(x -> logger.info("Fin CustomerClient ::: findById"));
    }

    private Mono<String> fallBackFindById(String id, RuntimeException e) {
        return Mono.error(() -> new ModelNotFoundException("Microservicio Customer no está repondiendo."));
    }
}
