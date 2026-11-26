#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE identity_db;
    CREATE DATABASE academic_db;
    CREATE DATABASE learning_db;
    CREATE DATABASE assessment_db;
    CREATE DATABASE notification_db;
    CREATE DATABASE report_db;
    CREATE DATABASE ai_db;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "ai_db" <<-EOSQL
    CREATE EXTENSION IF NOT EXISTS vector;
EOSQL
