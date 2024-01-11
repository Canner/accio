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

package io.accio.base.sqlrewrite.analyzer;

import com.google.common.collect.ImmutableList;
import io.trino.sql.tree.DefaultTraversalVisitor;
import io.trino.sql.tree.DereferenceExpression;
import io.trino.sql.tree.Expression;
import io.trino.sql.tree.Identifier;
import io.trino.sql.tree.QualifiedName;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.trino.sql.tree.DereferenceExpression.getQualifiedName;
import static java.util.Objects.requireNonNull;

public class ExpressionAnalyzer
{
    private ExpressionAnalyzer() {}

    public static ExpressionAnalysis analyze(Scope scope, Expression expression)
    {
        ExpressionVisitor visitor = new ExpressionVisitor(scope);
        visitor.process(expression);

        return new ExpressionAnalysis(visitor.getCollectedFields());
    }

    private static class ExpressionVisitor
            extends DefaultTraversalVisitor<Void>
    {
        private final Scope scope;
        private final List<Field> collectedFields = new ArrayList<>();

        public ExpressionVisitor(Scope scope)
        {
            this.scope = requireNonNull(scope);
        }

        @Override
        protected Void visitDereferenceExpression(DereferenceExpression node, Void context)
        {
            QualifiedName qualifiedName = getQualifiedName(node);
            if (qualifiedName != null) {
                collectedFields.addAll(scope.getRelationType().getFields().stream()
                        .filter(field -> field.canResolve(qualifiedName))
                        .collect(toImmutableList()));
            }
            return null;
        }

        @Override
        protected Void visitIdentifier(Identifier node, Void context)
        {
            QualifiedName qualifiedName = QualifiedName.of(ImmutableList.of(node));
            collectedFields.addAll(scope.getRelationType().getFields().stream()
                    .filter(field -> field.canResolve(qualifiedName))
                    .collect(toImmutableList()));
            return null;
        }

        public List<Field> getCollectedFields()
        {
            return collectedFields;
        }
    }
}