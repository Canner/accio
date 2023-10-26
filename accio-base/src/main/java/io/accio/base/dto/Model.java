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

package io.accio.base.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.airlift.units.Duration;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static io.accio.base.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

public class Model
        implements PreAggregationInfo
{
    private final String name;
    private final String refSql;
    private final String baseModel;
    private final List<Column> columns;
    private final String primaryKey;
    private final boolean preAggregated;
    private final Duration refreshTime;
    private final String description;

    public static Model model(String name, String refSql, List<Column> columns)
    {
        return model(name, refSql, columns, null);
    }

    public static Model model(String name, String refSql, List<Column> columns, boolean preAggregated)
    {
        return new Model(name, refSql, null, columns, null, preAggregated, null, null);
    }

    public static Model model(String name, String refSql, List<Column> columns, String primaryKey)
    {
        return model(name, refSql, columns, primaryKey, null);
    }

    public static Model model(String name, String refSql, List<Column> columns, String primaryKey, String description)
    {
        return new Model(name, refSql, null, columns, primaryKey, false, null, description);
    }

    public static Model onModel(String name, String baseModel, List<Column> columns, String primaryKey)
    {
        return new Model(name, null, baseModel, columns, primaryKey, false, null, null);
    }

    @JsonCreator
    public Model(
            @JsonProperty("name") String name,
            @JsonProperty("refSql") String refSql,
            @JsonProperty("baseModel") String baseModel,
            @JsonProperty("columns") List<Column> columns,
            @JsonProperty("primaryKey") String primaryKey,
            @JsonProperty("preAggregated") boolean preAggregated,
            @JsonProperty("refreshTime") Duration refreshTime,
            @JsonProperty("description") String description)
    {
        this.name = requireNonNull(name, "name is null");
        checkArgument(Stream.of(refSql, baseModel).filter(Objects::nonNull).count() == 1,
                "either none or more than one of (refSql, baseModel) are set");
        this.refSql = refSql;
        this.baseModel = baseModel;
        this.columns = columns == null ? List.of() : columns;
        this.primaryKey = primaryKey;
        this.preAggregated = preAggregated;
        this.refreshTime = refreshTime == null ? defaultRefreshTime : refreshTime;
        this.description = description;
    }

    @JsonProperty
    public String getName()
    {
        return name;
    }

    @JsonProperty
    public String getRefSql()
    {
        return refSql;
    }

    @JsonProperty
    public String getBaseModel()
    {
        return baseModel;
    }

    @JsonProperty
    public List<Column> getColumns()
    {
        return columns;
    }

    @JsonProperty
    public String getPrimaryKey()
    {
        return primaryKey;
    }

    @Override
    @JsonProperty
    public boolean isPreAggregated()
    {
        return preAggregated;
    }

    @Override
    @JsonProperty
    public Duration getRefreshTime()
    {
        return refreshTime;
    }

    @JsonProperty
    public String getDescription()
    {
        return description;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Model that = (Model) obj;
        return preAggregated == that.preAggregated
                && Objects.equals(name, that.name)
                && Objects.equals(refSql, that.refSql)
                && Objects.equals(baseModel, that.baseModel)
                && Objects.equals(columns, that.columns)
                && Objects.equals(primaryKey, that.primaryKey)
                && Objects.equals(refreshTime, that.refreshTime)
                && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, refSql, baseModel, columns, primaryKey, description);
    }

    @Override
    public String toString()
    {
        return "Model{" +
                "name='" + name + '\'' +
                ", refSql='" + refSql + '\'' +
                ", baseModel='" + baseModel + '\'' +
                ", columns=" + columns +
                ", primaryKey='" + primaryKey + '\'' +
                ", preAggregated=" + preAggregated +
                ", refreshTime='" + refreshTime + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
