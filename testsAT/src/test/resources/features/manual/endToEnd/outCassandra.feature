@ignore @manual

Feature: Checking a Cassandra output with corner cases

Feature: Checking reconnect to Cassandra output

  Scenario: Check if the schema is correct
    Given I've created an output feature with valid C* connection parameters
    When A new policy is created using that output
    And It (the policy) has been started
    And The policy input source has received at least one event
    Then I should check that a table has been created at the keyspace "Sparkta" for each cube in the policy

  Scenario: Check if the output is being written
    Given I've created an output feature with valid C* connection parameters
    When A new policy is created using that output
    And It (the policy) has been started
    And The policy input source has received at least one event
    Then The events have modified the contents of each cube table

  Scenario: Check if the written output is correct
    Given I've created an output feature with valid C* connection parameters
    When A new policy is created using that output
    And It (the policy) has been started
    And The policy input source has received at least one event
    Then The aggregated data contained in each cube's table match the transformations described in the policy for
  the events dumped at the input
