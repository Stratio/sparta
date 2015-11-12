@ignore @manual

Feature: Checking reconnect to MongoDB output

  Scenario: Check if the policy can reconnect to MongoDB
    Given A running policy with any input and a MongoDB output
    When I stop the MongoDB service
    And I restart the service again
    Then I should see how the policy keep writting data
