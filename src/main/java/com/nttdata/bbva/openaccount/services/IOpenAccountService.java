package com.nttdata.bbva.openaccount.services;

import com.nttdata.bbva.openaccount.documents.OpenAccount;
import reactor.core.publisher.Mono;

public interface IOpenAccountService extends ICRUD<OpenAccount, String> {}
