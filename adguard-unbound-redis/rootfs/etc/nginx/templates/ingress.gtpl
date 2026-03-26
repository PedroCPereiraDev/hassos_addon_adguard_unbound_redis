server {
    listen {{ .interface }}:{{ .port }} default_server;

    include /etc/nginx/includes/server_params.conf;
    include /etc/nginx/includes/proxy_params.conf;

    # Absolute base URL for rewriting AdGuard Home's redirects and links.
    # The Supervisor proxies /api/hassio_ingress/<token>/* to this server,
    # but AdGuard Home generates absolute paths (/login.html, /control/*, etc.)
    # that must be prefixed with the ingress entry path.
    set $ingress_entry {{ .ingress_entry }};

    location / {
        allow   172.30.32.2;
        deny    all;

        proxy_pass http://backend;

        # Rewrite Location headers so redirects stay within ingress
        proxy_redirect / {{ .ingress_entry }}/;

        # Rewrite absolute paths in HTML/JS/CSS responses
        sub_filter_once off;
        sub_filter_types text/html application/javascript text/css;
        sub_filter 'href="/' 'href="{{ .ingress_entry }}/';
        sub_filter 'src="/' 'src="{{ .ingress_entry }}/';
        sub_filter 'action="/' 'action="{{ .ingress_entry }}/';
        sub_filter 'url(/' 'url({{ .ingress_entry }}/';
        sub_filter '"/login.html' '"{{ .ingress_entry }}/login.html';

        # Ensure gzip responses are decompressed before sub_filter applies
        proxy_set_header Accept-Encoding "";
    }
}
