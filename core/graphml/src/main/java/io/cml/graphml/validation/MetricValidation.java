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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.cml.graphml.validation.NotNullValidation.NOT_NULL;
import static java.util.stream.Collectors.toList;

public final class MetricValidation
{
    private MetricValidation() {}

    public static List<ValidationResult> validate(Client client, GraphML graphML)
    {
        Validator validator = new Validator(client, graphML)
                .register(NOT_NULL);
        return validator.validate();
    }

    static class Validator
    {
        private final Client dbClient;
        private final GraphML graphML;
        private final List<ValidationRule> tasks = new ArrayList<>();

        public Validator(Client client, GraphML graphML)
        {
            this.dbClient = client;
            this.graphML = graphML;
        }

        public Validator register(ValidationRule task)
        {
            tasks.add(task);
            return this;
        }

        public List<ValidationResult> validate()
        {
            return tasks.stream().flatMap(task -> task.validate(dbClient, graphML).stream())
                    .map(future -> {
                        try {
                            return future.get(5, TimeUnit.MINUTES);
                        }
                        catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(toList());
        }
    }
}