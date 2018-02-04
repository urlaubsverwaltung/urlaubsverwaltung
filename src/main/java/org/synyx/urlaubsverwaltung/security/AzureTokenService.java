package org.synyx.urlaubsverwaltung.security;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AzureTokenService {
	@FormUrlEncoded
	@POST("/{tenantid}/oauth2/token")
	Call<AzureTokenResponse> getAccessTokenFromAuthCode(
		@Path("tenantid") String tenantId,
		@Field("client_id") String clientId,
		@Field("client_secret") String clientSecret,
		@Field("grant_type") String grantType,
		@Field("code") String code,
		@Field("redirect_uri") String redirectUrl,
		@Field("resource") String resource
	);
	
	@FormUrlEncoded
	@POST("/{tenantid}/oauth2/token")
	Call<AzureTokenResponse> getAccessTokenFromRefreshToken(
		@Path("tenantid") String tenantId,
		@Field("client_id") String clientId,
		@Field("client_secret") String clientSecret,
		@Field("grant_type") String grantType,
		@Field("refresh_token") String code,
		@Field("redirect_uri") String redirectUrl
	);
}
