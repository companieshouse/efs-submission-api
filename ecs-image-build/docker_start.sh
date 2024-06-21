#!/bin/bash

# Start script for efs-submission-api


PORT=8080
exec java -jar -Dserver.port="${PORT}" "efs-submission-api.jar"
