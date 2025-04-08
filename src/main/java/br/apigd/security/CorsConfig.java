package br.apigd.security;

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.context.annotation.Configuration;
import java.io.Serializable;

/**
 * Classe responsável pelas configurações de permissão do CORS.
 * 
 * @author Felipe Nascimento
 * 
 */

@Configuration
public class CorsConfig implements WebMvcConfigurer, Serializable {
	private static final long serialVersionUID = 5484691406174692234L;
	
	/**
	 * Método responsável por permitir que todos os endpoints da API-GD recebam requisições de qualquer origem.
	 * 
	 * @param registry - {@link CorsRegistry} - permite definir as configurações de CORS da API-GD
	 * 
	 */
	@Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/v1/**").allowedOrigins("*").allowedMethods("GET", "DELETE");
    }
	
}