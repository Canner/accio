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

package io.cml.testing.bigquery;

import io.cml.spi.type.BigIntType;
import io.cml.spi.type.PGType;
import io.cml.testing.AbstractWireProtocolTest;
import io.cml.testing.TestingCmlServer;

public class TestWireProtocolWithBigquery
        extends AbstractWireProtocolTest
        implements BigQueryTesting
{
    @Override
    protected TestingCmlServer createCmlServer()
    {
        return createCmlServerWithBigQuery();
    }

    @Override
    protected PGType<?> correspondingIntegerType()
    {
        return BigIntType.BIGINT;
    }
}
