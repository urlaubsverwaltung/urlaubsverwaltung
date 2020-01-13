package org.synyx.urlaubsverwaltung.plattform.gcp.stackdriver;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
@ConditionalOnProperty(prefix = "gcp.resourcetype.labels", name = "enabled", havingValue = "true")
public class GcpResourceTypeLabelMeterRegistryCustomizer {

    private GcpProperties gcpProperties;

    @Autowired
    public GcpResourceTypeLabelMeterRegistryCustomizer(GcpProperties gcpProperties) {
        this.gcpProperties = gcpProperties;
    }

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags(gcpProperties.toTags());
    }

    @Configuration
    @ConfigurationProperties(prefix = "gcp.resourcetype.labels")
    public static class GcpProperties {

        /**
         * Location of the kubernetes cluster
         */
        private String location;

        /**
         * Name of the kubernetes cluster
         */
        private String clusterName;

        /**
         * Namespace of urlaubsverwaltung pod
         */
        private String namespaceName;

        /*
         * Name of the urlaubsverwaltung pod
         */
        private String podName;

        /**
         * Name of the urlaubsverwaltung container
         */
        private String containerName;

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getClusterName() {
            return clusterName;
        }

        public void setClusterName(String clusterName) {
            this.clusterName = clusterName;
        }

        public String getNamespaceName() {
            return namespaceName;
        }

        public void setNamespaceName(String namespaceName) {
            this.namespaceName = namespaceName;
        }

        public String getPodName() {
            return podName;
        }

        public void setPodName(String podName) {
            this.podName = podName;
        }

        public String getContainerName() {
            return containerName;
        }

        public void setContainerName(String containerName) {
            this.containerName = containerName;
        }

        public boolean isValid() {
            return location != null && clusterName != null && namespaceName != null && podName != null && containerName != null;
        }

        public List<Tag> toTags() {

            if(!isValid()) {
                return Collections.emptyList();
            }

            List<Tag> tags = new ArrayList<>();
            tags.add(Tag.of("location", location));
            tags.add(Tag.of("cluster_name", clusterName));
            tags.add(Tag.of("namespace_name", namespaceName));
            tags.add(Tag.of("pod_name", podName));
            tags.add(Tag.of("container_name", containerName));
            return tags;
        }
    }

}
