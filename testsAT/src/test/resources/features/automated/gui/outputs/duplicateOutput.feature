@web @rest
Feature: Test duplicating an output in Sparkta GUI
		
	Background: Setup Sparkta GUI
		Given I set web base url to '${SPARKTA_HOST}:${SPARKTA_PORT}'
		Given I send requests to '${SPARKTA_HOST}:${SPARKTA_API_PORT}'
		
	Scenario: Try to duplicate an existing output
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| fragmentType | UPDATE | output |
		| name | UPDATE | outputfragment1 |
		| element.type | UPDATE | Redis |
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID'
		When I send a 'GET' request to '/fragment/output'
		Then the service response status must be '200' and its response length must be '1'

		Given I browse to '/#/dashboard/outputs'
		Then I wait '1' second
		Given '1' element exists with 'css:span[data-qa="output-context-menu-!{previousFragmentID}"]'
		Then I click on the element on index '0'
		And I wait '1' second
		Given '1' element exists with 'css:st-menu-element[data-qa="output-context-menu-!{previousFragmentID}-duplicate"]'
		When I click on the element on index '0'
		Then I wait '1' second
		And '1' element exists with 'css:aside[data-qa="duplicate-modal"]'
		
		# Try with empty name
		Given '1' element exists with 'css:input[data-qa="duplicate-modal-name"]'
		When I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		Then '1' elements exist with 'css:span[data-qa="duplicate-modal-name-error-required"]'
		
		# Try name with spaces
		Given '1' element exists with 'css:input[data-qa="duplicate-modal-name"]'
		Then I type 'valid Redis Output' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' element exists with 'css:span[data-qa="duplicate-modal-name-error-pattern"]'
		Given '1' element exists with 'css:input[data-qa="duplicate-modal-name"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		
		# Try with duplicate name
		Given '1' element exists with 'css:input[data-qa="duplicate-modal-name"]'
		When I type 'outputfragment1' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		Then '1' elements exist with 'css:div[data-qa="error-msg"]'
		And '1' elements exist with 'css:span[translate="_INPUT_ERROR_100_"]'
		
		# Try with valid name
		Given '1' element exists with 'css:input[data-qa="duplicate-modal-name"]'
		When I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		And I type 'outputfragment1bis' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		Then I wait '1' second
		And '0' element exists with 'css:aside[data-qa="duplicate-modal"]'
		
		# Check that input fragment has been created
		# Retrieve input fragment id using api
		When I send a 'GET' request to '/fragment/output/name/outputfragment1bis'
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID_2'
		# Check that an input element has been created
		Then '1' element exists with 'css:span[data-qa="output-context-menu-!{previousFragmentID_2}"]'
		And '1' element exists with 'css:span[data-qa="output-context-menu-!{previousFragmentID}"]'
		
		Scenario: Delete everything
		When I send a 'DELETE' request to '/fragment/output/!{previousFragmentID}'
		Then the service response status must be '200'.
		When I send a 'DELETE' request to '/fragment/output/!{previousFragmentID_2}'
		Then the service response status must be '200'.