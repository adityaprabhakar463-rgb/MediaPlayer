#!/bin/bash
cd "$(dirname "$0")"
echo "Starting Celestial backend server..."
./gradlew :backend:run &
echo $! > backend.pid
echo "Backend started with PID $(cat backend.pid). Port 8080."
