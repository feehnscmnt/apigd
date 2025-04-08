package br.apigd.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import com.google.api.services.drive.model.File;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import br.apigd.model.ResponseMessages;
import br.apigd.service.GoogleDriveService;
import org.springframework.http.MediaType;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.io.IOException;
import java.util.List;

/**
 * Classe controller responsável pela listagem e download de arquivos.
 * 
 * @author Felipe Nascimento
 *
 */

@RestController
@AllArgsConstructor
@RequestMapping("/v1/drive")
public class GoogleDriveController implements Serializable {
	private static final long serialVersionUID = 6153545797045040459L;
	private GoogleDriveService googleDriveService;
	
	/**
	 * Método responsável pela listagem de arquivos de uma determinada pasta.
	 * 
	 * @param folderId - {@link PathVariable} / {@link String} - id da pasta que contém os arquivos
	 * 
	 * @return lista de arquivos encontrados na pasta
	 * 
	 * @throws {@link IOException}
	 * 
	 */
	@GetMapping("/listar-arquivos-pasta/{folderId}")
    public List<File> listFilesInFolder(@PathVariable String folderId) throws IOException {
		
        return googleDriveService.listFilesInFolder(folderId);
        
    }
	
	/**
	 * Método responsável pelo download de um determinado arquivo.
	 * 
	 * @param fileId - {@link PathVariable} / {@link String} - id do arquivo que será baixado
	 * @param response - {@link HttpServletResponse} - configura e envia a resposta HTTP de volta ao cliente.
	 * 
	 * @return arquivo baixado
	 * 
	 * @throws {@link IOException}
	 * 
	 */
	@GetMapping("/baixar-arquivo/{fileId}")
	public ResponseEntity<Void> downloadFile(@PathVariable String fileId, HttpServletResponse response) throws IOException {
		
		var file = googleDriveService.getFile(fileId);
		
		response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", file.getName()));
		
        googleDriveService.downloadFile(fileId, response.getOutputStream());
        
        return ResponseEntity.ok().build();
		
	}
    
    /**
     * Método responsável por obter informações de um arquivo para visualização.
     * Retorna um objeto File contendo metadados, incluindo links de visualização (se disponíveis).
     *
     * @param fileId - {@link PathVariable} / {@link String} - id do arquivo que será visualizado
     *
     * @return arquivo para visualização
     *
     * @throws {@link IOException}
     * 
     */
    @GetMapping("/visualizar-metadados-arquivo/{fileId}")
    public ResponseEntity<File> viewMetadataFile(@PathVariable String fileId) throws IOException {
    	
        return ResponseEntity.ok(googleDriveService.viewMetadataFile(fileId));
        
    }
    
    /**
     * Método responsável pela exclusão de um determinado arquivo.
     *
     * @param fileId - {@link PathVariable} / {@link String} - id do arquivo que será excluído
     *
     * @return status de sucesso ou erro da exclusão
     *
     * @throws {@link IOException}
     * 
     */
    @DeleteMapping("/excluir-arquivo/{fileId}")
    public ResponseEntity<Object> deleteFile(@PathVariable String fileId) throws IOException {
    	
        googleDriveService.deleteFile(fileId);
        
        return ResponseEntity.status(HttpStatus.OK)
        	.body(new ResponseMessages("Arquivo excluído com sucesso.", HttpStatus.NO_CONTENT, HttpStatus.NO_CONTENT.value()));
        
    }
	
}