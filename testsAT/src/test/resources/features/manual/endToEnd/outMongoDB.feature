@ignore @manual

Feature: Checking a MongoDB output with corner cases

  Scenario: Check if the schema is correct
    Given A running policy with any input and a MongoDB output
    When I access the MongoDB instance
    Then I should see an schema created

  Scenario: Check if the output is being written
    Given A running policy with any input and a MongoDB output
    When I access the MongoDB instance
    Then I should see how the MongoDB collection is updated with the aggregated data

  Scenario: Check if the written output is correct
    Given A running policy that is writting data to a MongoDB collection
    When I check the data
    Then I should see how the proper aggregations are being written