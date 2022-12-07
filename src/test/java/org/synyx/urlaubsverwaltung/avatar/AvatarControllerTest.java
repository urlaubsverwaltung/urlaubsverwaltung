package org.synyx.urlaubsverwaltung.avatar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Locale;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class AvatarControllerTest {

    private AvatarController sut;

    @Mock
    private SvgService svgService;

    @BeforeEach
    void setUp() {
        sut = new AvatarController(svgService);
    }

    @ParameterizedTest
    @CsvSource({
        "The Batman,TB",
        "Batman,B",
        "The ultimate Batman,TB",
    })
    void ensureGeneratesAvatarWithInitials(String name, String expectedInitials) throws Exception {

        when(svgService.createSvg("thymeleaf/svg/avatar", Locale.GERMAN, Map.of("initials", expectedInitials)))
            .thenReturn("<svg></svg>");

        perform(get("/web/avatar")
            .locale(Locale.GERMAN)
            .param("name", name))
            .andExpect(status().isOk())
            .andExpect(content().contentType("image/svg+xml"))
            .andExpect(header().string("Cache-Control", "max-age=3600"))
            .andExpect(content().string("<svg></svg>"));
    }

    /*TODO unify these tests when we have junit 5.8 with https://github.com/junit-team/junit5/issues/1100 */
    @Test
    void ensureGeneratesAvatarWithWhitespacesAtStart() throws Exception {

        when(svgService.createSvg("thymeleaf/svg/avatar", Locale.GERMAN, Map.of("initials", "B")))
            .thenReturn("<svg></svg>");

        perform(get("/web/avatar")
            .locale(Locale.GERMAN)
            .param("name", " Batman"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("image/svg+xml"))
            .andExpect(header().string("Cache-Control", "max-age=3600"))
            .andExpect(content().string("<svg></svg>"));
    }

    @Test
    void ensureGeneratesAvatarWithWhitespacesAtEnd() throws Exception {

        when(svgService.createSvg("thymeleaf/svg/avatar", Locale.GERMAN, Map.of("initials", "B")))
            .thenReturn("<svg></svg>");

        perform(get("/web/avatar")
            .locale(Locale.GERMAN)
            .param("name", "Batman "))
            .andExpect(status().isOk())
            .andExpect(content().contentType("image/svg+xml"))
            .andExpect(header().string("Cache-Control", "max-age=3600"))
            .andExpect(content().string("<svg></svg>"));
    }

    @Test
    void ensureGeneratesAvatarWithMultipleWhitespacesInTheMiddle() throws Exception {

        when(svgService.createSvg("thymeleaf/svg/avatar", Locale.GERMAN, Map.of("initials", "BM")))
            .thenReturn("<svg></svg>");

        perform(get("/web/avatar")
            .locale(Locale.GERMAN)
            .param("name", "Bat  man"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("image/svg+xml"))
            .andExpect(header().string("Cache-Control", "max-age=3600"))
            .andExpect(content().string("<svg></svg>"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
