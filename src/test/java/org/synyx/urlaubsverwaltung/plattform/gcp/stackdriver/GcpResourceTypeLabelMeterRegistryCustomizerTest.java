package org.synyx.urlaubsverwaltung.plattform.gcp.stackdriver;


import io.micrometer.core.instrument.Tag;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GcpResourceTypeLabelMeterRegistryCustomizerTest {

    @Test
    public void withAllPropertiesSetIsValid() {

        GcpResourceTypeLabelMeterRegistryCustomizer.GcpProperties gcpProperties = new GcpResourceTypeLabelMeterRegistryCustomizer.GcpProperties();

        gcpProperties.setLocation("eu-west-1b");
        gcpProperties.setClusterName("my-k8s-cluster");
        gcpProperties.setNamespaceName("my-uv-namespace");
        gcpProperties.setPodName("deployment-76f9689956-q8tbx");
        gcpProperties.setContainerName("my-urlaubsverwaltung-container");

        assertThat(gcpProperties.isValid()).isTrue();
    }

    @Test
    public void returnsTags() {

        GcpResourceTypeLabelMeterRegistryCustomizer.GcpProperties gcpProperties = new GcpResourceTypeLabelMeterRegistryCustomizer.GcpProperties();

        gcpProperties.setLocation("eu-west-1b");
        gcpProperties.setClusterName("my-k8s-cluster");
        gcpProperties.setNamespaceName("my-uv-namespace");
        gcpProperties.setPodName("deployment-76f9689956-q8tbx");
        gcpProperties.setContainerName("my-urlaubsverwaltung-container");

        assertThat(gcpProperties.toTags()).containsOnly(
                Tag.of("location", "eu-west-1b"),
                Tag.of("cluster_name", "my-k8s-cluster"),
                Tag.of("namespace_name", "my-uv-namespace"),
                Tag.of("pod_name", "deployment-76f9689956-q8tbx"),
                Tag.of("container_name", "my-urlaubsverwaltung-container")
        );
    }

    @Test
    public void returnsEmptyListOfTags() {

        GcpResourceTypeLabelMeterRegistryCustomizer.GcpProperties gcpProperties = new GcpResourceTypeLabelMeterRegistryCustomizer.GcpProperties();

        assertThat(gcpProperties.toTags()).isEmpty();
    }
}
