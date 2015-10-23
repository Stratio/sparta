@web @rest
Feature: Test adding a new CSV output in Sparkta GUI
		
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
		
		# Select CSV
		Given '1' element exists with 'css:label[data-qa="fragment-detail-type-csv"]'
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
		
		# Try with empty path
		Given '1' element exists with 'css:input[data-qa="fragment-details-csv-path"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' element exists with 'css:span[data-qa="fragment-details-csv-path-error-required"]'
		
		# Try with invalid path
		# no csv extension
		Given '1' element exists with 'css:input[data-qa="fragment-details-csv-path"]'
		Then I type 'invalidPath' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' element exists with 'css:span[data-qa="fragment-details-csv-path-error-pattern"]'
		# invalid name
		Given '1' element exists with 'css:input[data-qa="fragment-details-csv-path"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		And I type '9.csv' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' element exists with 'css:span[data-qa="fragment-details-csv-path-error-pattern"]'
		
		# Try empty Delimiter
		Given '1' element exists with 'css:input[data-qa="fragment-details-csv-delimiter"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# NO error message should appear
		Then '0' element exists with 'css:span[data-qa="fragment-details-csv-delimiter-error-required"]'
		
		# Select and deselect Header
		Given '1' element exists with 'css:label[data-qa="fragment-details-csv-header"]'
		Then I click on the element on index '0'
		And I click on the element on index '0'
		
		# Fill in name field
		Given '1' element exists with 'css:input[data-qa="fragment-detail-name"]'
		Then I type 'validCSVOutput' on the element on index '0'
		# Fill in Path field
		Given '1' element exists with 'css:input[data-qa="fragment-details-csv-path"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		And I type 'mypath.csv' on the element on index '0'
		# Fill in Delimiter field
		Given '1' element exists with 'css:input[data-qa="fragment-details-csv-delimiter"]'
		Then I type ',' on the element on index '0'
		
		# Create
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		
		# Check that output fragment has been created
		# Retrieve output fragment id using api
		When I send a 'GET' request to '/fragment/output/name/validcsvoutput'
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID'
		# Check that an output element has been created
		Then '1' element exists with 'css:span[data-qa="output-context-menu-!{previousFragmentID}"]'
		
		# Add same output fragment
		Then '1' element exists with 'css:button[data-qa="outputs-new-button"]'
		When I click on the element on index '0'
		Then I wait '1' second
		And '1' element exists with 'css:aside[data-qa="fragment-details-modal"]'
		# Select CSV
		Given '1' element exists with 'css:label[data-qa="fragment-detail-type-csv"]'
		When I click on the element on index '0'
		Then I wait '1' second
		# Fill in name field
		Given '1' element exists with 'css:input[data-qa="fragment-detail-name"]'
		Then I type 'validCSVOutput' on the element on index '0'
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
		
		
		
		