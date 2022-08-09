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
package io.cml.pgcatalog;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.cml.metadata.Metadata;
import io.cml.pgcatalog.builder.PgCatalogTableBuilder;
import io.cml.pgcatalog.builder.PgFunctionBuilder;
import io.cml.pgcatalog.function.PgFunction;
import io.cml.pgcatalog.function.PgFunctionRegistry;
import io.cml.pgcatalog.table.CharacterSets;
import io.cml.pgcatalog.table.KeyColumnUsage;
import io.cml.pgcatalog.table.PgAmTable;
import io.cml.pgcatalog.table.PgAttrdefTable;
import io.cml.pgcatalog.table.PgAttributeTable;
import io.cml.pgcatalog.table.PgCatalogTable;
import io.cml.pgcatalog.table.PgClassTable;
import io.cml.pgcatalog.table.PgConstraintTable;
import io.cml.pgcatalog.table.PgDatabaseTable;
import io.cml.pgcatalog.table.PgDescriptionTable;
import io.cml.pgcatalog.table.PgEnumTable;
import io.cml.pgcatalog.table.PgIndexTable;
import io.cml.pgcatalog.table.PgNamespaceTable;
import io.cml.pgcatalog.table.PgProcTable;
import io.cml.pgcatalog.table.PgRangeTable;
import io.cml.pgcatalog.table.PgRolesTable;
import io.cml.pgcatalog.table.PgSettingsTable;
import io.cml.pgcatalog.table.PgTablespaceTable;
import io.cml.pgcatalog.table.PgTypeTable;
import io.cml.pgcatalog.table.ReferentialConstraints;
import io.cml.pgcatalog.table.TableConstraints;
import io.cml.spi.metadata.TableMetadata;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.cml.pgcatalog.PgCatalogUtils.CML_TEMP_NAME;
import static io.cml.pgcatalog.PgCatalogUtils.PG_CATALOG_NAME;
import static java.util.Objects.requireNonNull;

public class PgCatalogManager
{
    private final Map<String, PgCatalogTable> tables;

    private final Metadata connector;

    private final PgFunctionRegistry pgFunctionRegistry;
    private final PgCatalogTableBuilder pgCatalogTableBuilder;
    private final PgFunctionBuilder pgFunctionBuilder;

    private final List<String> highPriorityTableName = ImmutableList.of(PgTypeTable.NAME);

    @Inject
    public PgCatalogManager(Metadata connector, PgCatalogTableBuilder pgCatalogTableBuilder, PgFunctionBuilder pgFunctionBuilder)
    {
        this.tables = initTables();
        this.connector = requireNonNull(connector, "connector is null");
        this.pgCatalogTableBuilder = requireNonNull(pgCatalogTableBuilder, "pgCatalogBuilder is null");
        this.pgFunctionBuilder = requireNonNull(pgFunctionBuilder, "pgFunctionBuilder is null");
        this.pgFunctionRegistry = new PgFunctionRegistry();
    }

    private Map<String, PgCatalogTable> initTables()
    {
        return ImmutableMap.<String, PgCatalogTable>builder()
                .put(PgAmTable.NAME, new PgAmTable())
                .put(PgAttrdefTable.NAME, new PgAttrdefTable())
                .put(PgAttributeTable.NAME, new PgAttributeTable())
                .put(PgClassTable.NAME, new PgClassTable())
                .put(PgConstraintTable.NAME, new PgConstraintTable())
                .put(PgDatabaseTable.NAME, new PgDatabaseTable())
                .put(PgDescriptionTable.NAME, new PgDescriptionTable())
                .put(PgEnumTable.NAME, new PgEnumTable())
                .put(PgIndexTable.NAME, new PgIndexTable())
                .put(PgNamespaceTable.NAME, new PgNamespaceTable())
                .put(PgProcTable.NAME, new PgProcTable())
                .put(PgRangeTable.NAME, new PgRangeTable())
                .put(PgRolesTable.NAME, new PgRolesTable())
                .put(PgSettingsTable.NAME, new PgSettingsTable())
                .put(PgTablespaceTable.NAME, new PgTablespaceTable())
                .put(PgTypeTable.NAME, new PgTypeTable())
                .put(CharacterSets.NAME, new CharacterSets())
                .put(ReferentialConstraints.NAME, new ReferentialConstraints())
                .put(KeyColumnUsage.NAME, new KeyColumnUsage())
                .put(TableConstraints.NAME, new TableConstraints())
                .build();
    }

    public void initPgCatalog()
    {
        createCatalogIfNotExist(PG_CATALOG_NAME);
        if (!isPgCatalogValid()) {
            initPgTables();
            initPgFunctions();
        }
    }

    public void initPgTables()
    {
        createCatalogIfNotExist(CML_TEMP_NAME);

        // Some table has dependency with the high priority table.
        // Create them first.
        for (String tableName : highPriorityTableName) {
            createPgCatalogTable(tables.get(tableName));
        }

        List<PgCatalogTable> lowPriorityTable = tables.values().stream()
                .filter(pgCatalogTable -> !highPriorityTableName.contains(pgCatalogTable.getName()))
                .collect(toImmutableList());

        for (PgCatalogTable pgCatalogTable : lowPriorityTable) {
            createPgCatalogTable(pgCatalogTable);
        }
    }

    public void initPgFunctions()
    {
        for (PgFunction pgFunction : pgFunctionRegistry.getPgFunctions()) {
            pgFunctionBuilder.createPgFunction(pgFunction);
        }
    }

    private void createCatalogIfNotExist(String name)
    {
        if (!connector.isSchemaExist(name)) {
            connector.createSchema(name);
        }
    }

    private boolean isPgCatalogValid()
    {
        List<TableMetadata> remoteTables = connector.listTables(PG_CATALOG_NAME);
        if (remoteTables.size() != tables.values().size()) {
            return false;
        }

        List<String> remoteFunctions = connector.listFunctionNames(PG_CATALOG_NAME);
        return pgFunctionRegistry.getPgFunctions().size() == remoteFunctions.size();
    }

    private void createPgCatalogTable(PgCatalogTable pgCatalogTable)
    {
        pgCatalogTableBuilder.createPgTable(pgCatalogTable);
    }
}