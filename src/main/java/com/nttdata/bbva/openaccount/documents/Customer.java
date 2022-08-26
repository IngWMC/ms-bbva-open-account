package com.nttdata.bbva.openaccount.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {
	private String id;
	private String fullName;
	private String customerTypeId;
	private CustomerType customerType;
	private String identificationDocument;
	private String emailAddress;
}
