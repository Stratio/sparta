Feature: Download policy description JSON

  Scenario: Download a recently created policy
    Given The policy creation UI wizard has been completed up to the last step (confirmation) and the preview json
  has been preserved as a reference
    When The policy creation is confirmed
    And Listed in the policies UI panel
    And The recently policy has been downloaded as a JSON file
    Then The downloaded JSON must fit with the preview reference