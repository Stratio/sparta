@ignore @manual

Feature: Make a DateTime transformation to add a timestamp

  Background: Use a common input for every test

  Scenario: Create a DateTime transformation
    Given An extraction/transformation morphline with a text formatted timestamp output field `ts` has been described
    When Including a description of a `DateTime` transformation over `ts`
    Then The generated morphline pipeline should have a DateTime output field.
