#!/bin/bash
BIN_DIR="$(dirname "$0")/../out/production/sae2.03-programmation-d-un-serveur-web-configurable"
CONF_DIR="$(dirname "$0")/../conf.d"
exec java -cp "$BIN_DIR" ServeurWeb "$CONF_DIR"
