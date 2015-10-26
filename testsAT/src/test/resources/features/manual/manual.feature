@ignore @manual

Feature: Manual template

  Background: This is gonna be executed before every scenario.
    Given I perform an action
    When I wait for something
    Then I verify something

  Scenario: Manual test evaluation
    Given I perform an action
    When I wait for something
    Then I verify something

  Scenario Outline: Manual test evaluation with different cases.
    Given I perform an action
    When I wait for <something>
    Then I verify <value>
  Examples:

    | something | value |
    | example   | test  |
