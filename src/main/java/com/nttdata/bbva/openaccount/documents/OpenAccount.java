package com.nttdata.bbva.openaccount.documents;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "openAccounts")
public class OpenAccount {
    @Id
    private String id;
    @NotEmpty(message = "El campo customerId es requerido.")
    private String customerId;
    @NotEmpty(message = "El campo productId es requerido.")
    private String productId;
    @DecimalMin(value = "0.0", message = "El campo amountAvailable debe tener un valor mínimo de '0.0'.")
    @Digits(integer = 10, fraction = 3, message = "El campo amountAvailable tiene un formato no válido (#####.000).")
    @NotNull(message = "El campo amountAvailable es requerido.")
    private BigDecimal amountAvailable; // Monto disponible
    private BigDecimal creditLine; // Monto de la linea de crédito (Tarjeta de crédito)
}
