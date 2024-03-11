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

import static java.lang.String.format;

public class DuckdbCacheStorageConfig
        implements CacheStorageConfig
{
    @Override
    public String generateDuckdbParquetStatement(String path, String tableName)
    {
        return "BEGIN TRANSACTION;\n" +
                format("CREATE TABLE \"%s\" AS SELECT * FROM read_parquet('%s');", tableName, path) +
                "COMMIT;\n";
    }
}
