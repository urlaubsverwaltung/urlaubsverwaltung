package org.synyx.urlaubsverwaltung.security;

import java.util.Base64;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AzureIdToken {
	// NOTE: This is just a subset of the claims returned in the
	// ID token. For a full listing, see:
	// https://azure.microsoft.com/en-us/documentation/articles/active-directory-v2-tokens/#idtokens
	@JsonProperty("exp")
	private long expirationTime;
	@JsonProperty("nbf")
	private long notBefore;
	@JsonProperty("family_name")
	private String familyName;
	@JsonProperty("given_name")
	private String givenName;
	@JsonProperty("unique_name")
	private String uniqueName;
	private String upn;
	
	public static AzureIdToken parseEncodedToken(String encodedToken) {
		// Encoded token is in three parts, separated by '.'
		String[] tokenParts = encodedToken.split("\\.");

		// The three parts are: header.token.signature
		String idToken = tokenParts[1];

		byte[] decodedBytes = Base64.getUrlDecoder().decode(idToken);

		ObjectMapper mapper = new ObjectMapper();
		AzureIdToken newToken = null;
		try {
			newToken = mapper.readValue(decodedBytes, AzureIdToken.class);
			if (!newToken.isValid()) {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newToken;
	}
	
	public long getExpirationTime() {
		return expirationTime;
	}
	public void setExpirationTime(long expirationTime) {
		this.expirationTime = expirationTime;
	}
	
	public long getNotBefore() {
		return notBefore;
	}
	public void setNotBefore(long notBefore) {
		this.notBefore = notBefore;
	}
	
	public String getFamilyName() {
		return familyName;
	}
	public void seFamilyName(String familyName) {
		this.familyName = familyName;
	}
	
	public String getGivenName() {
		return givenName;
	}
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}
	
	public String getUniqueName() {
		return uniqueName;
	}
	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}
	
	public String getUpn() {
		return upn;
	}
	public void setUpn(String upn) {
		this.upn = upn;
	}
	
	private Date getUnixEpochAsDate(long epoch) {
		// Epoch timestamps are in seconds,
		// but Jackson converts integers as milliseconds.
		// Rather than create a custom deserializer, this helper will do
		// the conversion.
		return new Date(epoch * 1000);
	}

	private boolean isValid() {
		// This method does some basic validation
		// For more information on validation of ID tokens, see
		// https://azure.microsoft.com/en-us/documentation/articles/active-directory-v2-tokens/#validating-tokens
		Date now = new Date();

		// Check expiration and not before times
		if (now.after(this.getUnixEpochAsDate(this.expirationTime))
				|| now.before(this.getUnixEpochAsDate(this.notBefore))) {
			// Token is not within it's valid "time"
			return false;
		}

		return true;
	}
}
