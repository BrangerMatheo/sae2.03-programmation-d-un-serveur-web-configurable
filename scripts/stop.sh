#!/bin/bash
PID_FILE="$HOME/serveurWeb/run/myweb.pid"
if [ ! -f "$PID_FILE" ]; then
    echo "Fichier PID introuvable : $PID_FILE"
    exit 1
fi
PID=$(cat "$PID_FILE")
echo "Arrêt du serveur (PID $PID)..."
kill "$PID" && rm -f "$PID_FILE"
echo "Serveur arrêté."
