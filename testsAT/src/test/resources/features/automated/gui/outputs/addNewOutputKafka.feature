@web @rest
Feature: Test adding a new Kafka output in Sparta GUI

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

   # Select kafka
    Given '1' element exists with 'css:label[data-qa="fragment-detail-type-kafka"]'
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

   # Try to add new Host-Port pair
    Given '1' element exists with 'css:i[data-qa="fragment-details-kafka-metadata-broker-list-plus-1"]'
    When I click on the element on index '0'
    Then '1' element exists with 'css:input[data-qa="fragment-details-kafka-host-1"]'
    And '1' element exists with 'css:input[data-qa="fragment-details-kafka-port-1"]'
    And '1' elements exist with 'css:i[data-qa^="fragment-details-kafka-metadata-broker-list-plus"]'
    And '2' elements exist with 'css:i[data-qa^="fragment-details-kafka-metadata-broker-list-minus"]'
    Then I wait '2' seconds
    When I click on the element on index '1'
    Then '0' elements exist with 'css:input[data-qa="fragment-details-kafka-host-1"]'
    And '0' elements exist with 'css:input[data-qa="fragment-details-kafka-port-1"]'

   # Try with port using letters
    Given '1' element exists with 'css:input[id="dataSource_METADATA_BROKER_LIST_Form-0-port"]'
    Then I type 'port' on the element on index '0'
    Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
    When I click on the element on index '0'
   # Error message should appear
    Then '1' element exists with 'css:span[data-qa="fragment-details-kafka-port-0-error-pattern"]'

   # Try with empty Host
    Given '1' element exists with 'css:input[id="dataSource_METADATA_BROKER_LIST_Form-0-host"]'
    When I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
   # Try with empty Port
    Given '1' element exists with 'css:input[id="dataSource_METADATA_BROKER_LIST_Form-0-port"]'
    When I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
   # Try with empty Serializer class for messages
    Given '1' element exists with 'css:input[data-qa="fragment-details-kafka-serializer-class"]'
    When I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
   # Try with empty Producer type
    Given '1' element exists with 'css:input[data-qa="fragment-details-kafka-producer-type"]'
    When I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
   # Try with empty num messages
    Given '1' element exists with 'css:input[data-qa="fragment-details-kafka-batch-num-messages"]'
    When I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
    Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
    When I click on the element on index '0'
   # Error message should appear
    Then '1' elements exist with 'css:span[data-qa="fragment-details-kafka-host-0-error-required"]'
    And '1' elements exist with 'css:span[data-qa="fragment-details-kafka-port-0-error-required"]'
    And '1' elements exist with 'css:span[data-qa="fragment-details-kafka-serializer-class-error-required"]'
    And '1' elements exist with 'css:span[data-qa="fragment-details-kafka-producer-type-error-required"]'
    And '1' elements exist with 'css:span[data-qa="fragment-details-kafka-batch-num-messages-error-required"]'

   # Try with invalid port number
    Given '1' element exists with 'css:input[id="dataSource_METADATA_BROKER_LIST_Form-0-port"]'
    Then I type '66666' on the element on index '0'
    Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
    When I click on the element on index '0'
   # Error message should appear
    Then '1' elements exist with 'css:span[data-qa="fragment-details-kafka-port-0-error-pattern"]'

   # Try with invalid Producer type
    Given '1' element exists with 'css:input[data-qa="fragment-details-kafka-producer-type"]'
    Then I type 'invalid' on the element on index '0'
    Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
    When I click on the element on index '0'
   # Error message should appear
    Then '1' elements exist with 'css:span[data-qa="fragment-details-kafka-producer-type-error-pattern"]'

   # Try with invalid Batch numm messages
    Given '1' element exists with 'css:input[data-qa="fragment-details-kafka-batch-num-messages"]'
    Then I type 'invalid' on the element on index '0'
    Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
    When I click on the element on index '0'
   # Error message should appear
    Then '1' elements exist with 'css:span[data-qa="fragment-details-kafka-batch-num-messages-error-pattern"]'

   # Fill in name field
    Given '1' element exists with 'css:input[data-qa="fragment-detail-name"]'
    Then I type 'validKafkaOutput' on the element on index '0'
   # Fill in host field
    Given '1' element exists with 'css:input[id="dataSource_METADATA_BROKER_LIST_Form-0-host"]'
    Then I type 'localhost' on the element on index '0'
   # Fill in port field
    Given '1' element exists with 'css:input[id="dataSource_METADATA_BROKER_LIST_Form-0-port"]'
    Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
    And I type '2181' on the element on index '0'
   # Fill in Serializer class
    Given '1' element exists with 'css:input[data-qa="fragment-details-kafka-serializer-class"]'
    Then I type 'kafka.serializer.StringEncoder' on the element on index '0'
   # Fill in kafka producer wait for acknowledgement
    Given '1' element exists with 'css:label[data-qa="fragment-details-kafka-request-required-acks"]'
    Then I click on the element on index '0'
   # Fill in Producer type
    Given '1' element exists with 'css:input[data-qa="fragment-details-kafka-producer-type"]'
    Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
    And I type 'async' on the element on index '0'
   # Fill in Batch numm messages
    Given '1' element exists with 'css:input[data-qa="fragment-details-kafka-batch-num-messages"]'
    Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
    And I type '200' on the element on index '0'

   # Create
    Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
    When I click on the element on index '0'
   # Check that output fragment has been created
   # Retrieve output fragment id using api
    When I send a 'GET' request to '/fragment/output/name/validkafkaoutput'
    Then the service response status must be '200'.
    And I save element '$.id' in environment variable 'previousFragmentID'
   # Check that an output element has been created
    Then '1' element exists with 'css:span[data-qa="output-context-menu-!{previousFragmentID}"]'

   # Add same output fragment
    Then '1' element exists with 'css:button[data-qa="output-filter-new-output"]'
    When I click on the element on index '0'
    Then I wait '1' second
    And '1' element exists with 'css:aside[data-qa="fragment-details-modal"]'
   # Select kafka
    Given '1' element exists with 'css:label[data-qa="fragment-detail-type-kafka"]'
    When I click on the element on index '0'
    Then I wait '1' second
   # Fill in name field
    Given '1' element exists with 'css:input[data-qa="fragment-detail-name"]'
    Then I type 'validKafkaOutput' on the element on index '0'
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
