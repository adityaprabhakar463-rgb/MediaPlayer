#!/bin/bash
cd "$(dirname "$0")"
if [ -f backend.pid ]; then
    kill $(cat backend.pid) 2>/dev/null
    rm backend.pid
    echo "Celestial backend stopped."
else
    echo "No backend.pid found."
fi
