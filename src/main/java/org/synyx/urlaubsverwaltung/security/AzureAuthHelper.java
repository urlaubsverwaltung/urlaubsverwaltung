package org.synyx.urlaubsverwaltung.security;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponentsBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.security.AzureTokenResponse;

@Service
public class AzureAuthHelper {
	@Autowired
	@Value("${uv.security.azure.tenand_id}") 
	private String tenandId;
	@Autowired
	@Value("${uv.security.azure.redirect_url}")
	private String redirectUrl;
	@Autowired
	@Value("${uv.security.azure.application.id}")
	private String appId;
	@Autowired
	@Value("${uv.security.azure.application.password}")
	private String appPassword;
	
	private static final String authority = "https://login.microsoftonline.com";
	
	public AzureAuthHelper() {}
	
	private String getAuthorizeUrl() {
		return authority + "/" + tenandId + "/oauth2/authorize";
	}
	public String getLoginUrl(UUID state, UUID nonce) {
		//https://docs.microsoft.com/de-de/azure/active-directory/develop/active-directory-protocols-oauth-code
		
		UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromHttpUrl(getAuthorizeUrl());
		urlBuilder.queryParam("client_id", appId);
		urlBuilder.queryParam("redirect_uri", redirectUrl);
		urlBuilder.queryParam("response_type", "code");
		urlBuilder.queryParam("state", state);
		urlBuilder.queryParam("response_mode", "form_post");
		urlBuilder.queryParam("prompt", "login");
		
		return urlBuilder.toUriString();
	}
	
	public AzureTokenResponse getTokenFromAuthCode(String authCode) {
		// Create a logging interceptor to log request and responses
		HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
		interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
		
		OkHttpClient client = new OkHttpClient.Builder()
				.addInterceptor(interceptor).build();
		
		// Create and configure the Retrofit object
		
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(authority)
				.client(client)
				.addConverterFactory(JacksonConverterFactory.create())
				.build();
		
		// Generate the token service
		AzureTokenService AzureTokenService = retrofit.create(AzureTokenService.class);
		
		try {
			return AzureTokenService.getAccessTokenFromAuthCode(tenandId, appId, appPassword, 
					"authorization_code", authCode, redirectUrl, "https://graph.microsoft.com").execute().body();
		} catch (IOException e) {
			AzureTokenResponse error = new AzureTokenResponse();
			error.setError("IOException");
			error.setErrorDescription(e.getMessage());
			return error;
		}
	}
}