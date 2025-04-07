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
import org.springframework.stereotype.Service;
import java.security.GeneralSecurityException;
import com.google.api.services.drive.Drive;
import jakarta.annotation.PostConstruct;
import java.io.InputStreamReader;
import java.util.Collections;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

/**
 * Classe service responsável pela configuração da aplicação junto à API do Google
 * 
 * @author Felipe Nascimento
 *
 */

@Service
public class GoogleDriveService implements Serializable {
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
		
		var netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
		drive = new Drive.Builder(netHttpTransport, GsonFactory.getDefaultInstance(), getCredentials(netHttpTransport)).setApplicationName("API-GD").build();
		
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
		
	    List<File> allFiles = new ArrayList<>();
	    
	    Drive.Files.List request = drive.files().list()
	    	.setQ(String.format("'%s' in parents", folderId))
	        .setFields("nextPageToken, files(id, name)");

	    do {
	    	
	        var files = request.execute();
	        allFiles.addAll(files.getFiles());
	        request.setPageToken(files.getNextPageToken());
	        
	    } while (Objects.nonNull(request.getPageToken()) && request.getPageToken().isEmpty());

	    return allFiles;
	    
	}
	
	/**
	 * Método responsável pelo download de um determinado arquivo.
	 * 
	 * @param fileId - {@link String} - id do arquivo que será baixado
	 * @param outputStream - {@link OutputStream} - objeto com os bytes do arquivo
	 * 
	 * @throws {@link IOException}
	 * 
	 */
	public void downloadFile(String fileId, OutputStream outputStream) throws IOException {
		
		drive.files().get(fileId).executeMedia().download(outputStream);
		
    }
	
	/**
	 * 
	 * @param fileId - {@link String} - id do arquivo que será baixado
	 * 
	 * @return o arquivo de acordo com o id
	 * 
	 * @throws {@link IOException}
	 * 
	 */
	public File getFile(String fileId) throws IOException {
		
        return drive.files().get(fileId).execute();
        
    }
	
}