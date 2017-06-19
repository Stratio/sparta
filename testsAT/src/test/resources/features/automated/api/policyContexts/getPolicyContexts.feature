@rest
Feature: Test all Get operations for policyContexts in Sparta Swagger API

	Background: Setup Sparta REST client
		Given I send requests to '${SPARTA_HOST}:${SPARTA_API_PORT}'
		
	Scenario: Get all policyContexts when none available
		When I send a 'GET' request to '/policyContext'
		Then the service response status must be '200'
	
	Scenario: Get all policyContexts when one available
		Given I send a 'POST' request to '/policyContext' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | policyContextAvailable |
		| fragments | DELETE | N/A |
		| id | DELETE | N/A |
		Then the service response status must be '200'
		When I send a 'GET' request to '/policyContext'
		Then the service response status must be '200' and its response length must be '1'

	Scenario: Clean up
		When I send a 'DELETE' request to '/fragment'
    	Then the service response status must be '200'
    	When I send a 'DELETE' request to '/policy'
    	Then the service response status must be '200'