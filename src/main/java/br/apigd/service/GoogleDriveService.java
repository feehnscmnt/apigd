package br.apigd.service;

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import java.security.GeneralSecurityException;
import org.springframework.http.HttpHeaders;
import com.google.api.services.drive.Drive;
import org.apache.logging.log4j.LogManager;
import org.springframework.http.MediaType;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.Logger;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.io.Serializable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

/**
 * Classe service responsável pela configuração da API-GD junto à API do Google Drive.
 * 
 * @author Felipe Nascimento
 *
 */

@Service
public class GoogleDriveService implements Serializable {
	private static final Logger LOG = LogManager.getLogger(GoogleDriveService.class.getName());
	private static final long serialVersionUID = 7756442328171923425L;
	private transient Drive drive;
	
	/**
	 * Método responsável pela configuração e inicialização do cliente da API do Google Drive
	 * com as credenciais de transporte necessárias.
	 * 
	 * @throws {@link GeneralSecurityException}
	 * @throws {@link IOException}
	 * 
	 */
	@PostConstruct
	public void init() throws GeneralSecurityException, IOException {
		
		LOG.info("Realizando a configuração e inicialização do cliente da API do Google Drive...");
		
		var netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
		drive = new Drive.Builder(netHttpTransport, GsonFactory.getDefaultInstance(), getCredentials(netHttpTransport)).setApplicationName("API-GD").build();
		
		LOG.info("Configuração realizada com sucesso.");
		
	}
	
	/**
	 * Método responsável pela autenticação da aplicação com a API do Google Drive usando OAuth 2.0.
	 * 
	 * @param netHttpTransport - {@link NetHttpTransport} - transportador HTTP seguro
	 * 
	 * @return as credenciais necessárias para inicialização da API do Google Drive
	 * 
	 * @throws {@link IOException}
	 * 
	 */
	private Credential getCredentials(NetHttpTransport netHttpTransport) throws IOException {
		
        return new AuthorizationCodeInstalledApp(
        		
	        	new GoogleAuthorizationCodeFlow.Builder(
	        	
	        	netHttpTransport,
	        	GsonFactory.getDefaultInstance(),
	        	GoogleClientSecrets.load(
	        		
	        		GsonFactory.getDefaultInstance(),
	        		
	        		new InputStreamReader(
	        			
	        			GoogleDriveService.class.getResourceAsStream("/credentials.json")
	        			
	        		)
	        		
	        	),
	        	
	        	Collections.singletonList(DriveScopes.DRIVE)
	        	
	        ).setDataStoreFactory(
	        		
	        	new FileDataStoreFactory(new java.io.File("tokens"))
	        	
	        ).setAccessType("offline").build(),
	        	
	        	new LocalServerReceiver.Builder().setPort(8888).build()
	        	
        ).authorize("user");
        
	}
	
	/**
	 * Método responsável pela listagem de arquivos de uma determinada pasta.
	 * 
	 * @param folderId - {@link String} - id da pasta que contém os arquivos
	 * 
	 * @return lista de arquivos encontrados na pasta
	 * 
	 * @throws {@link IOException}
	 * 
	 */
	public List<File> listFilesInFolder(String folderId) throws IOException {
		
		LOG.info("Listando arquivos da pasta id {}", folderId);
		
	    List<File> allFiles = new ArrayList<>();
	    
	    Drive.Files.List request = drive.files().list()
	    	.setQ(String.format("'%s' in parents", folderId))
	        .setFields("nextPageToken, files(id, name)");

	    do {
	    	
	        var files = request.execute();
	        allFiles.addAll(files.getFiles());
	        request.setPageToken(files.getNextPageToken());
	        
	    } while (Objects.nonNull(request.getPageToken()));
	    
	    allFiles.sort(Comparator.comparing(File::getName));
	    
	    LOG.info("Arquivos listados com sucesso.");
	    
	    return allFiles;
	    
	}
	
	/**
	 * Método responsável pelo download de um determinado arquivo.
	 * 
	 * @param fileId - {@link String} - id do arquivo que será baixado
	 * @param response - {@link HttpServletResponse} - configura e envia a resposta HTTP de volta ao cliente
	 * 
	 * @throws {@link IOException}
	 * 
	 */
	public void downloadFile(String fileId, HttpServletResponse response) throws IOException {
		
		LOG.info("Realizando o download do arquivo id {}", fileId);
		
		var file = drive.files().get(fileId).execute();
		
		response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", file.getName()));
		
		drive.files().get(fileId).executeMedia().download(response.getOutputStream());
		
		LOG.info("Download realizado com sucesso.");
		
    }
    
    /**
     * Método responsável por obter informações de um arquivo para visualização (se aplicável).
     * Para alguns tipos de arquivo, o Google Drive pode gerar um link de visualização.
     *
     * @param fileId - {@link String} - id do arquivo que será visualizado
     * 
     * @return {@link File} - objeto contendo metadados do arquivo, incluindo um possível link de visualização
     * 
     * @throws {@link IOException}
     * 
     */
    public File viewMetadataFile(String fileId) throws IOException {
    	
    	LOG.info("As informações do arquivo id {} forma obtidas com sucesso.", fileId);
    	
    	return drive.files().get(fileId).setFields("id, name, mimeType, webViewLink, webContentLink").execute();
    	
    }
    
    /**
     * Método responsável pela exclusão de um determinado arquivo.
     *
     * @param fileId - {@link String} - id do arquivo que será excluído
     *
     * @throws {@link IOException}
     * 
     */
    public void deleteFile(String fileId) throws IOException {
    	
    	LOG.info("Realizando a exclusão do arquivo id {}", fileId);
    	
        drive.files().delete(fileId).execute();
        
        LOG.info("Exclusão realizada com sucesso.");
        
    }
	
}