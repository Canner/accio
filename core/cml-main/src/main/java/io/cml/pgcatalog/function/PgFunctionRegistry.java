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

package io.cml.pgcatalog.function;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import io.cml.spi.CmlException;

import javax.annotation.concurrent.ThreadSafe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.cml.pgcatalog.function.PgFunctionRegistry.FunctionKey.functionKey;
import static io.cml.pgcatalog.function.PgFunctions.ARRAY_IN;
import static io.cml.pgcatalog.function.PgFunctions.ARRAY_OUT;
import static io.cml.pgcatalog.function.PgFunctions.ARRAY_RECV;
import static io.cml.pgcatalog.function.PgFunctions.ARRAY_UPPER;
import static io.cml.pgcatalog.function.PgFunctions.CURRENT_DATABASE;
import static io.cml.pgcatalog.function.PgFunctions.CURRENT_SCHEMAS;
import static io.cml.pgcatalog.function.PgFunctions.PG_GET_EXPR;
import static io.cml.pgcatalog.function.PgFunctions.PG_GET_EXPR_PRETTY;
import static io.cml.pgcatalog.function.PgFunctions.PG_RELATION_SIZE__INT_VARCHAR___BIGINT;
import static io.cml.pgcatalog.function.PgFunctions.PG_RELATION_SIZE__INT___BIGINT;
import static io.cml.spi.metadata.StandardErrorCode.NOT_FOUND;
import static java.lang.String.format;

@ThreadSafe
public final class PgFunctionRegistry
{
    private final List<PgFunction> pgFunctions;
    private final Map<FunctionKey, PgFunction> simpleNameToFunction = new HashMap<>();

    public PgFunctionRegistry()
    {
        pgFunctions = ImmutableList.<PgFunction>builder()
                .add(CURRENT_DATABASE)
                .add(CURRENT_SCHEMAS)
                .add(PG_RELATION_SIZE__INT___BIGINT)
                .add(PG_RELATION_SIZE__INT_VARCHAR___BIGINT)
                .add(ARRAY_IN)
                .add(ARRAY_OUT)
                .add(ARRAY_RECV)
                .add(ARRAY_UPPER)
                .add(PG_GET_EXPR)
                .add(PG_GET_EXPR_PRETTY)
                .build();

        // TODO: handle function name overloading
        //  https://github.com/Canner/canner-metric-layer/issues/73
        // use HashMap to handle multiple same key entries
        pgFunctions.forEach(pgFunction -> simpleNameToFunction.put(functionKey(pgFunction.getName(), pgFunction.getArguments().map(List::size).orElse(0)), pgFunction));
    }

    public List<PgFunction> getPgFunctions()
    {
        return pgFunctions;
    }

    public PgFunction getPgFunction(String name, int numArgument)
    {
        return Optional.ofNullable(simpleNameToFunction.get(functionKey(name, numArgument)))
                .orElseThrow(() -> new CmlException(NOT_FOUND, format("%s is undefined", name)));
    }

    /**
     * TODO: analyze the type of argument expression
     *  https://github.com/Canner/canner-metric-layer/issues/92
     * <p>
     * We only support function overloading with different number of argument now. Because
     * the work of analyze the type of argument is too huge to implement, FunctionKey only
     * recognizes each function by its name and number of argument.
     */
    static class FunctionKey
    {
        public static FunctionKey functionKey(String name, int numArgument)
        {
            return new FunctionKey(name, numArgument);
        }

        private final String name;
        private final int numArgument;

        private FunctionKey(String name, int numArgument)
        {
            this.name = name;
            this.numArgument = numArgument;
        }

        public String getName()
        {
            return name;
        }

        @Override
        public int hashCode()
        {
            return Objects.hashCode(name, numArgument);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            FunctionKey that = (FunctionKey) o;
            return numArgument == that.numArgument && Objects.equal(name, that.name);
        }
    }
}
