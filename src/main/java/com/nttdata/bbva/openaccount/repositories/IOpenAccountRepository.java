package com.nttdata.bbva.openaccount.repositories;

import com.nttdata.bbva.openaccount.documents.OpenAccount;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface IOpenAccountRepository extends ReactiveMongoRepository<OpenAccount, String> {}
