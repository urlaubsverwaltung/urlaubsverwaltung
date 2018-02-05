package org.synyx.urlaubsverwaltung.security;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface OutlookService {

	@GET("/v1.0/me")
	Call<OutlookUser> getCurrentUser();

}