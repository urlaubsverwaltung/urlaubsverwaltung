package org.synyx.urlaubsverwaltung.absence.web;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class VacationOverviewViewControllerTest {

    @Test
    void ensureRedirectToAbsenceOverview() throws Exception {

        final var vacationOverviewViewController = new VacationOverviewViewController();
        final var mockMvc = standaloneSetup(vacationOverviewViewController).build();

        mockMvc.perform(get("/web/application/vacationoverview"))
            .andExpect(status().isMovedPermanently())
            .andExpect(redirectedUrl("/web/absences"));
    }
}
