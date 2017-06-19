@web
Feature: Test all expected elements are present in Sparta GUI
	
	Background: Setup Sparta GUI
		Given My app is running in '${SPARTA_HOST}:${SPARTA_PORT}'

	Scenario: Check all expected elements are available
		Given I browse to '/#/dashboard'
		Then I wait '2' seconds
		Then '1' elements exists with 'css:div[data-qaref="dashboard-menu-inputs"]'
		Then '1' elements exists with 'css:div[data-qaref="dashboard-menu-outputs"]'
		Then '1' elements exists with 'css:div[data-qaref="dashboard-menu-policies"]'
		
		Given '1' elements exists with 'css:div[data-qaref="dashboard-menu-inputs"]'
		When I click on the element on index '0'
		Then I wait '2' seconds
		And we are in page '/#/dashboard/inputs'

		Given I browse to '/#/dashboard'
		Then I wait '2' seconds
		Given '1' elements exists with 'css:div[data-qaref="dashboard-menu-outputs"]'
		When I click on the element on index '0'
		Then I wait '2' seconds
		And we are in page '/#/dashboard/outputs'
		
		Given I browse to '/#/dashboard'
		Then I wait '2' seconds
		Given '1' elements exists with 'css:div[data-qaref="dashboard-menu-policies"]'
		When I click on the element on index '0'
		Then I wait '2' seconds
		And we are in page '/#/dashboard/policies'
