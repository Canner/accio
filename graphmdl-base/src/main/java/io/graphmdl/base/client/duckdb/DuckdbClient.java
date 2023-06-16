/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.graphmdl.base.client.duckdb;

import io.airlift.log.Logger;
import io.graphmdl.base.client.AutoCloseableIterator;
import io.graphmdl.base.client.Client;
import io.graphmdl.base.client.ColumnDescription;
import io.graphmdl.base.client.jdbc.BaseJdbcRecordIterator;
import io.graphmdl.base.client.jdbc.JdbcRecordIterator;
import org.duckdb.DuckDBConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static io.graphmdl.base.client.jdbc.JdbcTypeMapping.toGraphMDLType;
import static java.lang.String.format;

public final class DuckdbClient
        implements Client
{
    private static final Logger LOG = Logger.get(DuckdbClient.class);
    private final Connection duckDBConnection;

    public DuckdbClient()
    {
        try {
            // The instance will be cleared after the process end. We don't need to
            // close this connection.
            this.duckDBConnection = DriverManager.getConnection("jdbc:duckdb:");
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AutoCloseableIterator<Object[]> query(String sql)
    {
        try {
            return JdbcRecordIterator.of(this, sql);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AutoCloseableIterator<ColumnDescription> describe(String sql)
    {
        try {
            return new ColumnMetadataIterator(this, "describe " + sql);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void executeDDL(String sql)
    {
        try (Connection connection = createConnection()) {
            Statement statement = connection.createStatement();
            statement.execute(sql);
        }
        catch (SQLException se) {
            throw new RuntimeException(se);
        }
    }

    // for canner use
    public void executeDDL(String sql, List<Object> parameters)
    {
        try (Connection connection = createConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }
            statement.execute();
        }
        catch (SQLException se) {
            throw new RuntimeException(se);
        }
    }

    @Override
    public List<String> listTables()
    {
        try (Connection connection = createConnection();
                ResultSet resultSet = connection.getMetaData().getTables(null, null, null, null)) {
            List<String> names = new ArrayList<>();
            while (resultSet.next()) {
                String tableName = resultSet.getString(3);
                names.add(tableName);
            }
            return names;
        }
        catch (SQLException se) {
            throw new RuntimeException(se);
        }
    }

    public void dropTableQuietly(String tableName)
    {
        try {
            executeDDL(format("BEGIN TRANSACTION;DROP TABLE IF EXISTS %s;COMMIT;", tableName));
        }
        catch (Exception e) {
            LOG.error(e, "Failed to drop table %s", tableName);
        }
    }

    static class ColumnMetadataIterator
            extends BaseJdbcRecordIterator<ColumnDescription>
            implements AutoCloseableIterator<ColumnDescription>
    {
        public ColumnMetadataIterator(Client client, String sql)
                throws SQLException
        {
            super(client, sql);
        }

        // The schema of a describe query in duckDB:
        // │ column_name ┆ column_type ┆ null ┆ key ┆ default ┆ extra │
        // ╞═════════════╪═════════════╪══════╪═════╪═════════╪═══════╡
        // │ 1           ┆ INTEGER     ┆ YES  ┆     ┆         ┆       │

        @Override
        public ColumnDescription getCurrentRecord()
                throws SQLException
        {
            return new ColumnDescription(
                    resultSet.getString(1),
                    toGraphMDLType(JDBCType.valueOf(resultSet.getString(2))));
        }
    }

    @Override
    public Connection createConnection()
            throws SQLException
    {
        // Refer to the official doc, if we want to create multiple read-write connections,
        // to the same database in-memory database instance, we can use the custom `duplicate()` method.
        // https://duckdb.org/docs/api/java

        return ((DuckDBConnection) duckDBConnection).duplicate();
    }
}
