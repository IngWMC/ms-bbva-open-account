package com.nttdata.bbva.openaccount.services.impl;

import com.nttdata.bbva.openaccount.clients.CustomerClient;
import com.nttdata.bbva.openaccount.clients.ProductClient;
import com.nttdata.bbva.openaccount.documents.OpenAccount;
import com.nttdata.bbva.openaccount.documents.Product;
import com.nttdata.bbva.openaccount.enums.CustomerTypeEnum;
import com.nttdata.bbva.openaccount.enums.ProductEnum;
import com.nttdata.bbva.openaccount.enums.ProductTypeEnum;
import com.nttdata.bbva.openaccount.exceptions.BadRequestException;
import com.nttdata.bbva.openaccount.exceptions.ModelNotFoundException;
import com.nttdata.bbva.openaccount.repositories.IOpenAccountRepository;
import com.nttdata.bbva.openaccount.services.IOpenAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
public class OpenAccountServiceImpl implements IOpenAccountService {
	private static final Logger logger = LoggerFactory.getLogger(OpenAccountServiceImpl.class);
	@Autowired
	private CustomerClient customerClient;
	@Autowired
	private ProductClient productClient;
	@Autowired
	private WebClient.Builder webClientBuilder;
	@Autowired
	private IOpenAccountRepository repo;

	@Override
	public Mono<OpenAccount> insert(OpenAccount obj) {
		return customerClient.findById(obj.getCustomerId())
				.switchIfEmpty(Mono.error(() -> new BadRequestException("El campo customerId tiene un valor no válido.")))
				.flatMap(customer -> productClient.findById(obj.getProductId())
						.switchIfEmpty(Mono.error(() -> new BadRequestException("El campo productId tiene un valor no válido.")))
						.flatMap(product -> {
							String customerTypeShortName = customer.getCustomerType().getShortName();
							String productTypeShortName = product.getProductType().getShortName();

							if (productTypeShortName.equals(ProductTypeEnum.CUEB.toString())) { // APERTURAR CUENTA BANCARIA
								return this.openBankAccount(customerTypeShortName, obj, product);
							} else { // APERTURAR CUENTA DE CRÉDITO
								return openCreditAccount(customerTypeShortName, obj, product);
							}
						})
				)
				.doOnNext(openAccount -> logger.info("SE APERTURÓ LA CUENTA ::: " + openAccount.getId()));
	}

	@Override
	public Mono<OpenAccount> update(OpenAccount obj) {
		if (obj.getId() == null || obj.getId().isEmpty())
			return Mono.error(() -> new BadRequestException("El campo id es requerido."));
		
		return repo.findById(obj.getId())
				.switchIfEmpty(Mono.error(() -> new ModelNotFoundException("CONTRATO NO ENCONTRADO")))
				.flatMap(openAccount -> customerClient.findById(obj.getCustomerId())
						.switchIfEmpty(Mono.error(() -> new BadRequestException("El campo customerId tiene un valor no válido.")))
						.flatMap(customer -> productClient.findById(obj.getProductId())
								.switchIfEmpty(Mono.error(() -> new BadRequestException("El campo productId tiene un valor no válido.")))
								.flatMap(product -> repo.save(obj))
						)
				)
				.doOnNext(o -> logger.info("SE ACTUALIZÓ EL CONTRATO ::: " + o.getId()));
	}

	@Override
	public Flux<OpenAccount> findAll() {
		return repo.findAll();
	}

	@Override
	public Mono<OpenAccount> findById(String id) {
		return repo.findById(id)
				.switchIfEmpty(Mono.error(() -> new ModelNotFoundException("CONTRATO NO ENCONTRADO")))
				.doOnNext(c -> logger.info("SE ENCONTRÓ EL CONTRATO ::: " + id));
	}

	@Override
	public Mono<Void> delete(String id) {
		return repo.findById(id)
				.switchIfEmpty(Mono.error(() -> new ModelNotFoundException("CONTRATO NO ENCONTRADO")))
				.flatMap(contract -> repo.deleteById(contract.getId()))
				.doOnNext(c -> logger.info("SE ELIMINÓ EL CONTRATO ::: " + id));
	}

