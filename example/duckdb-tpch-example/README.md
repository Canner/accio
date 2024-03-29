# Description

This is an example Docker Compose project for DuckDB data source with some tpch data.
You can learn how to set up the Wren Engine with DuckDB data source to analyze your data.

# How to use

1. Set up the platform in `.env` file. (`linux/amd64`, `linux/arm64`)
2. Configure settings in the `etc/config.properties` file.
3. Place your data in `etc/data` file after removing the sample data files.
4. Set up your initial SQL and session SQL in `etc/duckdb-init.sql` and `etc/duckdb-session.sql` files.
5. Place your MDL in `etc/mdl` file after removing the sample MDL file `etc/mdl/sample.json`.
    - The `mdl` directory should contain only one json file.
6. Set up the accounts if you needs or remove the sample accounts if you don't need.
    - Sample accounts are provided in the `etc/accounts` directory.
7. Run the docker-compose in this directory.
    ```bash
    docker compose --env-file .env up
    ```
8. Connect using psql or another PostgreSQL driver using port 7432.
    - Sample usernames and passwords are `ina` and `wah`, or `azki` and `guess`.
    - The default database name should match the catalog of the MDL file.
    - The default schema name should match the schema of the MDL file.
   ```bash
    psql 'host=localhost user=ina dbname=wren port=7432 options=--search_path=tpch'
    ```
