package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SickNoteExtensionInteractionService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;

/**
 * The unit tests of {@link SickNoteViewController} use a standalone {@code MockMvc} setup, which does not apply method
 * security. This test therefore covers the {@code @PreAuthorize} layer of the sick note endpoints.
 */
@SpringBootTest
class SickNoteViewControllerSecurityIT extends SingleTenantTestContainersBase {

    private static final long SICK_NOTE_ID = 1L;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PersonService personService;
    @MockitoBean
    private SickNoteService sickNoteService;
    @MockitoBean
    private SickNoteInteractionService sickNoteInteractionService;
    @MockitoBean
    private SickNoteExtensionInteractionService sickNoteExtensionInteractionService;
    @MockitoBean
    private SickNoteCommentService sickNoteCommentService;


    @ParameterizedTest
    @ValueSource(strings = {"USER", "BOSS", "SICK_NOTE_VIEW", "SICK_NOTE_ADD", "SICK_NOTE_CANCEL", "SICK_NOTE_COMMENT"})
    void ensureAcceptSickNoteExtensionIsForbiddenWithoutSickNoteEditRole(final String role) throws Exception {
        perform(post("/web/sicknote/" + SICK_NOTE_ID + "/extension/accept")
            .with(oidcLogin().authorities(signIn("USER", role)))
            .with(csrf())
        ).andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"OFFICE", "SICK_NOTE_EDIT"})
    void ensureAcceptSickNoteExtensionIsAllowedForRole(final String role) throws Exception {
        perform(post("/web/sicknote/" + SICK_NOTE_ID + "/extension/accept")
            .with(oidcLogin().authorities(signIn("USER", "BOSS", role)))
            .with(csrf())
        ).andExpect(status().is3xxRedirection());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "BOSS", "SICK_NOTE_VIEW", "SICK_NOTE_ADD", "SICK_NOTE_CANCEL", "SICK_NOTE_COMMENT"})
    void ensureAcceptSickNoteIsForbiddenWithoutSickNoteEditRole(final String role) throws Exception {
        perform(post("/web/sicknote/" + SICK_NOTE_ID + "/accept")
            .with(oidcLogin().authorities(signIn("USER", role)))
            .with(csrf())
        ).andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"OFFICE", "SICK_NOTE_EDIT"})
    void ensureAcceptSickNoteIsAllowedForRole(final String role) throws Exception {
        perform(post("/web/sicknote/" + SICK_NOTE_ID + "/accept")
            .with(oidcLogin().authorities(signIn("USER", "BOSS", role)))
            .with(csrf())
        ).andExpect(status().is3xxRedirection());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "BOSS", "SICK_NOTE_VIEW", "SICK_NOTE_ADD", "SICK_NOTE_EDIT"})
    void ensureCancelSickNoteIsForbiddenWithoutSickNoteCancelRole(final String role) throws Exception {
        perform(post("/web/sicknote/" + SICK_NOTE_ID + "/cancel")
            .with(oidcLogin().authorities(signIn("USER", role)))
            .with(csrf())
        ).andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"OFFICE", "SICK_NOTE_CANCEL"})
    void ensureCancelSickNoteIsAllowedForRole(final String role) throws Exception {
        perform(post("/web/sicknote/" + SICK_NOTE_ID + "/cancel")
            .with(oidcLogin().authorities(signIn("USER", "BOSS", role)))
            .with(csrf())
            .param("text", "cancelled because of a mistake")
        ).andExpect(status().is3xxRedirection());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "BOSS", "SICK_NOTE_VIEW", "SICK_NOTE_ADD", "SICK_NOTE_EDIT"})
    void ensureCommentSickNoteIsForbiddenWithoutSickNoteCommentRole(final String role) throws Exception {
        perform(post("/web/sicknote/" + SICK_NOTE_ID + "/comment")
            .with(oidcLogin().authorities(signIn("USER", role)))
            .with(csrf())
        ).andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"OFFICE", "SICK_NOTE_COMMENT"})
    void ensureCommentSickNoteIsAllowedForRole(final String role) throws Exception {
        perform(post("/web/sicknote/" + SICK_NOTE_ID + "/comment")
            .with(oidcLogin().authorities(signIn("USER", "BOSS", role)))
            .with(csrf())
            .param("text", "a comment")
        ).andExpect(status().is3xxRedirection());
    }

    private SimpleGrantedAuthority[] signIn(String... roles) {

        final Person signedInUser = new Person();
        signedInUser.setId(1L);
        signedInUser.setPermissions(Arrays.stream(roles).map(Role::valueOf).toList());

        final Person sickNotePerson = new Person();
        sickNotePerson.setId(2L);

        final SickNote sickNote = SickNote.builder()
            .id(SICK_NOTE_ID)
            .person(sickNotePerson)
            .startDate(LocalDate.of(2025, 8, 4))
            .endDate(LocalDate.of(2025, 8, 8))
            .status(ACTIVE)
            .build();

        when(personService.getSignedInUser()).thenReturn(signedInUser);
        when(sickNoteService.getById(SICK_NOTE_ID)).thenReturn(Optional.of(sickNote));
        when(sickNoteInteractionService.accept(any(), any(), any())).thenReturn(sickNote);
        when(sickNoteInteractionService.cancel(any(), any(), any())).thenReturn(sickNote);
        when(sickNoteCommentService.getCommentsBySickNote(any())).thenReturn(List.of());

        return Arrays.stream(roles).map(SimpleGrantedAuthority::new).toArray(SimpleGrantedAuthority[]::new);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
