package org.synyx.urlaubsverwaltung.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.core.person.MailNotification;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;

import org.synyx.urlaubsverwaltung.security.AzureAuthHelper;
import org.synyx.urlaubsverwaltung.security.AzureSyncService;
import org.synyx.urlaubsverwaltung.security.AzureTokenResponse;

import org.synyx.urlaubsverwaltung.security.OutlookService;
import org.synyx.urlaubsverwaltung.security.OutlookServiceBuilder;
import org.synyx.urlaubsverwaltung.security.OutlookUser;

import org.synyx.urlaubsverwaltung.security.AzureIdToken;

@Component
@RequestMapping("/authorize")
public class AzureAuthorizeProvider implements AuthenticationProvider {

	private static final Logger LOG = Logger.getLogger(AzureAuthorizeProvider.class);

	private final PersonService personService;

	@Autowired
	AzureAuthHelper AuthHelper;
	@Autowired
	AzureSyncService azureSyncService;

	@Autowired
	public AzureAuthorizeProvider(PersonService personService) {
		this.personService = personService;
	}

	@Override
	public Authentication authenticate(Authentication authentication) {
		return null;
	}

	@RequestMapping(value = "", params = "code", method = RequestMethod.POST)
	public String authorize(@RequestParam("code") String code, @RequestParam("state") UUID state,
			HttpServletRequest request) {
		// Get the expected state value from the session
		writeRequestData(request);
		HttpSession session = request.getSession();
		UUID expectedState = (UUID) session.getAttribute("expected_state");
		session.removeAttribute("expected_state");

		// Make sure that the state query parameter returned matches
		// the expected state
		if (state.equals(expectedState)) {
			AzureTokenResponse tokenResponse = AuthHelper.getTokenFromAuthCode(code);
			AzureIdToken idTokenObj = AzureIdToken.parseEncodedToken(tokenResponse.getIdToken());
			if (idTokenObj != null) {
				OutlookService outlookService = OutlookServiceBuilder.getOutlookService(tokenResponse.getAccessToken(),
						null);
				OutlookUser user;
				try {
					user = outlookService.getCurrentUser().execute().body();
					session.setAttribute("userEmail", user.getMail());
					session.setAttribute("displayName", user.getDisplayName());
				} catch (IOException e) {
					session.setAttribute("error", e.getMessage());
				}
				String username = idTokenObj.getUniqueName();
				Optional<String> firstName = Optional.ofNullable(idTokenObj.getGivenName());
				Optional<String> lastName = Optional.ofNullable(idTokenObj.getFamilyName());
				Optional<String> email = Optional.ofNullable(idTokenObj.getUpn());

				Optional<Person> optionalPerson = personService.getPersonByLogin(username);
				Person person;

				if (optionalPerson.isPresent()) {
					Person existentPerson = optionalPerson.get();

					if (existentPerson.hasRole(Role.INACTIVE)) {
						LOG.info("User '" + username + "' has been deactivated and can not sign in therefore");
					}

					person = azureSyncService.syncPerson(existentPerson, firstName, lastName, email);
				} else {
					LOG.info("No user found for username '" + username + "'");

					person = azureSyncService.createPerson(username, firstName, lastName, email);
				}

				/**
				 * NOTE: If the system has no office user yet, grant office permissions to
				 * successfully signed in user
				 */
				boolean noOfficeUserYet = personService.getPersonsByRole(Role.OFFICE).isEmpty();

				if (noOfficeUserYet) {
					azureSyncService.appointPersonAsOfficeUser(person);
				}

				UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, null,
						getGrantedAuthorities(person));
				SecurityContextHolder.getContext().setAuthentication(token);

			} else {
				LOG.info("No user information available!");
			}
		}
		return "redirect:/";
	}

	@RequestMapping(value = "", params = "error", method = RequestMethod.POST)
	public String error(@RequestParam("error") String error,
			@RequestParam("error_description") String errorDescription) {

		LOG.info(error);
		LOG.info(errorDescription);

		throw new BadCredentialsException(errorDescription);

	}

	public void writeRequestData(HttpServletRequest req) {
		Enumeration<String> parameterNames = req.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String paramName = parameterNames.nextElement();

			System.out.println(paramName);
			String[] paramValues = req.getParameterValues(paramName);
			for (int i = 0; i < paramValues.length; i++) {
				String paramValue = paramValues[i];
				System.out.println(paramValue);
			}
		}
	}

	Person createPerson(String login, Optional<String> firstName, Optional<String> lastName,
			Optional<String> mailAddress) {

		Assert.notNull(login, "Missing login name!");

		Person person = personService.create(login, lastName.orElse(null), firstName.orElse(null),
				mailAddress.orElse(null), Collections.singletonList(MailNotification.NOTIFICATION_USER),
				Collections.singletonList(Role.USER));

		LOG.info("Successfully auto-created person: " + person.toString());

		return person;
	}

	/**
	 * Gets the granted authorities using the {@link Role}s of the given
	 * {@link Person}.
	 *
	 * @param person
	 *            to get the granted authorities for, may not be {@code null}
	 *
	 * @return the granted authorities for the person
	 */
	Collection<GrantedAuthority> getGrantedAuthorities(Person person) {

		Assert.notNull(person, "Person must be given.");

		Collection<Role> permissions = person.getPermissions();

		if (permissions.isEmpty()) {
			throw new IllegalStateException("Every user must have at least one role, data seems to be corrupt.");
		}

		Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();

		permissions.stream().forEach(role -> grantedAuthorities.add(role::name));

		return grantedAuthorities;
	}

	@Override
	public boolean supports(Class<?> authentication) {

		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

}
