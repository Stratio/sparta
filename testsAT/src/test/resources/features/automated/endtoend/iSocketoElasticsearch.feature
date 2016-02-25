@rest
Feature: Test policy with Socket input and Elasticsearch output

  Background: Setup Sparta REST client
    Given I send requests to '${SPARTA_HOST}:${SPARTA_API_PORT}'

  Scenario: Start socket
    Given I start a socket in '@{IP.${IFACE}}:10666'
    And I wait '5' seconds

    # Add the policy
    When I send a 'POST' request to '/policy' based on 'schemas/policies/iSocketoElasticsearch.conf' as 'json' with:
      | id | DELETE | N/A |
      | input.configuration.hostname | UPDATE | @{IP.${IFACE}} |
      | input.configuration.port | UPDATE | 10666 |
      | outputs[0].configuration.nodes[0].node | UPDATE | ${ELASTICSEARCH_HOST} |
      | outputs[0].configuration.clusterName       | UPDATE | elasticsearch              |
    Then the service response status must be '200'.
    And I save element '$.id' in environment variable 'previousPolicyID'
    And I wait '10' seconds

    # Start the policy
    When I send a 'GET' request to '/policy/run/!{previousPolicyID}'
    Then the service response status must be '200' and its response must contain the text '{"message":"Creating new context'

    # Send Data
    Given I send data from file 'src/test/resources/schemas/dataInput/info.csv' to socket
    And I wait '10' seconds

    # Check Data aggregated by time
    Given I connect to 'Elasticsearch' cluster at '${ELASTICSEARCH_HOST}'
    When I execute query 'schemas/queries/elasticsearch.conf' of type 'string' in 'elasticsearch' database 'testcubewithtime' using collection 'day_v1' with:
      | productValue | UPDATE | producta |
    Then There are results found with:
      | avg_price | sum_price | count_price | first_price | last_price | max_price | min_price | fulltext_price | stddev_price | variance_price | range_price | totalEntity_text | entityCount_text | occurrences |
      | 639.0     | 5112.0    | 8           | 10          | 600        | 1002.0    | 10.0      | 10 500 1000 500 1000 500 1002 600 | 347.9605889013459 | 121076.57142857143 | 992.0 | 24 | {"holo":8,"hola":16} | 1 |
    When I execute query 'schemas/queries/elasticsearch.conf' of type 'string' in 'elasticsearch' database 'testcubewithtime' using collection 'day_v1' with:
      | productValue | UPDATE | productb |
    Then There are results found with:
      | avg_price | sum_price | count_price | first_price | last_price | max_price | min_price | fulltext_price | stddev_price | variance_price | range_price | totalEntity_text | entityCount_text | occurrences |
      | 758.25     | 6066.0   | 8           | 15          | 50         | 1001.0    | 15.0      | 15 1000 1000 1000 1000 1000 1001 50 | 448.04041590655 | 200740.2142857143 | 986.0 | 24 | {"holo":8,"hola":16} | 1 |

    # Check Data aggregated without time
    Given I connect to 'Elasticsearch' cluster at '${ELASTICSEARCH_HOST}'
    When I execute query 'schemas/queries/elasticsearch.conf' of type 'string' in 'elasticsearch' database 'testcubewithouttime' using collection 'day_v1' with:
      | productValue | UPDATE | producta |
    Then There are results found with:
      | avg_price | sum_price | count_price | first_price | last_price | max_price | min_price | fulltext_price | stddev_price | variance_price | range_price | totalEntity_text | entityCount_text | occurrences |
      | 639.0     | 5112.0    | 8           | 10          | 600        | 1002.0    | 10.0      | 10 500 1000 500 1000 500 1002 600 | 347.9605889013459 | 121076.57142857143 | 992.0 | 24 | {"holo":8,"hola":16} | 1 |
    When I execute query 'schemas/queries/elasticsearch.conf' of type 'string' in 'elasticsearch' database 'testcubewithouttime' using collection 'day_v1' with:
      | productValue | UPDATE | productb |
    Then There are results found with:
      | avg_price | sum_price | count_price | first_price | last_price | max_price | min_price | fulltext_price | stddev_price | variance_price | range_price | totalEntity_text | entityCount_text | occurrences |
      | 758.25     | 6066.0   | 8           | 15          | 50         | 1001.0    | 15.0      | 15 1000 1000 1000 1000 1000 1001 50 | 448.04041590655 | 200740.2142857143 | 986.0 | 24 | {"holo":8,"hola":16} | 1 |

  # Clean everything up
  Scenario: Clean up
    When I send a 'PUT' request to '/policyContext' based on 'schemas/policies/policyStatusModel.conf' as 'json' with:
      | id | UPDATE | !{previousPolicyID} |
      | status | UPDATE | Stopping |
    Then the service response status must be '201'.
    And I wait '5' seconds
    When I send a 'DELETE' request to '/policy/!{previousPolicyID}'
    Then the service response status must be '200'.
    And I drop an elasticsearch index named 'testcube'