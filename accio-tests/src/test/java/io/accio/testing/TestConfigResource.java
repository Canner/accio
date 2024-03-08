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

package io.accio.testing;

import com.google.common.collect.ImmutableMap;
import io.accio.base.dto.Manifest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static io.accio.base.config.AccioConfig.ACCIO_DATASOURCE_TYPE;
import static io.accio.base.config.AccioConfig.ACCIO_DIRECTORY;
import static io.accio.base.config.BigQueryConfig.BIGQUERY_CRENDITALS_KEY;
import static io.accio.base.config.ConfigManager.ConfigEntry.configEntry;
import static io.accio.base.dto.Manifest.MANIFEST_JSON_CODEC;
import static io.accio.testing.AbstractTestFramework.withDefaultCatalogSchema;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class TestConfigResource
        extends RequireAccioServer
{
    private Path mdlDir;

    @Override
    protected TestingAccioServer createAccioServer()
    {
        Manifest manifest = withDefaultCatalogSchema()
                .build();

        try {
            mdlDir = Files.createTempDirectory("acciomdls");
            Path accioMDLFilePath = mdlDir.resolve("acciomdl.json");
            Files.write(accioMDLFilePath, MANIFEST_JSON_CODEC.toJsonBytes(manifest));
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        ImmutableMap.Builder<String, String> properties = ImmutableMap.<String, String>builder()
                .put(ACCIO_DIRECTORY, mdlDir.toAbsolutePath().toString())
                .put(ACCIO_DATASOURCE_TYPE, "duckdb");

        return TestingAccioServer.builder()
                .setRequiredConfigs(properties.build())
                .build();
    }

    @Test
    public void testGetConfigs()
    {
        assertThat(getConfigs().size()).isGreaterThan(2);
        assertThat(getConfig(ACCIO_DIRECTORY)).isEqualTo(configEntry(ACCIO_DIRECTORY, mdlDir.toAbsolutePath().toString()));
        assertThat(getConfig(ACCIO_DATASOURCE_TYPE)).isEqualTo(configEntry(ACCIO_DATASOURCE_TYPE, "DUCKDB"));

        assertThatThrownBy(() -> getConfig("notfound"))
                .hasMessageFindingMatch(".*404 Not Found.*");
        assertThatThrownBy(() -> getConfig(null))
                .hasMessageFindingMatch(".*404 Not Found.*");
    }

    @Test
    public void testuUpdateConfigs()
    {
        patchConfig(List.of(configEntry(ACCIO_DATASOURCE_TYPE, "BIGQUERY")));
        assertThat(getConfig(ACCIO_DATASOURCE_TYPE)).isEqualTo(configEntry(ACCIO_DATASOURCE_TYPE, "BIGQUERY"));
        assertThat(getConfig(ACCIO_DIRECTORY)).isEqualTo(configEntry(ACCIO_DIRECTORY, mdlDir.toAbsolutePath().toString()));
        assertThat(getConfig(BIGQUERY_CRENDITALS_KEY)).isEqualTo(configEntry(BIGQUERY_CRENDITALS_KEY, null));

        updateConfig(List.of(configEntry(ACCIO_DATASOURCE_TYPE, "BIGQUERY"), configEntry(BIGQUERY_CRENDITALS_KEY, "key")));
        assertThat(getConfig(ACCIO_DATASOURCE_TYPE)).isEqualTo(configEntry(ACCIO_DATASOURCE_TYPE, "BIGQUERY"));
        assertThat(getConfig(BIGQUERY_CRENDITALS_KEY)).isEqualTo(configEntry(BIGQUERY_CRENDITALS_KEY, "key"));

        updateConfig(List.of(configEntry(ACCIO_DATASOURCE_TYPE, "bigquery")));
        assertThat(getConfig(ACCIO_DATASOURCE_TYPE)).isEqualTo(configEntry(ACCIO_DATASOURCE_TYPE, "bigquery"));
        assertThat(getConfig(BIGQUERY_CRENDITALS_KEY)).isEqualTo(configEntry(BIGQUERY_CRENDITALS_KEY, null));
    }
}
