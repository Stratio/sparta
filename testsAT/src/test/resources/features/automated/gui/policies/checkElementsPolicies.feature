@web @rest
Feature: Test all expected elements are present in Sparkta GUI for policies

	Background: Setup Sparkta GUI
		Given I set web base url to '${SPARKTA_HOST}:${SPARKTA_PORT}'
		And I send requests to '${SPARKTA_HOST}:${SPARKTA_API_PORT}'

	Scenario: Check all expected elements are available for policies
		Given I browse to '/#/dashboard/policies'
		Then I wait '2' seconds
		And '1' element exists with 'css:a[data-qa="dashboard-menu-inputs"]'
		And '1' element exists with 'css:a[data-qa="dashboard-menu-outputs"]'
		And '1' element exists with 'css:a[data-qa="dashboard-menu-policies"]'
		And '1' element exists with 'css:div[data-qa="policy-first-message"]'
		And '1' element exists with 'css:button[data-qa="policies-new-button"]'
		
		# Press message and check we are in new page
		Given '1' element exists with 'css:div[data-qa="policy-first-message"]'
		When I click on the element on index '0'
		Then I wait '2' seconds
		And we are in page '/#/dashboard/policies/new'
			
		# Press add button and check we are in new page
		Given '1' element exists with 'css:a[data-qa="dashboard-menu-policies"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:button[data-qa="policies-new-button"]'
		And I click on the element on index '0'
		And I wait '2' seconds
		And we are in page '/#/dashboard/policies/new'
