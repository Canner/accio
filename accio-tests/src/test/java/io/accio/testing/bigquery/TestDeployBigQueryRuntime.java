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

package io.accio.testing.bigquery;

import com.google.common.collect.ImmutableMap;
import io.accio.base.dto.Manifest;
import io.accio.testing.TestingAccioServer;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static io.accio.base.config.ConfigManager.ConfigEntry.configEntry;
import static io.accio.testing.WebApplicationExceptionAssert.assertWebApplicationException;
import static java.lang.System.getenv;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class TestDeployBigQueryRuntime
        extends AbstractWireProtocolTestWithBigQuery
{
    @Override
    protected TestingAccioServer createAccioServer()
            throws Exception
    {
        ImmutableMap.Builder<String, String> properties = ImmutableMap.builder();
        try {
            Path dir = Files.createTempDirectory(getAccioDirectory());
            if (getAccioMDLPath().isPresent()) {
                Files.copy(Path.of(getAccioMDLPath().get()), dir.resolve("mdl.json"));
            }
            else {
                Files.write(dir.resolve("manifest.json"), Manifest.MANIFEST_JSON_CODEC.toJsonBytes(DEFAULT_MANIFEST));
            }
            properties.put("accio.directory", dir.toString());
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return TestingAccioServer.builder()
                .setRequiredConfigs(properties.build())
                .build();
    }

    @Test
    public void testDeployBigQueryRuntime()
    {
        patchConfig(List.of(
                configEntry("bigquery.project-id", getenv("TEST_BIG_QUERY_PROJECT_ID")),
                configEntry("bigquery.location", "asia-east1"),
                configEntry("bigquery.credentials-key", getenv("TEST_BIG_QUERY_CREDENTIALS_BASE64_JSON")),
                configEntry("accio.datasource.type", "bigquery")));
        assertThatNoException().isThrownBy(() -> {
            try (Connection connection = createConnection()) {
                Statement statement = connection.createStatement();
                statement.execute("SELECT count(*) from Orders");
                ResultSet resultSet = statement.getResultSet();
                resultSet.next();
            }
        });

        updateConfig(List.of(
                configEntry("bigquery.project-id", getenv("TEST_BIG_QUERY_PROJECT_ID")),
                configEntry("bigquery.location", "asia-east1"),
                configEntry("bigquery.credentials-key", getenv("TEST_BIG_QUERY_CREDENTIALS_BASE64_JSON")),
                configEntry("accio.datasource.type", "bigquery")));
        assertThatNoException().isThrownBy(() -> {
            try (Connection connection = createConnection()) {
                Statement statement = connection.createStatement();
                statement.execute("SELECT count(*) from Orders");
                ResultSet resultSet = statement.getResultSet();
                resultSet.next();
            }
        });
    }

    @Test
    public void testFakeCredentials()
    {
        assertWebApplicationException(() -> patchConfig(List.of(
                configEntry("bigquery.project-id", getenv("TEST_BIG_QUERY_PROJECT_ID")),
                configEntry("bigquery.location", "asia-east1"),
                configEntry("bigquery.credentials-key", "fake"),
                configEntry("accio.datasource.type", "bigquery"))))
                .hasErrorMessageMatches(".*Failed to create Credentials from key.*");

        patchConfig(List.of(
                configEntry("bigquery.project-id", "fake"),
                configEntry("bigquery.location", "asia-east1"),
                configEntry("bigquery.credentials-key", getenv("TEST_BIG_QUERY_CREDENTIALS_BASE64_JSON")),
                configEntry("accio.datasource.type", "bigquery")));

        assertThatThrownBy(() -> {
            try (Connection connection = createConnection()) {
                Statement statement = connection.createStatement();
                statement.execute("SELECT count(*) from Orders");
                ResultSet resultSet = statement.getResultSet();
                resultSet.next();
            }
        }).hasMessageFindingMatch(".*Access Denied: Project fake.*");
    }
}