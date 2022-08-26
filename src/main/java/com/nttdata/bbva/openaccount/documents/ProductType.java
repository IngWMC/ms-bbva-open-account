package com.nttdata.bbva.openaccount.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotEmpty;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductType {
	private String id;
	private String name;
	private String shortName;
}
