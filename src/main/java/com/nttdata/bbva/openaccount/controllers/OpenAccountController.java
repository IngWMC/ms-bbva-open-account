package com.nttdata.bbva.openaccount.controllers;

import com.nttdata.bbva.openaccount.documents.OpenAccount;
import com.nttdata.bbva.openaccount.services.IOpenAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("api/1.0.0/openaccounts")
public class OpenAccountController {
	private static final Logger logger = LoggerFactory.getLogger(OpenAccountController.class);
	@Autowired
	private IOpenAccountService service;
	
	@GetMapping
	public Mono<ResponseEntity<Flux<OpenAccount>>> findAll(){
		logger.info("Inicio OpenAccountController ::: findAll");
		Flux<OpenAccount> openAccounts = service.findAll().doOnNext(x -> logger.info("Fin OpenAccountController ::: findAll"));
		return Mono.just(ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(openAccounts));
	}
	
	@GetMapping("/{id}")
	public Mono<ResponseEntity<Mono<OpenAccount>>> findById(@PathVariable("id") String id){
		logger.info("Inicio OpenAccountController ::: fintById ::: " + id);
		Mono<OpenAccount> openAccount = service.findById(id).doOnNext(x -> logger.info("Fin OpenAccountController ::: findById"));
		return Mono.just(ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(openAccount));
	}
	
	@PostMapping
	public Mono<ResponseEntity<Mono<OpenAccount>>> insert(@Valid @RequestBody OpenAccount obj){
		logger.info("Inicio OpenAccountController ::: insert ::: " + obj.toString());
		Mono<OpenAccount> accountMono = service.insert(obj).doOnNext(x -> logger.info("Fin OpenAccountController ::: insert"));
		return Mono.just(new ResponseEntity<Mono<OpenAccount>>(accountMono, HttpStatus.CREATED));
	}
	
	@PutMapping
	public Mono<ResponseEntity<Mono<OpenAccount>>> update(@Valid @RequestBody OpenAccount obj){
		logger.info("Inicio OpenAccountController ::: update ::: " + obj.toString());
		Mono<OpenAccount> accountMono = service.update(obj).doOnNext(x -> logger.info("Fin OpenAccountController ::: update"));
		return Mono.just(new ResponseEntity<Mono<OpenAccount>>(accountMono, HttpStatus.CREATED));
	}
	
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> delete(@PathVariable("id") String id) {
		logger.info("Inicio OpenAccountController ::: delete ::: " + id);
		service.delete(id).doOnNext(x -> logger.info("Fin OpenAccountController ::: delete"));
		return Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT));
	}

}
