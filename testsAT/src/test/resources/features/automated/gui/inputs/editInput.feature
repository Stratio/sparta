@web @rest
Feature: Test editing an input in Sparkta GUI
		
	Background: Setup Sparkta GUI
		Given I set web base url to '${SPARKTA_HOST}:${SPARKTA_PORT}'
		Given I send requests to '${SPARKTA_HOST}:${SPARKTA_API_PORT}'
		
	Scenario: Try to edit an existing input
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| fragmentType | UPDATE | input |
		| name | UPDATE | inputfragment1 |
		| element.type | UPDATE | Flume |
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID'
		When I send a 'GET' request to '/fragment/input'
		Then the service response status must be '200' and its response length must be '1'

		Given I browse to '/#/dashboard/inputs'
		Then I wait '1' second
		Given '1' element exists with 'css:span[data-qa="input-context-menu-!{previousFragmentID}"]'
		Then I click on the element on index '0'
		And I wait '1' second
		Given '1' element exists with 'css:st-menu-element[data-qa="input-context-menu-!{previousFragmentID}-edit"]'
		When I click on the element on index '0'
		Then I wait '1' second
		And '1' element exists with 'css:aside[data-qa="fragment-details-modal"]'
		
		# Try with empty name
		Given '1' element exists with 'css:input[data-qa="fragment-detail-name"]'
		When I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
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
		
		# Make sure we are in pull in drop-down
		Given '1' element exists with 'css:select[data-qa="fragment-details-flume-type"]'
		Then I select 'pull' on the element on index '0'
		Then I wait '1' second
				
		# Try with empty Host
		Given '1' element exists with 'css:input[data-qa="fragment-details-flume-pull-host-0"]'
		When I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		Then '1' elements exist with 'css:span[data-qa="fragment-details-flume-pull-host-0-error-required"]'
		
		# Try with invalid Host
		Given '1' element exists with 'css:input[data-qa="fragment-details-flume-pull-host-0"]'
		When I type '@@@' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:span[data-qa="fragment-details-flume-pull-host-0-error-pattern"]'
		
		# Try with port using letters
		Given '1' element exists with 'css:input[data-qa="fragment-details-flume-pull-port-0"]'
		Then I type 'port' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' element exists with 'css:span[data-qa="fragment-details-flume-pull-port-0-error-pattern"]'
		
		# Try with empty Port
		Given '1' element exists with 'css:input[data-qa="fragment-details-flume-pull-port-0"]'
		When I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		Then '1' elements exist with 'css:span[data-qa="fragment-details-flume-pull-port-0-error-required"]'
				
		# Try to add new Host-Port pair
		Given '1' element exists with 'css:i[data-qa="fragment-details-flume-pull-addresses-plus-0"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:input[data-qa="fragment-details-flume-pull-host-1"]'
		And '1' element exists with 'css:input[data-qa="fragment-details-flume-pull-port-1"]'
		And '1' elements exist with 'css:i[data-qa="fragment-details-flume-pull-addresses-minus-0"]'
		And '1' elements exist with 'css:i[data-qa="fragment-details-flume-pull-addresses-minus-1"]'
		And I wait '1' second
		When I click on the element on index '0'
		Then '0' elements exist with 'css:input[data-qa="fragment-details-flume-pull-host-1"]'
		And '0' elements exist with 'css:input[data-qa="fragment-details-flume-pull-port-1"]'
		
		# Try with invalid Max batch size and Parallelism
		Given '1' element exists with 'css:input[data-qa="fragment-details-flume-pull-max-batch-size"]'
		Then I type 'invalid' on the element on index '0'
		Given '1' element exists with 'css:input[data-qa="fragment-details-flume-pull-parallelism"]'
		Then I type 'invalid' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' element exists with 'css:span[data-qa="fragment-details-flume-pull-max-batch-size-error-pattern"]'
		And '1' elements exist with 'css:span[data-qa="fragment-details-flume-pull-parallelism-error-pattern"]'
		
		# Fill in name field
		Given '1' element exists with 'css:input[data-qa="fragment-detail-name"]'
		Then I type 'inputfragment1New' on the element on index '0'
		# Fill in host field
		Given '1' element exists with 'css:input[data-qa="fragment-details-flume-pull-host-0"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		And I type 'localhost' on the element on index '0'
		# Fill in port field
		Given '1' element exists with 'css:input[data-qa="fragment-details-flume-pull-port-0"]'
		Then I type '11999' on the element on index '0'
		# Empty Max batch size
		Given '1' element exists with 'css:input[data-qa="fragment-details-flume-pull-max-batch-size"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		# Empty Parallelism
		Given '1' element exists with 'css:input[data-qa="fragment-details-flume-pull-parallelism"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		
		# Modify
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# I have to wait a while, if not 2 inputs will be reported
		Then I wait '2' seconds
		# Check that input fragment has been created
		# Retrieve input fragment id using api
		When I send a 'GET' request to '/fragment/input/name/inputfragment1new'
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID_2'
		# Check that an input element has been created
		Then '1' element exists with 'css:span[data-qa="input-context-menu-!{previousFragmentID_2}"]'
		
		Scenario: Delete everything
		When I send a 'DELETE' request to '/fragment/input/!{previousFragmentID_2}'
		Then the service response status must be '200'.