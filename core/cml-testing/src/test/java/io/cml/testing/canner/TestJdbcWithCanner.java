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

package io.cml.testing.canner;

import io.cml.testing.AbstractJdbcTest;
import io.cml.testing.JdbcTesting;
import io.cml.testing.TestingCmlServer;
import org.testng.annotations.Test;

/**
 * This test is only for local testing.
 */
@Test(enabled = false)
public class TestJdbcWithCanner
        extends AbstractJdbcTest
        implements JdbcTesting, CannerTesting
{
    @Override
    protected TestingCmlServer createCmlServer()
    {
        return createCmlServerWithCanner();
    }
}
