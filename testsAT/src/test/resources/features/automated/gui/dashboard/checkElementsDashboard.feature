@web
Feature: Test all expected elements are present in Sparkta GUI
	
	Background: Setup Sparkta GUI
		Given I set web base url to '${SPARKTA_HOST}:${SPARKTA_PORT}'

	Scenario: Check all expected elements are available
		Given I browse to '/#/dashboard'
		Then I wait '2' seconds
		Then '1' elements exists with 'css:a[data-qa="dashboard-menu-inputs"]'
		Then '1' elements exists with 'css:a[data-qa="dashboard-menu-outputs"]'
		Then '1' elements exists with 'css:a[data-qa="dashboard-menu-policies"]'
		
		Given '1' elements exists with 'css:a[data-qa="dashboard-menu-inputs"]'
		When I click on the element on index '0'
		Then I wait '2' seconds
		And we are in page '/#/dashboard/inputs'

		Given I browse to '/#/dashboard'
		Then I wait '2' seconds
		Given '1' elements exists with 'css:a[data-qa="dashboard-menu-outputs"]'
		When I click on the element on index '0'
		Then I wait '2' seconds
		And we are in page '/#/dashboard/outputs'
		
		Given I browse to '/#/dashboard'
		Then I wait '2' seconds
		Given '1' elements exists with 'css:a[data-qa="dashboard-menu-policies"]'
		When I click on the element on index '0'
		Then I wait '2' seconds
		And we are in page '/#/dashboard/policies'
