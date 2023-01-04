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

package io.cml.graphml;

import io.cml.graphml.base.GraphML;
import io.trino.sql.SqlFormatter;
import io.trino.sql.parser.ParsingOptions;
import io.trino.sql.tree.Statement;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static io.cml.graphml.MetricSqlRewrite.METRIC_SQL_REWRITE;
import static io.cml.graphml.ModelSqlRewrite.MODEL_SQL_REWRITE;
import static io.cml.graphml.Utils.SQL_PARSER;
import static io.cml.graphml.base.GraphMLTypes.INTEGER;
import static io.cml.graphml.base.GraphMLTypes.VARCHAR;
import static io.cml.graphml.base.dto.Column.column;
import static io.cml.graphml.base.dto.Column.measure;
import static io.cml.graphml.base.dto.Manifest.manifest;
import static io.cml.graphml.base.dto.Metric.metric;
import static io.cml.graphml.base.dto.Model.model;
import static io.trino.sql.parser.ParsingOptions.DecimalLiteralTreatment.AS_DECIMAL;
import static org.assertj.core.api.Assertions.assertThat;

public class TestMetricSqlRewrite
{
    private final GraphML graphML;

    public TestMetricSqlRewrite()
    {
        graphML = GraphML.fromManifest(manifest(
                List.of(model("Album",
                        "select * from (values (1, 'Gusare', 'ZUTOMAYO', 2560), " +
                                "(2, 'HisoHiso Banashi', 'ZUTOMAYO', 1500), " +
                                "(3, 'Dakara boku wa ongaku o yameta', 'Yorushika', 2553)) " +
                                "album(id, name, author, price)",
                        List.of(
                                column("id", INTEGER, null, true),
                                column("name", VARCHAR, null, true),
                                column("author", VARCHAR, null, true),
                                column("price", INTEGER, null, true)))),
                List.of(),
                List.of(),
                List.of(metric(
                        "Collection",
                        "Album",
                        List.of(column("author", VARCHAR, null, true)),
                        List.of(measure("price", INTEGER, null, true, "sum(Album.price)"))
                ))));
    }

    @DataProvider
    public Object[][] metricCases()
    {
        return new Object[][] {
                {"select author, price from Collection",
                        "WITH\n" +
                                "  Album AS (\n" +
                                "   SELECT\n" +
                                "     id\n" +
                                "   , name\n" +
                                "   , author\n" +
                                "   , price\n" +
                                "   FROM\n" +
                                "     (\n" +
                                "      SELECT *\n" +
                                "      FROM\n" +
                                "        (\n" +
                                " VALUES \n" +
                                "           ROW (1, 'Gusare', 'ZUTOMAYO', 2560)\n" +
                                "         , ROW (2, 'HisoHiso Banashi', 'ZUTOMAYO', 1500)\n" +
                                "         , ROW (3, 'Dakara boku wa ongaku o yameta', 'Yorushika', 2553)\n" +
                                "      )  album (id, name, author, price)\n" +
                                "   ) \n" +
                                ") \n" +
                                ", Collection AS (\n" +
                                "   SELECT\n" +
                                "     author\n" +
                                "   , sum(Album.price) price\n" +
                                "   FROM\n" +
                                "     Album\n" +
                                "   GROUP BY author\n" +
                                ") \n" +
                                "SELECT\n" +
                                "  author\n" +
                                ", price\n" +
                                "FROM\n" +
                                "  Collection"}};
    }

    @Test(dataProvider = "metricCases")
    public void testMetricSqlRewrite(String original, String expected)
    {
        Statement expectedState = SQL_PARSER.createStatement(expected, new ParsingOptions(AS_DECIMAL));
        assertThat(rewrite(original)).isEqualTo(SqlFormatter.formatSql(expectedState));
    }

    private String rewrite(String sql)
    {
        return GraphMLPlanner.rewrite(sql, graphML, List.of(MODEL_SQL_REWRITE, METRIC_SQL_REWRITE));
    }
}
