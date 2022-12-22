package org.synyx.urlaubsverwaltung.department.web;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DepartmentOverviewDtoTest {

    @Test
    void ensureEquals() {

        final DepartmentOverviewDto d1 = new DepartmentOverviewDto();
        d1.setId(1L);
        d1.setName("d1");
        final DepartmentOverviewDto d2 = new DepartmentOverviewDto();
        d2.setId(1L);
        d2.setName("d2");
        final DepartmentOverviewDto d3 = new DepartmentOverviewDto();
        d3.setId(2L);
        d3.setName("d3");

        assertThat(d1)
            .isEqualTo(d2)
            .isNotEqualTo(d3);
    }

    @Test
    void ensureHashCode() {

        final DepartmentOverviewDto d1 = new DepartmentOverviewDto();
        d1.setId(1L);
        d1.setName("d1");
        final DepartmentOverviewDto d2 = new DepartmentOverviewDto();
        d2.setId(1L);
        d2.setName("d2");
        final DepartmentOverviewDto d3 = new DepartmentOverviewDto();
        d3.setId(2L);
        d3.setName("d3");

        assertThat(d1.hashCode())
            .isEqualTo(d2.hashCode())
            .isNotEqualTo(d3.hashCode());
    }
}
