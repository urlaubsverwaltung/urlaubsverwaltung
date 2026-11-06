package org.synyx.urlaubsverwaltung.companyclosure.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosure;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureService;

import java.time.LocalDate;
import java.util.List;

import static java.time.Month.DECEMBER;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;

@ExtendWith(MockitoExtension.class)
class CompanyClosureApiControllerTest {

    private CompanyClosureApiController sut;

    @Mock
    private CompanyClosureService companyClosureService;

    @BeforeEach
    void setUp() {
        sut = new CompanyClosureApiController(companyClosureService);
    }

    @Test
    void returnsCompanyClosuresInRange() throws Exception {
        final LocalDate from = LocalDate.of(2026, DECEMBER, 24);
        final LocalDate to = LocalDate.of(2026, DECEMBER, 26);
        when(companyClosureService.getCompanyClosures(from, to)).thenReturn(List.of(new CompanyClosure(from, NOON, "Heiligabend")));

        perform(get("/api/company-closures")
            .param("from", "2026-12-24")
            .param("to", "2026-12-26"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("""
                {"companyClosures":[{"date":"2026-12-24","description":"Heiligabend","dayLength":0.5,"absencePeriodName":"NOON"}]}
                """, true));
    }

    @Test
    void badRequestWhenFromAfterTo() throws Exception {
        perform(get("/api/company-closures")
            .param("from", "2016-01-01")
            .param("to", "2015-01-01"))
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertInstanceOf(ResponseStatusException.class, result.getResolvedException()));

        verifyNoInteractions(companyClosureService);
    }

    @Test
    void badRequestWhenPeriodTooLong() throws Exception {
        perform(get("/api/company-closures")
            .param("from", "2026-01-01")
            .param("to", "+999999998-12-31"))
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertInstanceOf(ResponseStatusException.class, result.getResolvedException()));

        verifyNoInteractions(companyClosureService);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
