FROM quay.io/keycloak/keycloak:22.0.4 AS builder
#
# See https://www.keycloak.org/server/all-config?f=build for all build configuration options
#
ENV KC_CACHE=local
COPY export/ /tmp/realm-import
RUN /opt/keycloak/bin/kc.sh build
RUN /opt/keycloak/bin/kc.sh import --dir /tmp/realm-import

FROM quay.io/keycloak/keycloak:22.0.4
COPY --from=builder /opt/keycloak/ /opt/keycloak/
WORKDIR /opt/keycloak
ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "--verbose", "start-dev"]
