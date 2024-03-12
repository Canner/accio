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

package io.accio.testing.duckdb;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.google.inject.Key;
import io.accio.base.AccioMDL;
import io.accio.base.client.ForConnector;
import io.accio.base.client.duckdb.DuckdbClient;
import io.accio.cache.CacheInfoPair;
import io.accio.main.AccioManager;
import io.accio.testing.TestCache;
import io.accio.testing.TestingAccioServer;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.accio.base.AccioConfig.DataSourceType.DUCKDB;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

@Test(singleThreaded = true)
public class TestCacheWithDuckDB
        extends TestCache
{
    @Override
    protected void prepare()
    {
        try {
            initDuckDB(server());
            // Refresh MDL after data loaded
            String mdlJson = Resources.toString(requireNonNull(getClass().getClassLoader().getResource("duckdb/cache_mdl.json")).toURI().toURL(), UTF_8);
            getInstance(Key.get(AccioManager.class)).deployAndArchive(AccioMDL.fromJson(mdlJson).getManifest(), "v1");
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initDuckDB(TestingAccioServer accioServer)
            throws Exception
    {
        ClassLoader classLoader = getClass().getClassLoader();
        String initSQL = Resources.toString(requireNonNull(classLoader.getResource("duckdb/init.sql")).toURI().toURL(), UTF_8);
        initSQL = initSQL.replaceAll("basePath", requireNonNull(classLoader.getResource("duckdb/data")).getPath());
        accioServer.getInstance(Key.get(DuckdbClient.class, ForConnector.class)).executeDDL(initSQL);
    }

    @Override
    protected Optional<String> getAccioMDLPath()
    {
        return Optional.of(requireNonNull(getClass().getClassLoader().getResource("duckdb/mdl.json")).getPath());
    }

    @Override
    protected String getDefaultCatalog()
    {
        return "memory";
    }

    @Override
    protected String getDefaultSchema()
    {
        return "tpch";
    }

    @Override
    protected ImmutableMap.Builder<String, String> getProperties()
    {
        return ImmutableMap.<String, String>builder()
                .put("accio.datasource.type", DUCKDB.name());
    }

    @Override
    protected Optional<CacheInfoPair> getDefaultCacheInfoPair(String name)
    {
        return Optional.ofNullable(cachedTableMapping.get().getCacheInfoPair(getDefaultCatalog(), getDefaultSchema(), name));
    }
}