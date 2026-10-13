#!/bin/sh
set -eu

case "${DATABASE_URL:-}" in
    postgres://*|postgresql://*)
        export SPRING_DATASOURCE_URL="$(printf '%s' "$DATABASE_URL" | sed -E 's|^postgres(ql)?://[^@]+@|jdbc:postgresql://|')"
        ;;
esac

exec java -Dserver.port="${PORT:-8080}" -jar /app/app.jar