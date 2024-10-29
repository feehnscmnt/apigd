package br.apigd;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.SpringApplication;

/**
 * Classe responsável pela inicialização da API-GD.
 * 
 * @author Felipe Nascimento
 * 
 */

@SpringBootApplication
@ComponentScan({ "br.apigd.controller", "br.apigd.service" })
public class ApiGdApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGdApplication.class, args);
	}

}