	private Mono<Boolean> existOpenAccount(String customerId, String productId) {
		return this.findAll()
				.filter(openAccount -> openAccount.getCustomerId().equals(customerId) &&
						openAccount.getProductId().equals(productId))
				.next()
				.hasElement()
				.doOnNext(exist -> {
					if (exist) logger.info("SE ENCONTRÓ LA APERTURA DE CUENTA PARA EL CLIENTE ::: " + customerId + " ::: PRODUCTO ::: " + productId);
					else logger.info("SE NO ENCONTRÓ LA APERTURA DE CUENTA PARA EL CLIENTE ::: " + customerId + " ::: PRODUCTO ::: " + productId);
				});
	}
	private Mono<Boolean> existOpenAccountWithCreditCard(String customerId, String productShortName) {
		return this.findAll()
				.filter(openAccount -> openAccount.getCustomerId().equals(customerId))
				.map(openAccount -> productClient.findById(openAccount.getProductId())
							.filter(product -> product.getShortName().equals(productShortName))
				)
				.next()
				.hasElement()
				.doOnNext(exist -> {
					if (exist) logger.info("SE ENCONTRÓ EL PRODUCTO ::: " + productShortName + " ::: PARA EL CLIENTE ::: " + customerId);
					else logger.info("NO SE ENCONTRÓ EL PRODUCTO ::: " + productShortName + " ::: PARA EL CLIENTE ::: " + customerId);
				});
	}
	private Mono<OpenAccount> openBankAccount(String customerTypeShortName, OpenAccount openAccount, Product product) {
		String productShortName = product.getShortName();
		if (customerTypeShortName.equals(CustomerTypeEnum.E.toString())) { //=== CLIENTE EMPRESARIAL
			if (productShortName.equals(ProductEnum.CUEC.toString()))
				return repo.save(openAccount);
			else
				return Mono.error(() -> new BadRequestException("El cliente no puede aperturar la cuenta: " + product.getName()));
		} else if (customerTypeShortName.equals(CustomerTypeEnum.EPYME.toString())) { //=== CLIENTE EMPRESARIAL PYME
			if (productShortName.equals(ProductEnum.CUEC.toString()))
				return this.openBankAccountIfHaveCreditCard(openAccount, product);
			else
				return repo.save(openAccount);
		} else if (customerTypeShortName.equals(CustomerTypeEnum.PVIP.toString())) { //=== CLIENTE PERSONAL VIP
			if (productShortName.equals(ProductEnum.CUEA.toString()))
				return this.openBankAccountIfHaveCreditCard(openAccount, product);
			else
				return repo.save(openAccount);
		} else { //=== CLIENTE PERSONAL
			return this.existOpenAccount(openAccount.getCustomerId(), openAccount.getProductId())
					.flatMap(existOpenAccount -> {
						if (existOpenAccount)
							return Mono.error(() -> new BadRequestException("El cliente ya cuenta con el siguiente producto: " + product.getName()));
						else
							return repo.save(openAccount);
					});
		}
	}
	private Mono<OpenAccount> openBankAccountIfHaveCreditCard(OpenAccount openAccount, Product product) {
		return this.existOpenAccountWithCreditCard(openAccount.getCustomerId(), ProductEnum.TCRE.toString())
				.flatMap(existOpenAccountWithCreditCard -> {
					if (existOpenAccountWithCreditCard) return repo.save(openAccount);
					else return Mono.error(() -> new BadRequestException("El cliente no puede aperturar la cuenta '" + product.getName() + "', porque no cuenta con una tarjeta de crédito"));
				});
	}
	private Mono<OpenAccount> openCreditAccount(String customerTypeShortName, OpenAccount openAccount, Product product) {
		String productShortName = product.getShortName();
		if (customerTypeShortName.equals(CustomerTypeEnum.E.toString())) { //=== CLIENTE EMPRESARIAL
			if (productShortName.equals(ProductEnum.TCRE.toString())) {
				return this.existOpenAccount(openAccount.getCustomerId(), openAccount.getProductId())
						.flatMap(existOpenAccount -> {
							openAccount.setCreditLine(openAccount.getAmountAvailable());

							if (existOpenAccount) return Mono.error(() -> new BadRequestException("El cliente ya cuenta con el siguiente producto: " + product.getName()));
							else return repo.save(openAccount);
						});
			} else {
				return repo.save(openAccount);
			}
		} else { //=== CLIENTE PERSONAL
			return this.existOpenAccount(openAccount.getCustomerId(), openAccount.getProductId())
					.flatMap(existOpenAccount -> {
						if (productShortName.equals(ProductEnum.TCRE.toString()))
							openAccount.setCreditLine(openAccount.getAmountAvailable());

						if (existOpenAccount) return Mono.error(() -> new BadRequestException("El cliente ya cuenta con el siguiente producto: " + product.getName()));
						else return repo.save(openAccount);
					});
		}
	}
}
