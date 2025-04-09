package br.apigd.model;

import org.springframework.http.HttpStatus;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import lombok.Data;

/**
 * Classe model para tratamento das mensagens dos responses da API-GD.
 * 
 * @author Felipe Nascimento
 *
 */

@Data
@AllArgsConstructor
public class ResponseMessages implements Serializable {
	private static final long serialVersionUID = -4036533183034821280L;
	private String statusMessage;
	private HttpStatus statusRequest;
	private int statusCode;
}