@ignore @manual

Feature: Edit policies. This should be tested several times repeatedly

  Scenario: Check whether a policy can be edited
    Given An existing policy is available
    When It has been started
    And Stopped
    And Edited
    Then It's able to start again