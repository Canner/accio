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

package io.accio.main.sql.bigquery;

import com.google.common.collect.ImmutableList;
import io.accio.base.AccioMDL;
import io.accio.base.SessionContext;
import io.accio.base.sqlrewrite.BaseRewriter;
import io.accio.base.sqlrewrite.analyzer.Analysis;
import io.accio.base.sqlrewrite.analyzer.StatementAnalyzer;
import io.accio.base.type.PGType;
import io.accio.main.metadata.Metadata;
import io.accio.main.sql.SqlRewrite;
import io.accio.main.sql.bigquery.analyzer.BigQueryTypeCoercion;
import io.trino.sql.tree.ArithmeticBinaryExpression;
import io.trino.sql.tree.Expression;
import io.trino.sql.tree.Extract;
import io.trino.sql.tree.FunctionCall;
import io.trino.sql.tree.Identifier;
import io.trino.sql.tree.LongLiteral;
import io.trino.sql.tree.Node;
import io.trino.sql.tree.QualifiedName;
import io.trino.sql.tree.Statement;
import io.trino.sql.tree.StringLiteral;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.trino.sql.tree.ArithmeticBinaryExpression.Operator.SUBTRACT;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class RewriteToBigQueryFunction
        implements SqlRewrite
{
    public static final Extract.Field DAYOFYEAR = new Extract.Field("DAYOFYEAR");
    public static final Extract.Field DAYOFWEEK = new Extract.Field("DAYOFWEEK");
    public static final LongLiteral ONE = new LongLiteral("1");

    private final AccioMDL mdl;

    public RewriteToBigQueryFunction(AccioMDL mdl)
    {
        this.mdl = requireNonNull(mdl, "mdl is null");
    }

    public Node rewrite(Node node, Metadata metadata)
    {
        Analysis analysis = new Analysis((Statement) node);
        StatementAnalyzer.analyze(analysis, (Statement) node, SessionContext.builder()
                .setCatalog(mdl.getCatalog())
                .setSchema(mdl.getSchema())
                .build(), mdl, new BigQueryTypeCoercion(mdl));
        return new RewriteToBigQueryFunctionRewriter(metadata, analysis).process(node);
    }

    private static class RewriteToBigQueryFunctionRewriter
            extends BaseRewriter<Void>
    {
        private final Metadata metadata;
        private final Analysis analysis;

        private RewriteToBigQueryFunctionRewriter(Metadata metadata, Analysis analysis)
        {
            this.metadata = requireNonNull(metadata, "metadata is null");
            this.analysis = requireNonNull(analysis, "analysis is null");
        }

        @Override
        protected Node visitExtract(Extract node, Void context)
        {
            switch (node.getField().getName()) {
                case "DOY":
                    if (node.getLocation().isPresent()) {
                        return new Extract(node.getLocation().get(), visitAndCast(node.getExpression(), context), DAYOFYEAR);
                    }
                    return new Extract(visitAndCast(node.getExpression(), context), DAYOFYEAR);
                case "DOW":
                    // PostgreSQL returns the day of the week as an integer between 0 and 6, while BigQuery returns an integer between 1 and 7.
                    // We need to subtract 1 from the result to get the same result as PostgreSQL.
                    Extract dowExtract;
                    if (node.getLocation().isPresent()) {
                        dowExtract = new Extract(node.getLocation().get(), visitAndCast(node.getExpression(), context), DAYOFWEEK);
                    }
                    else {
                        dowExtract = new Extract(visitAndCast(node.getExpression(), context), DAYOFWEEK);
                    }
                    if (node.getLocation().isPresent()) {
                        return new ArithmeticBinaryExpression(
                                node.getLocation().get(),
                                SUBTRACT,
                                dowExtract,
                                ONE);
                    }
                    return new ArithmeticBinaryExpression(
                            SUBTRACT,
                            dowExtract,
                            ONE);
                default:
                    return super.visitExtract(node, context);
            }
        }

        @Override
        protected Node visitFunctionCall(FunctionCall node, Void context)
        {
            List<PGType<?>> pgTypes = node.getArguments().stream().map(arg -> analysis.getExpressionTypeMap().get(arg)).collect(toList());
            QualifiedName functionName = metadata.resolveFunction(node.getName().toString(), pgTypes);
            List<Expression> arguments = node.getArguments().stream()
                    .map(argument -> visitAndCast(argument, context))
                    .collect(toImmutableList());

            if (functionName.toString().equalsIgnoreCase("DATE_TRUNC")) {
                checkArgument(arguments.size() == 2, "DATE_TRUNC should have 2 arguments");
                checkArgument(arguments.get(0) instanceof StringLiteral, "Unable to resolve first argument of DATE_TRUNC");
                // bigquery DATE_TRUNC(date_expression, date_part) date_part should be an identifier while in pg it's a string literal
                arguments = ImmutableList.of(arguments.get(1), new Identifier(((StringLiteral) arguments.get(0)).getValue()));
            }
            else if (functionName.toString().equalsIgnoreCase("bool_or")) {
                functionName = QualifiedName.of("logical_or");
            }
            else if (functionName.toString().equalsIgnoreCase("every")) {
                functionName = QualifiedName.of("logical_and");
            }

            return FunctionCall.builder(node)
                    .name(functionName)
                    .window(node.getWindow().map(window -> visitAndCast(window, context)))
                    .filter(node.getFilter().map(filter -> visitAndCast(filter, context)))
                    .orderBy(node.getOrderBy().map(orderBy -> visitAndCast(orderBy, context)))
                    .processingMode(node.getProcessingMode().map(processingMode -> visitAndCast(processingMode, context)))
                    .arguments(arguments)
                    .build();
        }
    }
}
