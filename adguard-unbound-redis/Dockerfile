# syntax=docker/dockerfile:1.7
ARG BUILD_FROM=ghcr.io/hassio-addons/base:20.0.1
ARG ALPINE_VERSION=3.23
ARG UPSTREAM_IMAGE=imthai/adguardhome-unbound-redis:latest

#############################################
# Stage 1: Upstream (extract binaries + configs)
#############################################
# hadolint ignore=DL3006
FROM ${UPSTREAM_IMAGE} AS upstream

#############################################
# Stage 2: Builder (Compiles Unbound for HA base)
#############################################
FROM alpine:${ALPINE_VERSION} AS builder

ARG UNBOUND_URL="https://nlnetlabs.nl/downloads/unbound/unbound-latest.tar.gz"

WORKDIR /build

# Install all packages required for compiling
# hadolint ignore=DL3018
RUN apk add --no-cache \
    build-base \
    curl \
    openssl-dev \
    expat-dev \
    hiredis-dev \
    libevent-dev \
    libcap-dev \
    perl \
    linux-headers \
    wget

# Compile Unbound from source with hiredis (cachedb) support
WORKDIR /tmp/src
RUN set -eux; \
    wget --progress=dot:giga "${UNBOUND_URL}" -O /tmp/unbound.tar.gz; \
    tar -xzf /tmp/unbound.tar.gz --strip-components=1 -C .; \
    ./configure \
        --prefix=/usr \
        --sysconfdir=/etc \
        --localstatedir=/var \
        --with-libhiredis \
        --with-libexpat=/usr \
        --with-libevent \
        --enable-cachedb \
        --disable-flto \
        --disable-shared \
        --with-pthreads; \
    make -j"$(nproc)"; \
    make install DESTDIR=/build/install


#############################################
# Stage 3: Runtime (Home Assistant base)
#############################################
# hadolint ignore=DL3006
FROM ${BUILD_FROM}

LABEL \
    org.opencontainers.image.source="https://github.com/PedroCPereiraDev/hassos_addon_adguard_unbound_redis" \
    org.opencontainers.image.description="AdGuard Home with Unbound & Redis - Home Assistant Add-on" \
    org.opencontainers.image.licenses="MIT"

# Install runtime dependencies
# hadolint ignore=DL3018
RUN sed -i -E 's/^(lib(crypto|ssl)3)=.*/\1/' /etc/apk/world && \
    apk upgrade --no-cache && \
    apk add --no-cache \
    redis \
    nginx \
    yq-go \
    hiredis \
    libevent \
    expat \
    libcap \
    openssl \
    ca-certificates \
    netcat-openbsd

# Create Unbound user and necessary directories
RUN addgroup -S unbound \
    && adduser -S unbound -G unbound \
    && mkdir -p /etc/unbound/var \
    && chown -R unbound:unbound /etc/unbound

# Copy compiled Unbound binaries from builder stage
COPY --from=builder /build/install/usr/sbin/unbound /usr/sbin/unbound
COPY --from=builder /build/install/usr/sbin/unbound-anchor /usr/sbin/unbound-anchor
COPY --from=builder /build/install/usr/sbin/unbound-checkconf /usr/sbin/unbound-checkconf
COPY --from=builder /build/install/usr/sbin/unbound-control /usr/sbin/unbound-control

# Copy AdGuardHome binary from upstream (Go binary — fully self-contained)
COPY --from=upstream /opt/AdGuardHome/AdGuardHome /opt/AdGuardHome/AdGuardHome

# Copy root filesystem overlay (S6 services, nginx configs)
COPY rootfs /

# Copy default configuration files from upstream (dynamic — always in sync)
# This MUST come AFTER "COPY rootfs /" so upstream defaults are authoritative.
# Upstream stores them at /config_default/, we place them at /etc/defaults/.
COPY --from=upstream /config_default/ /etc/defaults/

# Ensure S6 scripts are executable
RUN chmod +x \
    /etc/s6-overlay/s6-rc.d/init-config/run \
    /etc/s6-overlay/s6-rc.d/init-nginx/run \
    /etc/s6-overlay/s6-rc.d/svc-redis/run \
    /etc/s6-overlay/s6-rc.d/svc-redis/finish \
    /etc/s6-overlay/s6-rc.d/svc-unbound/run \
    /etc/s6-overlay/s6-rc.d/svc-unbound/finish \
    /etc/s6-overlay/s6-rc.d/svc-adguard/run \
    /etc/s6-overlay/s6-rc.d/svc-adguard/finish \
    /etc/s6-overlay/s6-rc.d/svc-nginx/run \
    /etc/s6-overlay/s6-rc.d/svc-nginx/finish

# Remove default nginx config (replaced by our own)
RUN rm -rf /etc/nginx/http.d /etc/nginx/conf.d 2>/dev/null || true

# Set configuration environment variable (matches upstream)
ENV XDG_CONFIG_HOME=/config

# Build arguments for labels
ARG BUILD_ARCH
ARG BUILD_DATE
ARG BUILD_DESCRIPTION="All-in-one DNS: AdGuard Home + Unbound + Redis"
ARG BUILD_NAME="AdGuard Home with Unbound & Redis"
ARG BUILD_REF
ARG BUILD_REPOSITORY="PedroCPereiraDev/hassos_addon_adguard_unbound_redis"
ARG BUILD_VERSION

# Standard Home Assistant add-on labels
# hadolint ignore=DL3048
LABEL \
    io.hass.name="${BUILD_NAME}" \
    io.hass.description="${BUILD_DESCRIPTION}" \
    io.hass.arch="${BUILD_ARCH}" \
    io.hass.type="addon" \
    io.hass.version=${BUILD_VERSION} \
    maintainer="PedroCPereiraDev" \
    org.opencontainers.image.title="${BUILD_NAME}" \
    org.opencontainers.image.description="${BUILD_DESCRIPTION}" \
    org.opencontainers.image.source="https://github.com/${BUILD_REPOSITORY}" \
    org.opencontainers.image.created=${BUILD_DATE} \
    org.opencontainers.image.revision=${BUILD_REF} \
    org.opencontainers.image.version=${BUILD_VERSION}
