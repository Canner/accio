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

package io.wren.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.wren.base.type.AnyType;
import io.wren.base.type.PGType;
import io.wren.base.type.PGTypes;

public final class Column
{
    private final String name;
    private final PGType<?> type;

    @JsonCreator
    public Column(
            @JsonProperty("name") String name,
            @JsonProperty("type") String type)
    {
        this.name = name;
        this.type = PGTypes.nameToPgType(type).orElse(AnyType.ANY);
    }

    public Column(String name, PGType<?> type)
    {
        this.name = name;
        this.type = type;
    }

    @JsonProperty
    public String getName()
    {
        return name;
    }

    public PGType<?> getType()
    {
        return type;
    }

    @JsonProperty("type")
    public String getTypeName()
    {
        return type.typName();
    }
}
