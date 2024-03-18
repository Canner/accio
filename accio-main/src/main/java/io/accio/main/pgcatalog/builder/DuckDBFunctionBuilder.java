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

package io.accio.main.pgcatalog.builder;

import io.accio.base.AccioException;
import io.accio.base.client.duckdb.DuckdbClient;
import io.accio.base.pgcatalog.function.PgFunction;
import io.airlift.log.Logger;

import static io.accio.base.metadata.StandardErrorCode.GENERIC_INTERNAL_ERROR;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class DuckDBFunctionBuilder
        implements PgFunctionBuilder, PgMetastoreFunctionBuilder
{
    private static final Logger LOG = Logger.get(DuckDBFunctionBuilder.class);
    private final DuckdbClient duckdbClient;

    public DuckDBFunctionBuilder(DuckdbClient duckdbClient)
    {
        this.duckdbClient = requireNonNull(duckdbClient, "duckdbClient is null");
    }

    @Override
    public String generateCreateFunction(PgFunction pgFunction)
    {
        switch (pgFunction.getLanguage()) {
            case SQL:
                return generateCreateSqlFunction(pgFunction);
        }
        throw new AccioException(GENERIC_INTERNAL_ERROR, "Unsupported language: " + pgFunction.getLanguage());
    }

    private String generateCreateSqlFunction(PgFunction pgFunction)
    {
        StringBuilder parameterBuilder = new StringBuilder();
        if (pgFunction.getArguments().isPresent()) {
            for (PgFunction.Argument argument : pgFunction.getArguments().get()) {
                parameterBuilder
                        .append(argument.getName())
                        .append(", ");
            }
            parameterBuilder.delete(parameterBuilder.length() - 2, parameterBuilder.length());
        }

        return format("CREATE OR REPLACE MACRO %s(%s) AS (%s);",
                pgFunction.getName(),
                parameterBuilder,
                pgFunction.getDefinition());
    }

    public void createPgFunction(PgFunction pgFunction)
    {
        String sql = generateCreateFunction(pgFunction);
        LOG.info("Creating or updating pg_catalog.%s: %s", pgFunction.getName(), sql);
        duckdbClient.executeDDL(sql);
        LOG.info("pg_catalog.%s has created or updated", pgFunction.getName());
    }
}
