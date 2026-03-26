# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.107.73] - 2026-03-25

### Added

- Initial release
- AdGuard Home with Unbound recursive DNS resolver and Redis in-memory caching
- Web UI access on port 3000 ("Open Web UI" button in sidebar)
- S6-overlay v3 process supervision for all services
- Automatic HAOS CoreDNS coexistence (dns.bind_hosts auto-configuration)
- Config folder fully compatible with standalone Docker container for multi-server sync
- Custom AppArmor security profile
- Multi-architecture support (amd64, aarch64)
- Unbound compiled from source with hiredis/cachedb support
- Default DNS-over-TLS forwarding to Cloudflare DNS
- Pre-configured ad blocklists (AdGuard DNS filter, HaGeZi's TIF & Pro)
- DNSSEC validation enabled by default
- Redis cachedb via Unix socket for minimal latency
- Automatic upstream tracking with daily CI checks and smoke-tested builds
- Dynamic config extraction from upstream Docker image (always in sync)
- Version scheme follows upstream AdGuard Home releases
