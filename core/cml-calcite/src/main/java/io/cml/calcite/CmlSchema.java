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

package io.cml.calcite;

import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;

import java.util.Map;

import static java.util.Objects.requireNonNull;

class CmlSchema
        extends AbstractSchema
{
    private final Map<String, Table> tableMap;

    public CmlSchema(Map<String, Table> tableMap)
    {
        this.tableMap = requireNonNull(tableMap, "tableMap is null");
    }

    @Override
    protected Map<String, Table> getTableMap()
    {
        return tableMap;
    }
}
