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

package io.wren.validation;

import io.wren.base.WrenMDL;
import io.wren.base.client.AutoCloseableIterator;
import io.wren.base.client.Client;
import io.wren.base.dto.Column;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static io.wren.validation.ValidationResult.error;
import static io.wren.validation.ValidationResult.fail;
import static io.wren.validation.ValidationResult.formatRuleWithIdentifier;
import static io.wren.validation.ValidationResult.pass;
import static java.lang.String.format;

public class NotNullValidation
        extends ValidationRule
{
    public static final NotNullValidation NOT_NULL = new NotNullValidation();
    private static final String RULE_NAME = "not_null";

    @Override
    public List<CompletableFuture<ValidationResult>> validate(Client client, WrenMDL wrenMDL)
    {
        return wrenMDL.listModels().stream()
                .flatMap(model ->
                        model.getColumns().stream()
                                .filter(Column::isNotNull)
                                .map(column -> validateColumn(client, model.getRefSql(), model.getName(), column.getName())))
                .collect(Collectors.toList());
    }

    public CompletableFuture<ValidationResult> validateColumn(Client client, String refSql, String modelName, String columName)
    {
        return CompletableFuture.supplyAsync(() -> {
            long start = System.currentTimeMillis();
            try (AutoCloseableIterator<Object[]> result = client.query(buildNotNullCheck(refSql, columName))) {
                long elapsed = System.currentTimeMillis() - start;
                if (result.hasNext()) {
                    Object[] row = result.next();
                    if ((boolean) row[0]) {
                        return pass(formatRuleWithIdentifier(RULE_NAME, modelName, columName), Duration.of(elapsed, ChronoUnit.MILLIS));
                    }
                    return fail(formatRuleWithIdentifier(RULE_NAME, modelName, columName), Duration.of(elapsed, ChronoUnit.MILLIS), "Got null value in " + columName);
                }
                return error(formatRuleWithIdentifier(RULE_NAME, modelName, columName), Duration.of(elapsed, ChronoUnit.MILLIS), "Query executed failed");
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String buildNotNullCheck(String refSql, String columnName)
    {
        return format("WITH source AS (%s) SELECT count(*) = 0 FROM source WHERE %s IS NULL", refSql, columnName);
    }
}
