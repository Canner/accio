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

package io.graphmdl.base.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class Manifest
{
    private final String catalog;
    private final String schema;
    private final List<Model> models;
    private final List<Relationship> relationships;
    private final List<EnumDefinition> enumDefinitions;
    private final List<Metric> metrics;

    public static Builder builder()
    {
        return new Builder();
    }

    @JsonCreator
    public Manifest(
            @JsonProperty("catalog") String catalog,
            @JsonProperty("schema") String schema,
            @JsonProperty("models") List<Model> models,
            @JsonProperty("relationships") List<Relationship> relationships,
            @JsonProperty("enums") List<EnumDefinition> enumDefinitions,
            @JsonProperty("metrics") List<Metric> metrics)
    {
        this.catalog = requireNonNull(catalog, "catalog is null");
        this.schema = requireNonNull(schema, "schema is null");
        this.models = models == null ? List.of() : models;
        this.relationships = relationships == null ? List.of() : relationships;
        this.enumDefinitions = enumDefinitions == null ? List.of() : enumDefinitions;
        this.metrics = metrics == null ? List.of() : metrics;
    }

    public String getCatalog()
    {
        return catalog;
    }

    public String getSchema()
    {
        return schema;
    }

    public List<Model> getModels()
    {
        return models;
    }

    public List<Relationship> getRelationships()
    {
        return relationships;
    }

    public List<EnumDefinition> getEnumFields()
    {
        return enumDefinitions;
    }

    public List<Metric> getMetrics()
    {
        return metrics;
    }

    public static class Builder
    {
        private String catalog;
        private String schema;
        private List<Model> models;
        private List<Relationship> relationships;
        private List<EnumDefinition> enumDefinitions;
        private List<Metric> metrics;

        private Builder() {}

        public Builder setCatalog(String catalog)
        {
            this.catalog = catalog;
            return this;
        }

        public Builder setSchema(String schema)
        {
            this.schema = schema;
            return this;
        }

        public Builder setModels(List<Model> models)
        {
            this.models = models;
            return this;
        }

        public Builder setRelationships(List<Relationship> relationships)
        {
            this.relationships = relationships;
            return this;
        }

        public Builder setEnumDefinitions(List<EnumDefinition> enumDefinitions)
        {
            this.enumDefinitions = enumDefinitions;
            return this;
        }

        public Builder setMetrics(List<Metric> metrics)
        {
            this.metrics = metrics;
            return this;
        }

        public Manifest build()
        {
            return new Manifest(catalog, schema, models, relationships, enumDefinitions, metrics);
        }
    }
}