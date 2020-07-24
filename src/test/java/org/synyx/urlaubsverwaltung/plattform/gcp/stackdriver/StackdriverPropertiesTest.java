package org.synyx.urlaubsverwaltung.plattform.gcp.stackdriver;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StackdriverPropertiesTest {

    @Test
    void whenPropertiesProjectIdIsSetAdapterProjectIdReturnsIt() {
        StackdriverProperties properties = new StackdriverProperties();
        properties.setProjectId("project-id");
        assertThat(new StackdriverPropertiesConfigAdapter(properties).projectId()).isEqualTo("project-id");
    }

    @Test
    void whenPropertiesResourceTypeIsSetAdapterResourceTypeReturnsIt() {
        StackdriverProperties properties = new StackdriverProperties();
        properties.setResourceType("resource-type");
        assertThat(new StackdriverPropertiesConfigAdapter(properties).resourceType()).isEqualTo("resource-type");
    }

}
