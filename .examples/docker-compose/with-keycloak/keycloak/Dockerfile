FROM quay.io/keycloak/keycloak:20.0.1 as builder

#
# See https://www.keycloak.org/server/all-config?f=build for all build configuration options
#

# Cluster
ENV KC_CACHE=local

ENV KC_HEALTH_ENABLED=true

# HTTP/TLS
ENV KC_HTTP_RELATIVE_PATH=/auth

COPY import/ /tmp/realm-import

#
# custom quarkus build
#

# Build keycloak with all bell and whistles
RUN /opt/keycloak/bin/kc.sh build

# Import preconfigured realms
RUN /opt/keycloak/bin/kc.sh import --dir /tmp/realm-import


FROM quay.io/keycloak/keycloak:20.0.1
COPY --from=builder /opt/keycloak/ /opt/keycloak/
WORKDIR /opt/keycloak
ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "--verbose", "start-dev"]
