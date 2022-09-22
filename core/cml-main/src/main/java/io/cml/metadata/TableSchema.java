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
package io.cml.metadata;

import io.cml.spi.metadata.CatalogName;
import io.cml.spi.metadata.SchemaTableName;

import java.util.List;

import static com.google.common.collect.MoreCollectors.toOptional;
import static java.util.Objects.requireNonNull;

public final class TableSchema
{
    private final CatalogName catalogName;
    private final ConnectorTableSchema tableSchema;

    public TableSchema(CatalogName catalogName, ConnectorTableSchema tableSchema)
    {
        requireNonNull(catalogName, "catalog is null");
        requireNonNull(tableSchema, "metadata is null");

        this.catalogName = catalogName;
        this.tableSchema = tableSchema;
    }

    public CatalogName getCatalogName()
    {
        return catalogName;
    }

    public ConnectorTableSchema getTableSchema()
    {
        return tableSchema;
    }

    public SchemaTableName getTable()
    {
        return tableSchema.getTable();
    }

    public List<ColumnSchema> getColumns()
    {
        return tableSchema.getColumns();
    }

    public ColumnSchema getColumn(String name)
    {
        return tableSchema.getColumns().stream()
                .filter(columnMetadata -> columnMetadata.getName().equals(name))
                .collect(toOptional())
                .orElseThrow(() -> new IllegalArgumentException("Invalid column name: " + name));
    }
}
