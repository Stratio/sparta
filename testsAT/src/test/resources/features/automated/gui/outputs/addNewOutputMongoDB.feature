@web @rest
Feature: Test adding a new MongoDB output in Sparkta GUI
		
	Background: Setup Sparkta GUI
		Given I set web base url to '${SPARKTA_HOST}:${SPARKTA_PORT}'
		Given I send requests to '${SPARKTA_HOST}:${SPARKTA_API_PORT}'
		
	Scenario: Try to add a new input
		Given I browse to '/#/dashboard/outputs'
		Then I wait '1' second
		Then '1' element exists with 'css:button[data-qa="outputs-new-button"]'
		When I click on the element on index '0'
		Then I wait '1' second
		And '1' element exists with 'css:aside[data-qa="fragment-details-modal"]'
		
		# Select MongoDB
		Given '1' element exists with 'css:label[data-qa="fragment-detail-type-mongodb"]'
		When I click on the element on index '0'
		Then I wait '1' second
		
		# Try with empty name
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' element exists with 'css:span[data-qa="fragment-detail-name-error-required"]'

		# Try name with spaces
		Given '1' element exists with 'css:input[data-qa="fragment-detail-name"]'
		Then I type 'valid Flume Input' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' element exists with 'css:span[data-qa="fragment-detail-name-error-pattern"]'
		Given '1' element exists with 'css:input[data-qa="fragment-detail-name"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		
		# Try to add new Host-Port pair
		Given '1' element exists with 'css:i[data-qa="fragment-details-mongoDb-hosts-plus-0"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:input[data-qa="fragment-details-mongoDb-hostName-1"]'
		And '1' element exists with 'css:input[data-qa="fragment-details-mongoDb-port-1"]'
		And '2' elements exist with 'css:i[data-qa^="fragment-details-mongoDb-hosts-plus"]'
		And '2' elements exist with 'css:i[data-qa^="fragment-details-mongoDb-hosts-minus"]'
		Then I wait '2' seconds
		When I click on the element on index '1'
		Then '0' elements exist with 'css:input[data-qa="fragment-details-mongoDb-hostName-1"]'
		And '0' elements exist with 'css:input[data-qa="fragment-details-mongoDb-port-1"]'
		
		# Try empty Database name
		Given '1' element exists with 'css:input[data-qa="fragment-details-mongoDb-dbName"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' element exists with 'css:span[data-qa="fragment-details-mongoDb-dbName-error-required"]'
		
		# Add characters in Connections per host
		Given '1' element exists with 'css:input[data-qa="fragment-details-mongoDb-connectionsPerHost"]'
		Then I type 'invalid' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' element exists with 'css:span[data-qa="fragment-details-mongoDb-connectionsPerHost-error-pattern"]'
		
		# Try empty Connections per host
		Given '1' element exists with 'css:input[data-qa="fragment-details-mongoDb-connectionsPerHost"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# NO error message should appear
		Then '0' element exists with 'css:span[data-qa="fragment-details-mongoDb-connectionsPerHost-error-required"]'
		
		# Add characters in Threads allowed to block
		Given '1' element exists with 'css:input[data-qa="fragment-details-mongoDb-threadsAllowedToBlock"]'
		Then I type 'invalid' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' element exists with 'css:span[data-qa="fragment-details-mongoDb-threadsAllowedToBlock-error-pattern"]'
		
		# Try empty Threads allowed to block
		Given '1' element exists with 'css:input[data-qa="fragment-details-mongoDb-threadsAllowedToBlock"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# NO error message should appear
		Then '0' element exists with 'css:span[data-qa="fragment-details-mongoDb-threadsAllowedToBlock-error-required"]'
		
		# Add characters in Retry sleep
		Given '1' element exists with 'css:input[data-qa="fragment-details-mongoDb-retrySleep"]'
		Then I type 'invalid' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' element exists with 'css:span[data-qa="fragment-details-mongoDb-retrySleep-error-pattern"]'
		
		# Try empty Retry sleep
		Given '1' element exists with 'css:input[data-qa="fragment-details-mongoDb-retrySleep"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# NO error message should appear
		Then '0' element exists with 'css:span[data-qa="fragment-details-mongoDb-retrySleep-error-required"]'
		
		# Select and deselect Id as field
		Given '1' element exists with 'css:label[data-qa="fragment-details-mongoDb-idAsField"]'
		Then I click on the element on index '0'
		And I click on the element on index '0'
		
		# Try with port using letters
		Given '1' element exists with 'css:input[data-qa="fragment-details-mongoDb-port-0"]'
		Then I type 'port' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' element exists with 'css:span[data-qa="fragment-details-mongoDb-port-0-error-pattern"]'
		
		# Try with empty Host
		Given '1' element exists with 'css:input[data-qa="fragment-details-mongoDb-hostName-0"]'
		When I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		# Try with empty Port
		Given '1' element exists with 'css:input[data-qa="fragment-details-mongoDb-port-0"]'
		When I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' elements exist with 'css:span[data-qa="fragment-details-mongoDb-hostName-0-error-required"]'
		And '1' elements exist with 'css:span[data-qa="fragment-details-mongoDb-port-0-error-required"]'
		
		# Try with invalid port number
		Given '1' element exists with 'css:input[data-qa="fragment-details-mongoDb-port-0"]'
		Then I type '66666' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' elements exist with 'css:span[data-qa="fragment-details-mongoDb-port-0-error-pattern"]'
		
		# Fill in name field
		Given '1' element exists with 'css:input[data-qa="fragment-detail-name"]'
		Then I type 'validMongoDBOutput' on the element on index '0'
		# Fill in host field
		Given '1' element exists with 'css:input[data-qa="fragment-details-mongoDb-hostName-0"]'
		Then I type 'localhost' on the element on index '0'
		# Fill in port field
		Given '1' element exists with 'css:input[data-qa="fragment-details-mongoDb-port-0"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		And I type '27017' on the element on index '0'
		# Fill in Database name field
		Given '1' element exists with 'css:input[data-qa="fragment-details-mongoDb-dbName"]'
		Then I type 'sparkta' on the element on index '0'
		
		# Create
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		
		# Check that output fragment has been created
		# Retrieve output fragment id using api
		When I send a 'GET' request to '/fragment/output/name/validmongodboutput'
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID'
		# Check that an output element has been created
		Then '1' element exists with 'css:span[data-qa="output-context-menu-!{previousFragmentID}"]'
		
		# Add same output fragment
		Then '1' element exists with 'css:button[data-qa="outputs-new-button"]'
		When I click on the element on index '0'
		Then I wait '1' second
		And '1' element exists with 'css:aside[data-qa="fragment-details-modal"]'
		# Select MongoDB
		Given '1' element exists with 'css:label[data-qa="fragment-detail-type-mongodb"]'
		When I click on the element on index '0'
		Then I wait '1' second
		# Fill in name field
		Given '1' element exists with 'css:input[data-qa="fragment-detail-name"]'
		Then I type 'validMongoDBOutput' on the element on index '0'
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