package com.nttdata.bbva.openaccount.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerType {
	private String id;
	private String name;
	private String shortName;
}
