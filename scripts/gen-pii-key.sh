#!/usr/bin/env bash
# Genera una PII_ENCRYPTION_KEY de 32 bytes en base64 para pegar en .env
set -euo pipefail
key=$(head -c 32 /dev/urandom | base64)
echo "PII_ENCRYPTION_KEY=${key}"
echo
echo "Guarda esta línea en tu .env local o en el gestor de secretos."
echo "NO la subas al repositorio."