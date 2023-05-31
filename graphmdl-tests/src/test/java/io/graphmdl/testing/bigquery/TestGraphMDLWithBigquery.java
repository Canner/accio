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

package io.graphmdl.testing.bigquery;

import io.graphmdl.testing.AbstractWireProtocolTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class TestGraphMDLWithBigquery
        extends AbstractWireProtocolTest
{
    @Override
    protected Optional<String> getGraphMDLPath()
    {
        return Optional.of(getClass().getClassLoader().getResource("tpch_mdl.json").getPath());
    }

    @DataProvider
    public Object[][] queryModel()
    {
        return new Object[][] {
                {"select * from Orders"},
                {"select * from Orders WHERE orderkey > 100"},
                {"select * from Orders a JOIN Customer b ON a.custkey = b.custkey"},
        };
    }

    @Test(dataProvider = "queryModel")
    public void testQueryModel(String sql)
    {
        assertThatNoException().isThrownBy(() -> {
            try (Connection connection = createConnection()) {
                PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet resultSet = stmt.executeQuery();
                resultSet.next();
            }
        });
    }

    @Test
    public void testQueryOnlyModelColumn()
            throws Exception
    {
        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select * from Orders limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getInt("orderkey"));
            assertThatNoException().isThrownBy(() -> resultSet.getInt("custkey"));
            assertThatNoException().isThrownBy(() -> resultSet.getString("orderstatus"));
            assertThatNoException().isThrownBy(() -> resultSet.getString("totalprice"));
            assertThatThrownBy(() -> resultSet.getString("o_orderkey"))
                    .hasMessageMatching(".*The column name o_orderkey was not found in this ResultSet.*");
            int count = 1;

            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }
    }

    @Test
    public void testQueryRelationship()
            throws Exception
    {
        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select orderkey, customer.name as name from Orders limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getInt("orderkey"));
            assertThatNoException().isThrownBy(() -> resultSet.getString("name"));
            int count = 1;

            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }

        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select c.custkey, array_length(orders) as agg from Customer c limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getInt("custkey"));
            assertThatNoException().isThrownBy(() -> resultSet.getString("agg"));
            int count = 1;

            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }

        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select custkey, array_length(orders) as agg from Customer limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getInt("custkey"));
            assertThatNoException().isThrownBy(() -> resultSet.getString("agg"));
            int count = 1;

            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }

        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select array_length(orders) as agg from Customer limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getString("agg"));
            int count = 1;

            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }

        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select orders[1].orderstatus as orderstatus from Customer limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getString("orderstatus"));
            int count = 1;

            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }

        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select customer from Orders limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getString("customer"));
            int count = 1;
            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }
    }

    @Test
    public void testQueryMetric()
            throws Exception
    {
        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select custkey, totalprice from Revenue limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getInt("custkey"));
            assertThatNoException().isThrownBy(() -> resultSet.getInt("totalprice"));
            int count = 1;

            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }
    }

    @Test
    void testQueryMetricRollup()
            throws Exception
    {
        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select custkey, totalprice from roll_up(Revenue, orderdate, YEAR) limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getInt("custkey"));
            assertThatNoException().isThrownBy(() -> resultSet.getInt("totalprice"));
            int count = 1;

            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }
    }

    @Test
    public void testTransform()
            throws Exception
    {
        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select transform(Customer.orders, orderItem -> orderItem.orderstatus) as orderstatuses from Customer limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getString("orderstatuses"));
            int count = 1;
            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }
    }

    @DataProvider
    public Object[][] any()
    {
        return new Object[][] {
                {"select any(Part.lineitem) as col from Part limit 100"},
                {"select any(Part.lineitem).linenumber as col from Part limit 100"},
                // TODO: enable this test
                // {"select any(Part.lineitem).orderkey + any(Part.lineitem).linenumber as col from Part limit 100"}
        };
    }

    @Test(dataProvider = "any")
    public void testAny(String sql)
    {
        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getString("col"));
            int count = 1;
            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testLambdaFunctionChain()
            throws Exception
    {
        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement(
                    "select transform(array_reverse(filter(Customer.orders, orderItem -> orderItem.orderstatus = 'O' or orderItem.orderstatus = 'F')), orderItem -> orderItem.totalprice)\n" +
                            "as col_1\n" +
                            "from Customer limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getString("col_1"));
            int count = 1;
            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }

        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement(
                    "select transform(filter(Customer.orders, orderItem -> orderItem.orderstatus = 'O' or orderItem.orderstatus = 'F'), orderItem -> orderItem.totalprice)\n" +
                            "as col_1\n" +
                            "from Customer limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getString("col_1"));
            int count = 1;
            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }

        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement(
                    "select any(filter(filter(Customer.orders, orderItem -> orderItem.orderstatus = 'O'), orderItem -> orderItem.orderstatus = 'F')).totalprice\n" +
                            "as col_1\n" +
                            "from Customer limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getString("col_1"));
            int count = 1;
            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }

        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement(
                    "select array_concat(\n" +
                            "filter(Customer.orders, orderItem -> orderItem.orderstatus = 'O'),\n" +
                            "filter(Customer.orders, orderItem -> orderItem.orderstatus = 'F'))\n" +
                            "as col_1\n" +
                            "from Customer limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getString("col_1"));
            int count = 1;
            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }

        // test failed stmt
        try (Connection connection = createConnection()) {
            assertThatThrownBy(() -> {
                PreparedStatement stmt = connection.prepareStatement(
                        "select filter(transform(Customer.orders, orderItem -> orderItem.orderstatus), orderItem -> orderItem.orderstatus = 'O' or orderItem.orderstatus = 'F')\n" +
                                "as col_1\n" +
                                "from Customer limit 100");
                stmt.executeQuery();
            }).hasMessageStartingWith("ERROR: Invalid statement");
        }

        // test failed stmt
        try (Connection connection = createConnection()) {
            assertThatThrownBy(() -> {
                PreparedStatement stmt = connection.prepareStatement(
                        "select transform(array_concat(\n" +
                                "filter(Customer.orders, orderItem -> orderItem.orderstatus = 'O'),\n" +
                                "filter(Customer.orders, orderItem -> orderItem.orderstatus = 'F'))," +
                                "orderItem -> orderItem.totalprice)\n" +
                                "as col_1\n" +
                                "from Customer limit 100");
                stmt.executeQuery();
            }).hasMessageStartingWith("ERROR: Currently the first argument of a lambda function cannot contain more than one lambda function.");
        }
    }

    @Test
    public void testGroupByRelationship()
            throws Exception
    {
        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select customer, count(*) as totalcount from Orders group by customer");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getInt("totalcount"));
            int count = 1;
            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(1000);
        }

        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select customer, count(*) as totalcount from Orders group by 1");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getInt("totalcount"));
            assertThatNoException().isThrownBy(() -> resultSet.getString("customer"));
            int count = 1;
            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(1000);
        }
    }

    @Test
    public void testAccessMultiRelationship()
            throws Exception
    {
        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select linenumber, orders.orderstatus from Lineitem limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getInt("linenumber"));
            assertThatNoException().isThrownBy(() -> resultSet.getString("orderstatus"));
            int count = 1;

            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }

        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select linenumber, orders.orderstatus, part.name from Lineitem limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getInt("linenumber"));
            assertThatNoException().isThrownBy(() -> resultSet.getString("orderstatus"));
            assertThatNoException().isThrownBy(() -> resultSet.getString("name"));
            int count = 1;

            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }

        // TODO: https://github.com/Canner/canner-metric-layer/issues/229
        // try (Connection connection = createConnection()) {
        //     PreparedStatement stmt = connection.prepareStatement("select linenumber, orders.customer.name from Lineitem limit 100");
        //     ResultSet resultSet = stmt.executeQuery();
        //     resultSet.next();
        //     assertThatNoException().isThrownBy(() -> resultSet.getInt("linenumber"));
        //     assertThatNoException().isThrownBy(() -> resultSet.getString("name"));
        //
        //     int count = 1;
        //
        //     while (resultSet.next()) {
        //         count++;
        //     }
        //     assertThat(count).isEqualTo(100);
        // }
    }

    @Test
    public void testEnum()
            throws Exception
    {
        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select Status.F as f1");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThat(resultSet.getString("f1")).isEqualTo("F");
        }

        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select count(*) as totalcount from Orders where orderstatus = Status.O");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThat(resultSet.getInt("totalcount")).isEqualTo(7333);
        }
    }

    @Test
    public void testView()
            throws Exception
    {
        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select * from useModel limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getInt("totalprice"));
            int count = 1;
            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }

        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select * from useRelationship limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getString("name"));
            int count = 1;
            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }

        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select * from useRelationshipCustomer limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getString("name"));
            assertThatNoException().isThrownBy(() -> resultSet.getInt("length"));
            int count = 1;

            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }

        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select * from useMetric limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getInt("custkey"));
            assertThatNoException().isThrownBy(() -> resultSet.getInt("totalprice"));
            int count = 1;

            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }

        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select * from useMetricRollUp limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getInt("custkey"));
            assertThatNoException().isThrownBy(() -> resultSet.getInt("totalprice"));
            int count = 1;

            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }

        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select * from useUseMetric limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getInt("custkey"));
            assertThatNoException().isThrownBy(() -> resultSet.getInt("totalprice"));
            int count = 1;

            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }
    }
}
