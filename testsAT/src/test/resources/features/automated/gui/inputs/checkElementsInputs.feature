@web @rest
Feature: Test all expected elements are present in Sparkta GUI for inputs

	Background: Setup Sparkta GUI
		Given I set web base url to '${SPARKTA_HOST}:${SPARKTA_PORT}'
		And I send requests to '${SPARKTA_HOST}:${SPARKTA_API_PORT}'

	Scenario: Check all expected elements are available for inputs
		Given I browse to '/#/dashboard/inputs'
		Then I wait '2' seconds
		And '1' element exists with 'css:a[data-qa="dashboard-menu-inputs"]'
		And '1' element exists with 'css:a[data-qa="dashboard-menu-outputs"]'
		And '1' element exists with 'css:a[data-qa="dashboard-menu-policies"]'
		And '1' element exists with 'css:button[data-qa="inputs-new-button"]'
		And '1' element exists with 'css:div[data-qa="input-first-message"]'
		
		# Press message and cancel operation
		Given '1' element exists with 'css:div[data-qa="input-first-message"]'
		When I click on the element on index '0'
		Then I wait '2' seconds
		And '1' element exists with 'css:aside[data-qa="fragment-details-modal"]'
		Given '1' element exists with 'css:button[data-qa="modal-cancel-button"]'
		Then I click on the element on index '0'
		And I wait '1' second
		And '0' elements exist with 'css:aside[data-qa="fragment-details-modal"]'
		
		# Press message and close modal
		Given '1' element exists with 'css:div[data-qa="input-first-message"]'
		When I click on the element on index '0'
		Then I wait '2' seconds
		And '1' element exists with 'css:aside[data-qa="fragment-details-modal"]'
		Given '1' element exists with 'css:i[data-qa="modal-cancel-icon"]'
		Then I click on the element on index '0'
		And I wait '1' second
		And '0' elements exist with 'css:aside[data-qa="fragment-details-modal"]'
		
		# Press add button and cancel operation
		Given '1' elements exists with 'css:button[data-qa="inputs-new-button"]'
		When I click on the element on index '0'
		Then I wait '2' seconds
		And '1' element exists with 'css:aside[data-qa="fragment-details-modal"]'
		Given '1' element exists with 'css:button[data-qa="modal-cancel-button"]'
		Then I click on the element on index '0'
		And I wait '1' second
		And '0' elements exist with 'css:aside[data-qa="fragment-details-modal"]'
		
		# Press add button and close modal
		Given '1' elements exists with 'css:button[data-qa="inputs-new-button"]'
		When I click on the element on index '0'
		Then I wait '2' seconds
		And '1' element exists with 'css:aside[data-qa="fragment-details-modal"]'
		Given '1' element exists with 'css:i[data-qa="modal-cancel-icon"]'
		Then I click on the element on index '0'
		And I wait '1' second
		And '0' elements exist with 'css:aside[data-qa="fragment-details-modal"]'
		
		# Press add button and create one input to check filters appear
		Given '1' elements exists with 'css:button[data-qa="inputs-new-button"]'
		When I click on the element on index '0'
		Then I wait '2' seconds
		And '1' element exists with 'css:aside[data-qa="fragment-details-modal"]'
		Given '1' element exists with 'css:input[data-qa="fragment-detail-name"]'
		Then I type 'validFlumeInput' on the element on index '0'
		# Create
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		Then I click on the element on index '0'
		# Check that input fragment has been created
		# Retrieve input fragment id using api
		When I send a 'GET' request to '/fragment/input/name/validflumeinput'
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID'
		# Check that an input element has been created
		Then '1' element exists with 'css:span[data-qa="input-context-menu-!{previousFragmentID}"]'
		And '1' element exists with 'css:select[data-qa="input-filter-type"]'
		And '1' element exists with 'css:input[data-qa="input-filter-name"]'
		And '0' elements exist with 'css:div[data-qa="input-first-message"]'
		
	Scenario: Delete input created
		When I send a 'DELETE' request to '/fragment/input/!{previousFragmentID}'
		Then the service response status must be '200'.