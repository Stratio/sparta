@web @rest
Feature: Test all expected elements are present in Sparta GUI for outputs

	Background: Setup Sparta GUI
		Given I set web base url to '${SPARTA_HOST}:${SPARTA_PORT}'
		And I send requests to '${SPARTA_HOST}:${SPARTA_API_PORT}'
				
	Scenario: Check all expected elements are available for outputs
		Given I browse to '/#/dashboard/outputs'
		Then I wait '2' seconds
		And '1' element exists with 'css:div[data-qaref="dashboard-menu-inputs"]'
		And '1' element exists with 'css:div[data-qaref="dashboard-menu-outputs"]'
		And '1' element exists with 'css:div[data-qaref="dashboard-menu-policies"]'
		And '1' element exists with 'css:div[data-qa="output-first-message"]'
		
		# Press message and cancel operation
		Given '1' element exists with 'css:div[data-qa="output-first-message"]'
		When I click on the element on index '0'
		Then I wait '2' seconds
		And '1' element exists with 'css:aside[data-qa="fragment-details-modal"]'
		Given '1' element exists with 'css:button[data-qa="modal-cancel-button"]'
		Then I click on the element on index '0'
		And I wait '1' second
		And '0' elements exist with 'css:aside[data-qa="fragment-details-modal"]'
		
		# Press message and close modal
		Given '1' element exists with 'css:div[data-qa="output-first-message"]'
		When I click on the element on index '0'
		Then I wait '2' seconds
		And '1' element exists with 'css:aside[data-qa="fragment-details-modal"]'
		Given '1' element exists with 'css:i[data-qa="modal-cancel-icon"]'
		Then I click on the element on index '0'
		And I wait '1' second
		And '0' elements exist with 'css:aside[data-qa="fragment-details-modal"]'

		# Add output through api and reload
                Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
                | id | DELETE | N/A |
                | fragmentType | UPDATE | output |
                | name | UPDATE | outputfragment1 |
                Then the service response status must be '200'.
                # Retrieve input fragment id using api
                When I send a 'GET' request to '/fragment/output/name/outputfragment1'
                Then the service response status must be '200'.
                And I save element '$.id' in environment variable 'previousFragmentID'
                Given I browse to '/#/dashboard/outputs'
                Then I wait '2' seconds
		
		# Press add button and cancel operation
		Given '1' elements exists with 'css:button[data-qa="output-filter-new-output"]'
		When I click on the element on index '0'
		Then I wait '2' seconds
		And '1' element exists with 'css:aside[data-qa="fragment-details-modal"]'
		Given '1' element exists with 'css:button[data-qa="modal-cancel-button"]'
		Then I click on the element on index '0'
		And I wait '1' second
		And '0' elements exist with 'css:aside[data-qa="fragment-details-modal"]'
		
		# Press add button and close modal
		Given '1' elements exists with 'css:button[data-qa="output-filter-new-output"]'
		When I click on the element on index '0'
		Then I wait '2' seconds
		And '1' element exists with 'css:aside[data-qa="fragment-details-modal"]'
		Given '1' element exists with 'css:i[data-qa="modal-cancel-icon"]'
		Then I click on the element on index '0'
		And I wait '1' second
		And '0' elements exist with 'css:aside[data-qa="fragment-details-modal"]'
		
		# Check that an output element has been created
		Then '1' element exists with 'css:span[data-qa="output-context-menu-!{previousFragmentID}"]'
		And '1' element exists with 'css:select[data-qa="output-filter-type"]'
		And '1' element exists with 'css:input[data-qa="output-filter-name"]'
		And '0' elements exist with 'css:div[data-qa="output-first-message"]'
		
	Scenario: Delete input created
		When I send a 'DELETE' request to '/fragment/output/!{previousFragmentID}'
		Then the service response status must be '200'.
