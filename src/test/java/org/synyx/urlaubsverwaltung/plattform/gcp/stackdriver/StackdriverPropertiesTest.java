package org.synyx.urlaubsverwaltung.plattform.gcp.stackdriver;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StackdriverPropertiesTest {

    @Test
    public void whenPropertiesProjectIdIsSetAdapterProjectIdReturnsIt() {
        StackdriverProperties properties = new StackdriverProperties();
        properties.setProjectId("project-id");
        assertThat(new StackdriverPropertiesConfigAdapter(properties).projectId()).isEqualTo("project-id");
    }

    @Test
    public void whenPropertiesResourceTypeIsSetAdapterResourceTypeReturnsIt() {
        StackdriverProperties properties = new StackdriverProperties();
        properties.setResourceType("resource-type");
        assertThat(new StackdriverPropertiesConfigAdapter(properties).resourceType()).isEqualTo("resource-type");
    }

}
