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

package io.cml.graphml.analyzer;

import io.trino.sql.tree.DereferenceExpression;
import io.trino.sql.tree.NodeRef;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public class ExpressionAnalysis
{
    private final Map<NodeRef<DereferenceExpression>, DereferenceExpression> relationshipFieldRewrites;

    public ExpressionAnalysis(Map<NodeRef<DereferenceExpression>, DereferenceExpression> relationshipFields)
    {
        this.relationshipFieldRewrites = requireNonNull(relationshipFields, "relationshipFields is null");
    }

    public Map<NodeRef<DereferenceExpression>, DereferenceExpression> getRelationshipFieldRewrites()
    {
        return relationshipFieldRewrites;
    }
}