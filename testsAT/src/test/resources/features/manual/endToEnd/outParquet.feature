@ignore @manual

Feature: Make an HDFS Parquet output with corner cases

  Scenario: Check if the schema is correct
    Given I've created an output feature with valid HDFS+Parquet connection parameters
    When A new policy is created using that output
    And It (the policy) has been started
    And The policy input source has received at least one event
    Then A new HDFS folder named after each cube's dimensions should be created

  Scenario: Check if the output is being written
    Given I've created an output feature with valid HDFS+Parquet connection parameters
    When A new policy is created using that output
    And It (the policy) has been started
    And The policy input source has received at least one event
    Then The events have modified the contents of each cube table (it can be easily checked used SparkSQL)

  Scenario: Check if the written output is correct
    Given I've created an output feature with valid HDFS+Parquet connection parameters
    When A new policy is created using that output
    And It (the policy) has been started
    And The policy input source has received at least one event
    Then The aggregated data contained in each cube's table match the transformations described in the policy for
  the events dumped at the input
