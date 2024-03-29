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

package io.wren.main.pgcatalog.table;

import com.google.common.collect.ImmutableMap;
import io.wren.base.metadata.TableMetadata;
import io.wren.base.type.BooleanType;

import java.util.Map;

import static io.wren.base.type.BooleanType.BOOLEAN;
import static io.wren.base.type.DoubleType.DOUBLE;
import static io.wren.base.type.IntegerType.INTEGER;
import static io.wren.base.type.PGArray.VARCHAR_ARRAY;
import static io.wren.base.type.VarcharType.VARCHAR;

/**
 * @see <a href="https://www.postgresql.org/docs/13/catalog-pg-class.html">PostgreSQL pg_class</a>
 */
public class PgClassTable
        extends PgCatalogTable
{
    public static final String NAME = "pg_class";

    @Override
    protected TableMetadata createMetadata()
    {
        return PgCatalogTableUtils.table(NAME)
                .column("oid", INTEGER)
                .column("relname", VARCHAR)
                .column("relnamespace", INTEGER)
                .column("reltype", INTEGER)
                .column("reloftype", INTEGER)
                .column("relowner", INTEGER)
                .column("relam", INTEGER)
                .column("relfilenode", INTEGER)
                .column("reltablespace", INTEGER)
                .column("relpages", INTEGER)
                .column("reltuples", DOUBLE)
                .column("relallvisible", INTEGER)
                .column("reltoastrelid", INTEGER)
                .column("relhasindex", BOOLEAN)
                .column("relisshared", BOOLEAN)
                .column("relpersistence", VARCHAR)
                .column("relkind", VARCHAR)
                .column("relnatts", INTEGER)
                .column("relchecks", INTEGER)
                .column("relhasrules", BOOLEAN)
                .column("relhastriggers", BOOLEAN)
                .column("relhassubclass", BOOLEAN)
                .column("relrowsecurity", BOOLEAN)
                .column("relforcerowsecurity", BOOLEAN)
                .column("relispopulated", BOOLEAN)
                .column("relreplident", VARCHAR)
                .column("relispartition", BOOLEAN)
                .column("relrewrite", INTEGER)
                .column("relfrozenxid", INTEGER)
                .column("relminmxid", INTEGER)
                .column("relacl", VARCHAR_ARRAY)
                .column("reloptions", VARCHAR_ARRAY)
                .column("relpartbound", VARCHAR_ARRAY)
                .column("relhasoids", BooleanType.BOOLEAN)
                .build();
    }

    @Override
    protected Map<String, String> createTableContent()
    {
        return ImmutableMap.<String, String>builder()
                .put("oid", "${hash}(${concat}(${schemaName},${tableName}))")
                .put("relname", "${tableName}")
                .put("relnamespace", "${hash}(${schemaName})")
                .put("reltype", "0")
                .put("reloftype", "0")
                .put("relowner", "0")
                .put("relam", "0")
                .put("relfilenode", "0")
                .put("reltablespace", "0")
                .put("relpages", "0")
                .put("reltuples", "0")
                .put("relallvisible", "0")
                .put("reltoastrelid", "0")
                .put("relhasindex", "false")
                .put("relisshared", "false")
                .put("relpersistence", "'p'")
                .put("relkind", "'r'")
                .put("relnatts", "0")
                .put("relchecks", "0")
                .put("relhasrules", "false")
                .put("relhastriggers", "false")
                .put("relhassubclass", "false")
                .put("relrowsecurity", "false")
                .put("relforcerowsecurity", "false")
                .put("relispopulated", "false")
                .put("relreplident", "'n'")
                .put("relispartition", "false")
                .put("relrewrite", "0")
                .put("relfrozenxid", "0")
                .put("relminmxid", "0")
                .put("relacl", "null")
                .put("reloptions", "null")
                .put("relpartbound", "null")
                .put("relhasoids", "false")
                .build();
    }
}
