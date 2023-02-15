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

package io.graphmdl.main.calcite;

import io.graphmdl.base.metadata.ColumnMetadata;
import io.graphmdl.base.metadata.TableMetadata;
import io.graphmdl.base.type.PGType;
import io.graphmdl.main.metadata.Metadata;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.calcite.sql.SqlDialect;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static io.graphmdl.base.type.BigIntType.BIGINT;
import static io.graphmdl.base.type.BooleanType.BOOLEAN;
import static io.graphmdl.base.type.DateType.DATE;
import static io.graphmdl.base.type.DoubleType.DOUBLE;
import static io.graphmdl.base.type.IntegerType.INTEGER;
import static io.graphmdl.base.type.VarcharType.VARCHAR;
import static io.graphmdl.main.calcite.BigQueryGraphMDLSqlDialect.DEFAULT_CONTEXT;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static org.apache.calcite.jdbc.CalciteSchema.createRootSchema;

public final class GraphMDLSchemaUtil
{
    private GraphMDLSchemaUtil() {}

    public enum Dialect
    {
        BIGQUERY(new BigQueryGraphMDLSqlDialect(DEFAULT_CONTEXT));

        private final SqlDialect sqlDialect;

        Dialect(SqlDialect sqlDialect)
        {
            this.sqlDialect = sqlDialect;
        }

        public SqlDialect getSqlDialect()
        {
            return sqlDialect;
        }
    }

    public static SchemaPlus schemaPlus(List<TableMetadata> tableMetadatas, Metadata metadata)
    {
        SchemaPlus rootSchema = createRootSchema(true, true, "").plus();
        tableMetadatas.stream()
                .collect(groupingBy(tableMetadata -> metadata.getDefaultCatalog(),
                        groupingBy(tableMetadata -> tableMetadata.getTable().getSchemaName(),
                                mapping(tableMetadata -> Map.entry(tableMetadata.getTable().getTableName(), toGraphMDLTable(tableMetadata, metadata)),
                                        toImmutableMap(Map.Entry::getKey, Map.Entry::getValue)))))
                .forEach((catalogName, schemaTableMap) -> {
                    SchemaPlus secondSchema = rootSchema.add(catalogName, new AbstractSchema());
                    schemaTableMap.forEach((schemaName, graphMDLTableMap) -> secondSchema.add(schemaName, new GraphMDLSchema(graphMDLTableMap)));
                });
        return rootSchema;
    }

    private static GraphMDLTable toGraphMDLTable(TableMetadata tableMetadata, Metadata metadata)
    {
        JavaTypeFactoryImpl typeFactory = new CustomCharsetJavaTypeFactoryImpl(UTF_8, metadata.getRelDataTypeSystem());
        RelDataTypeFactory.Builder builder = new RelDataTypeFactory.Builder(typeFactory);
        for (ColumnMetadata columnMetadata : tableMetadata.getColumns()) {
            builder.add(columnMetadata.getName(), toRelDataType(typeFactory, columnMetadata.getType()));
        }

        return new GraphMDLTable(tableMetadata.getTable().getTableName(), builder.build());
    }

    // TODO: handle nested types
    private static RelDataType toRelDataType(JavaTypeFactory typeFactory, PGType<?> pgType)
    {
        if (pgType.equals(BOOLEAN)) {
            return typeFactory.createJavaType(Boolean.class);
        }
        if (pgType.equals(INTEGER)) {
            return typeFactory.createJavaType(Integer.class);
        }
        if (pgType.equals(BIGINT)) {
            return typeFactory.createJavaType(Long.class);
        }
        if (pgType.equals(VARCHAR)) {
            return typeFactory.createJavaType(String.class);
        }
        if (pgType.equals(DOUBLE)) {
            return typeFactory.createJavaType(Double.class);
        }
        if (pgType.equals(DATE)) {
            return typeFactory.createJavaType(Date.class);
        }
        throw new UnsupportedOperationException(pgType.typName() + " not supported yet");
    }
}
