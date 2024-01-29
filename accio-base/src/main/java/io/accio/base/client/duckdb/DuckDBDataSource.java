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

package io.accio.base.client.duckdb;

import org.duckdb.DuckDBConnection;
import org.postgresql.ds.common.BaseDataSource;

import javax.sql.DataSource;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static java.lang.String.format;

public class DuckDBDataSource
        extends BaseDataSource
        implements DataSource, Serializable
{
    private final DuckDBConnection duckDBConnection;
    private final DuckdbS3StyleStorageConfig duckdbS3StyleStorageConfig;

    public DuckDBDataSource(DuckDBConnection duckDBConnection, DuckdbS3StyleStorageConfig duckdbS3StyleStorageConfifg)
    {
        this.duckDBConnection = duckDBConnection;
        this.duckdbS3StyleStorageConfig = duckdbS3StyleStorageConfifg;
    }

    @Override
    public String getDescription()
    {
        return "Non-Pooling DataSource from DuckDB";
    }

    @Override
    public Connection getConnection()
            throws SQLException
    {
        // Refer to the official doc, if we want to create multiple read-write connections,
        // to the same database in-memory database instance, we can use the custom `duplicate()` method.
        // https://duckdb.org/docs/api/java
        Connection connection = duckDBConnection.duplicate();
        Statement statement = connection.createStatement();
        statement.execute("set search_path = 'main'");
        // install extensions from stable repository
        statement.execute("SET custom_extension_repository = 'http://extensions.duckdb.org'");
        // init httpfs settings
        statement.execute(format("SET s3_endpoint='%s'\n", duckdbS3StyleStorageConfig.getEndpoint()));
        statement.execute(format("SET s3_url_style='%s'\n", duckdbS3StyleStorageConfig.getUrlStyle()));
        return connection;
    }

    public boolean isWrapperFor(Class<?> iface)
            throws SQLException
    {
        return iface.isAssignableFrom(getClass());
    }

    public <T> T unwrap(Class<T> iface)
            throws SQLException
    {
        if (iface.isAssignableFrom(getClass())) {
            return iface.cast(this);
        }
        throw new SQLException("Cannot unwrap to " + iface.getName());
    }

    private void writeObject(ObjectOutputStream out)
            throws IOException
    {
        writeBaseObject(out);
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException
    {
        readBaseObject(in);
    }
}
