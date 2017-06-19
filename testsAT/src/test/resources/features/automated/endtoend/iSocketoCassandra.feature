@rest
Feature: Test policy with Socket input and Cassandra output

  Background: Setup Sparta REST client
    Given I send requests to '${SPARTA_HOST}:${SPARTA_API_PORT}'

  Scenario: Start socket
    Given I start a socket in '@{IP.${IFACE}}:10666'
    And I wait '5' seconds

    # Add the policy
    When I send a 'POST' request to '/policyContext' based on 'schemas/policies/iSocketoCassandra.conf' as 'json' with:
      | id | DELETE | N/A |
      | checkpointPath | UPDATE | /tmp/checkpoint |
      | input.configuration.hostname | UPDATE | @{IP.${IFACE}} |
      | input.configuration.port | UPDATE | 10666 |
      | outputs[0].configuration.connectionHost | UPDATE | ${CASSANDRA_HOST} |
      | outputs[0].configuration.connectionPort | UPDATE | ${CASSANDRA_PORT} |
    Then the service response status must be '200'
    And I save element '$.policyId' in environment variable 'previousPolicyID'
    And I wait '10' seconds

    # Send Data
    Given I send data from file 'src/test/resources/schemas/dataInput/info.csv' to socket
    And I wait '10' seconds

    # Check Data: Aggregation by time
    Given I connect to 'Cassandra' cluster at '${CASSANDRA_HOST}'
    When I execute a query over fields '*' with schema 'schemas/queries/where.conf' of type 'string' with magic_column 'empty' from table: 'testcubewithtime_v1' using keyspace: 'sparta' with:
      | column | UPDATE | product |
      | value  | UPDATE | producta |
    Then There are results found with:
      | avg_price | sum_price | count_price | first_price | last_price | max_price | min_price | fulltext_price | stddev_price | variance_price | range_price | totalentity_text | entitycount_text | occurrences |
      | 639.0     | 5112.0    | 8           | 10          | 600        | 1002.0     | 10.0     | 10 500 1000 500 1000 500 1002 600 | 347.9605889013459 | 121076.57142857143 | 992.0 | 24 | {hola=16, holo=8} | 1 |
    When I execute a query over fields '*' with schema 'schemas/queries/where.conf' of type 'string' with magic_column 'empty' from table: 'testcubewithtime_v1' using keyspace: 'sparta' with:
      | column | UPDATE | product |
      | value  | UPDATE | productb |
    Then There are results found with:
      | avg_price | sum_price | count_price | first_price | last_price | max_price | min_price | fulltext_price | stddev_price | variance_price | range_price | totalentity_text | entitycount_text | occurrences |
      | 758.25    | 6066.0    | 8           | 15          | 50        | 1001.0     | 15.0     | 15 1000 1000 1000 1000 1000 1001 50 | 448.04041590655 | 200740.2142857143 | 986.0 | 24 | {hola=16, holo=8} | 1 |

    # Check Data: Aggregation without time
    Given I connect to 'Cassandra' cluster at '${CASSANDRA_HOST}'
    When I execute a query over fields '*' with schema 'schemas/queries/where.conf' of type 'string' with magic_column 'empty' from table: 'testcubewithouttime_v1' using keyspace: 'sparta' with:
      | column | UPDATE | product |
      | value  | UPDATE | producta |
    Then There are results found with:
      | avg_price | sum_price | count_price | first_price | last_price | max_price | min_price | fulltext_price | stddev_price | variance_price | range_price | totalentity_text | entitycount_text | occurrences |
      | 639.0     | 5112.0    | 8           | 10          | 600        | 1002.0     | 10.0     | 10 500 1000 500 1000 500 1002 600 | 347.9605889013459 | 121076.57142857143 | 992.0 | 24 | {hola=16, holo=8} | 1 |
    When I execute a query over fields '*' with schema 'schemas/queries/where.conf' of type 'string' with magic_column 'empty' from table: 'testcubewithouttime_v1' using keyspace: 'sparta' with:
      | column | UPDATE | product |
      | value  | UPDATE | productb |
    Then There are results found with:
      | avg_price | sum_price | count_price | first_price | last_price | max_price | min_price | fulltext_price | stddev_price | variance_price | range_price | totalentity_text | entitycount_text | occurrences |
      | 758.25    | 6066.0    | 8           | 15          | 50        | 1001.0     | 15.0     | 15 1000 1000 1000 1000 1000 1001 50 | 448.04041590655 | 200740.2142857143 | 986.0 | 24 | {hola=16, holo=8} | 1 |


  # Clean everything up
  Scenario: Clean up
    When I send a 'PUT' request to '/policyContext' based on 'schemas/policies/policyStatusModel.conf' as 'json' with:
      | id | UPDATE | !{previousPolicyID} |
      | status | UPDATE | Stopping |
    Then the service response status must be '201'
    And I wait '5' seconds
    When I send a 'DELETE' request to '/policy/!{previousPolicyID}'
    Then the service response status must be '200'
    And I truncate a Cassandra table named 'testcubewithtime_v1' using keyspace 'sparta'
    And I truncate a Cassandra table named 'testcubewithouttime_v1' using keyspace 'sparta'
    When I send a 'DELETE' request to '/fragment'
    Then the service response status must be '200'
    When I send a 'DELETE' request to '/policy'
    Then the service response status must be '200'
