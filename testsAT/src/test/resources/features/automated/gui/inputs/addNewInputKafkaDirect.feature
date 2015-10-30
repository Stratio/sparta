@web @rest
Feature: Test adding a new Kafka Direct input in Sparkta GUI
		
	Background: Setup Sparkta GUI
		Given I set web base url to '${SPARKTA_HOST}:${SPARKTA_PORT}'
		Given I send requests to '${SPARKTA_HOST}:${SPARKTA_API_PORT}'
	
	Scenario: Try to add a new input
		Given I browse to '/#/dashboard/inputs'
		Then I wait '1' second
		Then '1' element exists with 'css:button[data-qa="inputs-new-button"]'
		When I click on the element on index '0'
		Then I wait '1' second
		And '1' element exists with 'css:aside[data-qa="fragment-details-modal"]'
		
		# Select kafka direct
		Given '1' element exists with 'css:label[data-qa="fragment-detail-type-kafkadirect"]'
		When I click on the element on index '0'
		Then I wait '1' second
		
		# Try with empty name
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear (Name, Group ID and Topic)
		# Error message should appear (Name, Group ID and Topic)
		Then '1' element exists with 'css:span[data-qa="fragment-detail-name-error-required"]'
		And '1' elements exist with 'css:span[data-qa="fragment-details-kafkadirect-group-id-error-required"]'
		And '1' elements exist with 'css:span[data-qa="fragment-details-kafkadirect-topic-0-error-required"]'
		
		# Try name with spaces
		Given '1' element exists with 'css:input[data-qa="fragment-detail-name"]'
		Then I type 'valid Kafka Direct Input' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' element exists with 'css:span[data-qa="fragment-detail-name-error-pattern"]'
		Given '1' element exists with 'css:input[data-qa="fragment-detail-name"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'	
		
		# Try to add new Host-Port pair
		Given '1' element exists with 'css:i[data-qa="fragment-details-kafkadirect-broker-list-plus-0"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:input[data-qa="fragment-details-kafkadirect-broker-1"]'
		And '1' element exists with 'css:input[data-qa="fragment-details-kafkadirect-port-1"]'
		And '2' elements exist with 'css:i[data-qa^="fragment-details-kafkadirect-broker-list-plus"]'
		And '2' elements exist with 'css:i[data-qa^="fragment-details-kafkadirect-broker-list-minus"]'
		Then I wait '2' seconds
		When I click on the element on index '1'
		Then '0' elements exist with 'css:input[data-qa="fragment-details-kafkadirect-broker-1"]'
		And '0' elements exist with 'css:input[data-qa="fragment-details-kafkadirect-port-1"]'
		
		# Try with port using letters
		Given '1' element exists with 'id:dataSourceForm0-port'
		Then I type 'port' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' element exists with 'css:span[data-qa="fragment-details-kafkadirect-port-0-error-pattern"]'
			
		# Try with empty Broker
		Given '1' element exists with 'css:input[data-qa="fragment-details-kafkadirect-broker-0"]'
		When I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		# Try with empty Port
		Given '1' element exists with 'css:input[data-qa="fragment-details-kafkadirect-port-0"]'
		When I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' elements exist with 'css:span[data-qa="fragment-details-kafkadirect-broker-0-error-required"]'
		And '1' elements exist with 'css:span[data-qa="fragment-details-kafkadirect-port-0-error-required"]'
		
		# Try with invalid port number
		Given '1' element exists with 'css:input[data-qa="fragment-details-kafkadirect-port-0"]'
		Then I type '66666' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' elements exist with 'css:span[data-qa="fragment-details-kafkadirect-port-0-error-pattern"]'
		
		# Fill in name field
		Given '1' element exists with 'css:input[data-qa="fragment-detail-name"]'
		Then I type 'validKafkaDirectInput' on the element on index '0'
		# Fill in broker field
		Given '1' element exists with 'css:input[data-qa="fragment-details-kafkadirect-broker-0"]'
		Then I type 'localhost' on the element on index '0'
		# Fill in port field
		Given '1' element exists with 'css:input[data-qa="fragment-details-kafkadirect-port-0"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		And I type '9092' on the element on index '0'
		# Fill in Group ID
		Given '1' element exists with 'css:input[data-qa="fragment-details-kafkadirect-group-id"]'
		Then I type 'myGroupID' on the element on index '0'
		# Fill in Topic
		Given '1' element exists with 'css:input[data-qa="fragment-details-kafkadirect-topic-0"]'
		Then I type 'myTopic' on the element on index '0'
		
		# Create
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Check that input fragment has been created
		# Retrieve input fragment id using api
		When I send a 'GET' request to '/fragment/input/name/validkafkadirectinput'
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID'
		# Check that an input element has been created
		Then '1' element exists with 'css:span[data-qa="input-context-menu-!{previousFragmentID}"]'
		
		# Add same input fragment
		Then '1' element exists with 'css:button[data-qa="inputs-new-button"]'
		When I click on the element on index '0'
		Then I wait '1' second
		And '1' element exists with 'css:aside[data-qa="fragment-details-modal"]'
		# Select kafka direct
		Given '1' element exists with 'css:label[data-qa="fragment-detail-type-kafkadirect"]'
		When I click on the element on index '0'
		Then I wait '1' second
		# Fill in name field
		Given '1' element exists with 'css:input[data-qa="fragment-detail-name"]'
		Then I type 'validKafkaDirectInput' on the element on index '0'
		# Fill in Group ID
		Given '1' element exists with 'css:input[data-qa="fragment-details-kafkadirect-group-id"]'
		Then I type 'myGroupID' on the element on index '0'
		# Fill in Topic
		Given '1' element exists with 'css:input[data-qa="fragment-details-kafkadirect-topic-0"]'
		Then I type 'myTopic' on the element on index '0'
		# Create
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:span[translate="_INPUT_ERROR_100_"]'
		
		# Cancel operation
		Given '1' element exists with 'css:button[data-qa="modal-cancel-button"]'
		Then I click on the element on index '0'
		# Check pop up is closed
		And I wait '1' second
		Then '0' element exists with 'css:button[data-qa="modal-cancel-button"]'
		
		# Delete input fragment created
		Given '1' element exists with 'css:span[data-qa="input-context-menu-!{previousFragmentID}"]'
		Then I click on the element on index '0'
		And I wait '1' second
		Given '1' element exists with 'css:st-menu-element[data-qa="input-context-menu-!{previousFragmentID}-delete"]'
		When I click on the element on index '0'
		Then I wait '1' second
		And '1' element exists with 'css:aside[data-qa="delete-modal"]'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		Then I click on the element on index '0'
		And I wait '1' second
		And '0' element exists with 'css:span[data-qa="input-context-menu-!{previousFragmentID}"]'