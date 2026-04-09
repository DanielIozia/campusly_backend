#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE campusly_local;
    CREATE DATABASE campusly_demo;
    CREATE DATABASE campusly_prod;
EOSQL
