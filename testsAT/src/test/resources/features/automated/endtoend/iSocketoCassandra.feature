@rest
Feature: Test policy with Socket input and Cassandra output

  Background: Setup Sparkta REST client
    Given I send requests to '${SPARKTA_HOST}:${SPARKTA_API_PORT}'

  Scenario: Start socket
    Given I start a socket in '@{IP.${IFACE}}:10666'
    And I wait '5' seconds

    # Add the policy
    When I send a 'POST' request to '/policy' based on 'schemas/policies/iSocketoCassandra.conf' as 'json' with:
      | id | DELETE | N/A |
      | input.configuration.hostname | UPDATE | @{IP.${IFACE}} |
      | input.configuration.port | UPDATE | 10666 |
      | outputs[0].configuration.connectionHost | UPDATE | ${CASSANDRA_HOST} |
      | outputs[0].configuration.connectionPort | UPDATE | ${CASSANDRA_PORT} |
    Then the service response status must be '200'.
    And I save element '$.id' in attribute 'previousPolicyID'
    And I wait '10' seconds

    # Start the policy
    When I send a 'GET' request to '/policy/run/!{previousPolicyID}'
    Then the service response status must be '200' and its response must contain the text '{"message":"Creating new context'

    # Send Data
    Given I send data from file 'src/test/resources/schemas/dataInput/info.csv' to socket
    And I wait '10' seconds

    # Check Data
    Given I connect to 'Cassandra' cluster at '${CASSANDRA_HOST}'
    When I execute a query over fields '*' with schema 'schemas/queries/where.conf' of type 'string' with magic_column 'empty' from table: 'testcube_v1' using keyspace: 'sparkta' with:
      | column | UPDATE | product |
      | value  | UPDATE | producta |
    Then There are results found with:
      | avg_price | sum_price | count_price | first_price | last_price | max_price | min_price | fulltext_price | stddev_price | variance_price | range_price | totalentity_text | entitycount_text | occurrences |
      | 639.0     | 5112.0    | 8           | 10          | 600        | 1002.0     | 10.0     | 10 500 1000 500 1000 500 1002 600 | 347.9605889013459 | 121076.57142857143 | 992.0 | 24 | {hola=16, holo=8} | 1 |

    # Clean everything up
    When I send a 'PUT' request to '/policyContext' based on 'schemas/policies/policyStatusModel.conf' as 'json' with:
      | id | UPDATE | !{previousPolicyID} |
      | status | UPDATE | Stopping |
    Then the service response status must be '201'.
    When I send a 'DELETE' request to '/policy/!{previousPolicyID}'
    Then the service response status must be '200'.
    And I truncate a Cassandra table named 'testcube_v1' using keyspace 'sparkta'
