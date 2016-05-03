@web @rest
Feature: Test adding a new JDBC output in Sparta GUI

  Background: Setup Sparta GUI
    Given I set web base url to '${SPARTA_HOST}:${SPARTA_PORT}'
    Given I send requests to '${SPARTA_HOST}:${SPARTA_API_PORT}'

  Scenario: Try to add a new output
    Given I browse to '/#/dashboard/outputs'
    Then I wait '1' second
    Then '1' element exists with 'css:div[data-qa="output-first-message"]'
    When I click on the element on index '0'
    Then I wait '1' second
    And '1' element exists with 'css:aside[data-qa="fragment-details-modal"]'

   # Select JDBC
    Given '1' element exists with 'css:label[data-qa="fragment-detail-type-jdbc"]'
    When I click on the element on index '0'
    Then I wait '1' second

   # Try with empty name
    Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
    When I click on the element on index '0'
   # Error message should appear (Name)
    Then '1' element exists with 'css:span[data-qa="fragment-detail-name-error-required"]'

   # Try name with spaces
    Given '1' element exists with 'css:input[data-qa="fragment-detail-name"]'
    Then I type 'valid Kafka Input' on the element on index '0'
    Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
    When I click on the element on index '0'
   # Error message should appear
    Then '1' element exists with 'css:span[data-qa="fragment-detail-name-error-pattern"]'
    Given '1' element exists with 'css:input[data-qa="fragment-detail-name"]'
    Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'

   # Try with empty Url
    Given '1' element exists with 'css:input[data-qa="fragment-details-jdbc-url"]'
    When I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
    Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
    When I click on the element on index '0'
   # Error message should appear
    Then '1' elements exist with 'css:span[data-qa="fragment-details-jdbc-url-error-required"]'

   # Fill in name field
    Given '1' element exists with 'css:input[data-qa="fragment-detail-name"]'
    Then I type 'validJDBCOutput' on the element on index '0'
   # Fill in url
    Given '1' element exists with 'css:input[data-qa="fragment-details-jdbc-url"]'
    Then I type 'jdbc:postgresql:dbserver' on the element on index '0'

   # Create
    Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
    When I click on the element on index '0'
   # Check that output fragment has been created
   # Retrieve output fragment id using api
    When I send a 'GET' request to '/fragment/output/name/validjdbcoutput'
    Then the service response status must be '200'.
    And I save element '$.id' in environment variable 'previousFragmentID'
   # Check that an output element has been created
    Then '1' element exists with 'css:span[data-qa="output-context-menu-!{previousFragmentID}"]'

   # Add same output fragment
    Then '1' element exists with 'css:button[data-qa="output-filter-new-output"]'
    When I click on the element on index '0'
    Then I wait '1' second
    And '1' element exists with 'css:aside[data-qa="fragment-details-modal"]'
   # Select JDBC
    Given '1' element exists with 'css:label[data-qa="fragment-detail-type-jdbc"]'
    When I click on the element on index '0'
    Then I wait '1' second
   # Fill in name field
    Given '1' element exists with 'css:input[data-qa="fragment-detail-name"]'
    Then I type 'validJDBCOutput' on the element on index '0'
   # Create
    Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
    When I click on the element on index '0'
    Then '1' element exists with 'css:span[translate="_ERROR_._100_"]'
    And a text 'There was an error. The name of the fragment already exists!' exists

   # Cancel operation
    Given '1' element exists with 'css:button[data-qa="modal-cancel-button"]'
    Then I click on the element on index '0'
   # Check pop up is closed
    And I wait '1' second
    Then '0' element exists with 'css:button[data-qa="modal-cancel-button"]'

   # Delete output fragment created
    Given '1' element exists with 'css:span[data-qa="output-context-menu-!{previousFragmentID}"]'
    Then I click on the element on index '0'
    And I wait '1' second
    Given '1' element exists with 'css:st-menu-element[data-qa="output-context-menu-!{previousFragmentID}-delete"]'
    When I click on the element on index '0'
    Then I wait '1' second
    And '1' element exists with 'css:aside[data-qa="delete-modal"]'
    Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
    Then I click on the element on index '0'
    And I wait '1' second
    And '0' element exists with 'css:span[data-qa="output-context-menu-!{previousFragmentID}"]'
