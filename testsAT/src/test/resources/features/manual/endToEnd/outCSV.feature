@ignore @manual

Feature: Checking an HDFS CSV output with corner cases

  Scenario: Check if the schema is correct
    Given A running policy with any input and an HDFS CSV output
    When I list the hdfs directory where the policy is written the data
    Then I should see how a new directory has been created

  Scenario: Check if the output is being written
    Given A running policy with any input and an HDFS CSV output
    When I get the file from HDFS and visualize it
    Then I should see how the data has been written

  Scenario: Check if the written output is correct
    Given A CSV file written by a policy
    When I double-check the written data
    Then I should see how the aggregation has been written properly

