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

package io.graphmdl.testing.bigquery;

import com.google.common.collect.ImmutableMap;
import io.graphmdl.testing.AbstractWireProtocolTest;
import io.graphmdl.testing.TestingGraphMDLServer;

import static java.lang.System.getenv;

public abstract class AbstractWireProtocolTestWithBigQuery
        extends AbstractWireProtocolTest
{
    @Override
    protected TestingGraphMDLServer createGraphMDLServer()
    {
        ImmutableMap.Builder<String, String> properties = ImmutableMap.<String, String>builder()
                .put("bigquery.project-id", getenv("TEST_BIG_QUERY_PROJECT_ID"))
                .put("bigquery.location", "asia-east1")
                .put("bigquery.credentials-key", getenv("TEST_BIG_QUERY_CREDENTIALS_BASE64_JSON"))
                .put("graphmdl.datasource.type", "bigquery");

        if (getGraphMDLPath().isPresent()) {
            properties.put("graphmdl.file", getGraphMDLPath().get());
        }

        return TestingGraphMDLServer.builder()
                .setRequiredConfigs(properties.build())
                .build();
    }

    @Override
    protected String getDefaultCatalog()
    {
        return "canner-cml";
    }

    @Override
    protected String getDefaultSchema()
    {
        return "tpch_tiny";
    }
}
