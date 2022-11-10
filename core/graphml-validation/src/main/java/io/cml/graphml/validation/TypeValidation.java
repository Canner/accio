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

package io.cml.graphml.validation;

import io.cml.graphml.GraphML;
import io.cml.graphml.connector.Client;
import io.cml.graphml.connector.ColumnDescription;
import io.cml.graphml.dto.Column;
import io.cml.graphml.dto.EnumDefinition;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.cml.graphml.validation.ValidationResult.error;
import static io.cml.graphml.validation.ValidationResult.fail;
import static io.cml.graphml.validation.ValidationResult.formatRuleWithIdentifier;
import static io.cml.graphml.validation.ValidationResult.pass;
import static java.lang.String.format;
import static java.util.stream.Collectors.toUnmodifiableList;

public class TypeValidation
        extends ValidationRule
{
    public static final TypeValidation TYPE_VALIDATION = new TypeValidation();
    private static final String RULE_NAME = "type";

    @Override
    public List<CompletableFuture<ValidationResult>> validate(Client client, GraphML graphML)
    {
        return graphML.listModels().stream()
                .flatMap(model ->
                        model.getColumns().stream()
                                .filter(column ->
                                        graphML.listEnums().stream().map(EnumDefinition::getName).noneMatch(name -> name.equals(column.getType())))
                                .map(column -> validateColumn(client, model.getRefSql(), model.getName(), column)))
                .collect(toUnmodifiableList());
    }

    private CompletableFuture<ValidationResult> validateColumn(Client client, String refSql, String modelName, Column column)
    {
        return CompletableFuture.supplyAsync(() -> {
            long start = System.currentTimeMillis();
            Iterator<ColumnDescription> result = client.describe(buildSql(refSql, column.getName()));
            long elapsed = System.currentTimeMillis() - start;
            if (result.hasNext()) {
                ColumnDescription row = result.next();
                if (column.getType().equals(row.getType())) {
                    return pass(formatRuleWithIdentifier(RULE_NAME, modelName, column.getName()), Duration.of(elapsed, ChronoUnit.MILLIS));
                }
                return fail(formatRuleWithIdentifier(RULE_NAME, modelName, column.getName()), Duration.of(elapsed, ChronoUnit.MILLIS), "Got incompatible type in " + column.getName());
            }
            return error(formatRuleWithIdentifier(RULE_NAME, modelName, column.getName()), Duration.of(elapsed, ChronoUnit.MILLIS), "Query executed failed");
        });
    }

    private String buildSql(String refSql, String columnName)
    {
        return format("WITH source AS (%s) SELECT %S FROM source", refSql, columnName);
    }
}
