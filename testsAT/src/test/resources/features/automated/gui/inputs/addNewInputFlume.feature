@web @rest
Feature: Test adding a new Flume input in Sparta GUI
		
	Background: Setup Sparta GUI
		Given I set web base url to '${SPARTA_HOST}:${SPARTA_PORT}'
		Given I send requests to '${SPARTA_HOST}:${SPARTA_API_PORT}'
		
	Scenario: Try to add a new input
		Given I browse to '/#/dashboard/inputs'
		Then I wait '1' second
		Then '1' element exists with 'css:div[data-qa="input-first-message"]'
		When I click on the element on index '0'
		Then I wait '1' second
		And '1' element exists with 'css:aside[data-qa="fragment-details-modal"]'
	
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
		
		# Try with invalid port number
		Given '1' element exists with 'css:input[data-qa="fragment-details-flume-pull-port-0"]'
		Then I type '66666' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' elements exist with 'css:span[data-qa="fragment-details-flume-pull-port-0-error-pattern"]'
				
		# Try to add new Host-Port pair
		Given '1' element exists with 'css:i[data-qa="fragment-details-flume-pull-addresses-plus-1"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:input[data-qa="fragment-details-flume-pull-host-1"]'
		And '1' element exists with 'css:input[data-qa="fragment-details-flume-pull-port-1"]'
		And '1' elements exist with 'css:i[data-qa^="fragment-details-flume-pull-addresses-plus"]'
		And '2' elements exist with 'css:i[data-qa^="fragment-details-flume-pull-addresses-minus"]'
		And I wait '1' second
		When I click on the element on index '1'
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
		Then I type 'validFlumeInput' on the element on index '0'
		# Fill in host field
		Given '1' element exists with 'css:input[data-qa="fragment-details-flume-pull-host-0"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		And I type 'localhost' on the element on index '0'
		# Fill in port field
		Given '1' element exists with 'css:input[data-qa="fragment-details-flume-pull-port-0"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		And I type '11999' on the element on index '0'
		# Empty Max batch size
		Given '1' element exists with 'css:input[data-qa="fragment-details-flume-pull-max-batch-size"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		# Empty Parallelism
		Given '1' element exists with 'css:input[data-qa="fragment-details-flume-pull-parallelism"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		
		# Create
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Check that input fragment has been created
		# Retrieve input fragment id using api
		When I send a 'GET' request to '/fragment/input/name/validflumeinput'
		Then the service response status must be '200'.
		And I save element '$.id' in environment variable 'previousFragmentID'
		# Check that an input element has been created
		Then '1' element exists with 'css:span[data-qa="input-context-menu-!{previousFragmentID}"]'
		
		# Try push type
		Given '1' element exists with 'css:button[data-qa="input-filter-new-input"]'
		Then  I click on the element on index '0'
		Then I wait '1' second
		And '1' element exists with 'css:aside[data-qa="fragment-details-modal"]'
		
		# Change value in drop-down menu to push
		Given '1' element exists with 'css:select[data-qa="fragment-details-flume-type"]'
		Then I select 'push' on the element on index '0'
		Then I wait '1' second
		
		# Try with empty Host
		Given '1' element exists with 'css:input[data-qa="fragment-details-flume-push-host"]'
		When I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		Then '1' elements exist with 'css:span[data-qa="fragment-details-flume-push-host-error-required"]'
		
		# Try with port using letters
		Given '1' element exists with 'css:input[data-qa="fragment-details-flume-push-port"]'
		Then I type 'port' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' element exists with 'css:span[data-qa="fragment-details-flume-push-port-error-pattern"]'
		
		# Try with empty Port
		Given '1' element exists with 'css:input[data-qa="fragment-details-flume-push-port"]'
		When I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' element exists with 'css:span[data-qa="fragment-details-flume-push-port-error-required"]'
		
		# Add same input fragment
		# Fill in name field
		Given '1' element exists with 'css:input[data-qa="fragment-detail-name"]'
		Then I type 'validFlumeInput' on the element on index '0'
		# Fill in host field
		Given '1' element exists with 'css:input[data-qa="fragment-details-flume-push-host"]'
		Then I type 'localhost' on the element on index '0'
		# Fill in port field
		Given '1' element exists with 'css:input[data-qa="fragment-details-flume-push-port"]'
		Then I type '11999' on the element on index '0'
		# Select Decompression checkbox
		Given '1' element exists with 'css:label[data-qa="fragment-details-flume-push-enable-decompression"]'
		Then I click on the element on index '0'
		
		# Create
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="error-msg"]'
		And '1' element exists with 'css:span[translate="_ERROR_._100_"]'
		And a text 'There was an error. The name of the fragment already exists!' exists
				
		# Cancel operation
		Given '1' element exists with 'css:button[data-qa="modal-cancel-button"]'
		Then I click on the element on index '0'
		# Check pop up is closed
		And I wait '1' second
		Then '0' element exists with 'css:button[data-qa="modal-cancel-button"]'
		
		# Check close button in modal
		Given '1' element exists with 'css:button[data-qa="input-filter-new-input"]'
		When I click on the element on index '0'
		Then I wait '1' second
		And '1' element exists with 'css:aside[data-qa="fragment-details-modal"]'
		Given '1' element exists with 'css:i[data-qa="modal-cancel-icon"]'
		When I click on the element on index '0'
		Then I wait '1' second
		And '0' element exists with 'css:aside[data-qa="fragment-details-modal"]'		
		
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
		
