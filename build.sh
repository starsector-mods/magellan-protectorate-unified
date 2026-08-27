#!/bin/bash
set -e

# Ensure we're in the mod directory
cd "$(dirname "$0")"

mvn clean package -DskipTests

echo "Magellan.jar built successfully via Maven"
