@ignore @manual

Feature: Edit policies

  Scenario: Check whether a policy can be edited
    Given An existing policy is available
    When It has been started
    And Stopped
    And Edited
    Then It's able to start again