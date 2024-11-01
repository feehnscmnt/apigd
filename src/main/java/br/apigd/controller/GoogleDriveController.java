package br.apigd.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import com.google.api.services.drive.model.File;
import org.springframework.http.ResponseEntity;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import br.apigd.service.GoogleDriveService;
import org.springframework.http.MediaType;
import java.io.OutputStream;
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
@RequestMapping("/v1/drive")
public class GoogleDriveController implements Serializable {
	private static final long serialVersionUID = 6153545797045040459L;
	private GoogleDriveService googleDriveService;
	
	/**
	 * Contrutor da classe.
	 * 
	 * @param googleDriveService - {@link GoogleDriveService} - serviço de comunicação com a API do Google Drive
	 * 
	 */
	public GoogleDriveController(GoogleDriveService googleDriveService) {
		this.googleDriveService = googleDriveService;
	}
	
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
		
		File file = googleDriveService.getFile(fileId);
		
		response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", file.getName()));
		
		OutputStream outputStream = response.getOutputStream();
        googleDriveService.downloadFile(fileId, response.getOutputStream());
        outputStream.flush();
        
        return ResponseEntity.ok().build();
		
	}
	
}