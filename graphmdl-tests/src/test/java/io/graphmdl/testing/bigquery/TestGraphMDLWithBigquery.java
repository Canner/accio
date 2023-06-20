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
import org.intellij.lang.annotations.Language;
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
    public static Object[][] functionIndex()
    {
        return new Object[][] {
                {"select filter(orders, orderItem -> orderItem.orderstatus = 'F')[1].orderstatus as col_1 from Customer limit 100"},
                {"select filter(Customer.orders, orderItem -> orderItem.orderstatus = 'F')[1].orderstatus as col_1 from Customer limit 100"},
                {"select filter(Customer.orders, orderItem -> orderItem.orderstatus = 'F')[1].customer.name as col_1 from Customer limit 100"},
                {"select filter(Customer.orders, orderItem -> orderItem.orderstatus = 'F')[1].customer.orders[2].orderstatus as col_1 from Customer limit 100"},
                {"select filter(Customer.orders[1].lineitem, lineitem -> lineitem.linenumber = 1)[1].linenumber as col_1 from Customer limit 100"},
                {"select filter(filter(Customer.orders[1].lineitem, lineitem -> lineitem.linenumber = 1), lineitem -> lineitem.partkey = 1)[1].linenumber as col_1 from Customer limit 100"},
        };
    }

    @Test(dataProvider = "functionIndex")
    public void testFunctionIndex(String sql)
            throws Exception
    {
        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getString("col_1"));
            int count = 1;
            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
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
            }).hasMessageStartingWith("ERROR: There should be only one relationship field function chain in dereference expression");
        }
    }

    @Test
    public void testAggregateForArray()
    {
        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select array_sum(orders, a -> a.totalprice) as col_1 from Customer limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getDouble("col_1"));
            int count = 1;
            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select array_avg(orders, a -> a.totalprice) as col_1 from Customer limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getDouble("col_1"));
            int count = 1;
            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select array_count(orders, a -> a.totalprice) as col_1 from Customer limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getDouble("col_1"));
            int count = 1;
            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select array_max(orders, a -> a.totalprice) as col_1 from Customer limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getDouble("col_1"));
            int count = 1;
            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select array_min(orders, a -> a.totalprice) as col_1 from Customer limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getDouble("col_1"));
            int count = 1;
            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select array_min(filter(orders, a -> a.orderstatus = 'F'), a -> a.totalprice) as col_1 from Customer limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getDouble("col_1"));
            int count = 1;
            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select array_bool_or(orders, a -> a.orderstatus = 'F') as col_1 from Customer limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getDouble("col_1"));
            int count = 1;
            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select array_every(orders, a -> a.orderstatus = 'F') as col_1 from Customer limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getDouble("col_1"));
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

        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select linenumber, orders.customer.name from Lineitem limit 100");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getInt("linenumber"));
            assertThatNoException().isThrownBy(() -> resultSet.getString("name"));

            int count = 1;

            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }

        // TODO: analyze nested to_many relationship access
        // try (Connection connection = createConnection()) {
        //     PreparedStatement stmt = connection.prepareStatement("select name, orders[1].lineitem[1].extendedprice from Customer limit 100");
        //     ResultSet resultSet = stmt.executeQuery();
        //     resultSet.next();
        //     assertThatNoException().isThrownBy(() -> resultSet.getInt("name"));
        //     assertThatNoException().isThrownBy(() -> resultSet.getString("extendedprice"));
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

    @DataProvider
    public Object[][] anyFunction()
    {
        return new Object[][] {
                {"SELECT (any(filter(orders, orderItem -> orderItem.orderstatus = 'F')) IS NOT NULL) AS col_1 FROM Customer LIMIT 100"},
                {"SELECT any(filter(orders, orderItem -> orderItem.orderstatus = 'F')).totalprice FROM Customer LIMIT 100"},
                // useAny is a view that invoke any function
                {"SELECT * FROM useAny"}
        };
    }

    @Test(dataProvider = "anyFunction")
    public void testAnyFunction(@Language("sql") String sql)
            throws SQLException
    {
        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getObject(1));
            int count = 1;

            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }
    }

    @DataProvider
    public Object[][] arraySort()
    {
        return new Object[][] {
                {"SELECT array_sort(orders, totalprice, DESC) FROM Customer LIMIT 100"},
                {"SELECT array_sort(c.orders, totalprice, DESC) FROM Customer c LIMIT 100"},
                {"SELECT array_sort(filter(orders, orderItem -> orderItem.orderstatus = 'F'), totalprice, ASC) FROM Customer LIMIT 100"},
                {"SELECT array_sort(filter(orders, orderItem -> orderItem.orderstatus = 'F'), totalprice, ASC)[1].totalprice FROM Customer LIMIT 100"},
        };
    }

    @Test(dataProvider = "arraySort")
    public void testArraySortFunction(String sql)
            throws SQLException
    {
        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getObject(1));
            int count = 1;

            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }
    }

    @DataProvider
    public Object[][] first()
    {
        return new Object[][] {
                {"SELECT first(orders, totalprice, DESC) IS NOT NULL FROM Customer LIMIT 100"},
                {"SELECT first(c.orders, totalprice, DESC).totalprice FROM Customer c LIMIT 100"},
                {"SELECT first(filter(orders, orderItem -> orderItem.orderstatus = 'F'), totalprice, ASC) IS NOT NULL FROM Customer LIMIT 100"},
                {"SELECT first(filter(orders, orderItem -> orderItem.orderstatus = 'F'), totalprice, ASC).totalprice FROM Customer LIMIT 100"},
        };
    }

    @Test(dataProvider = "first")
    public void testFirstFunction(String sql)
            throws SQLException
    {
        try (Connection connection = createConnection()) {
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            assertThatNoException().isThrownBy(() -> resultSet.getObject(1));
            int count = 1;

            while (resultSet.next()) {
                count++;
            }
            assertThat(count).isEqualTo(100);
        }
    }
}
