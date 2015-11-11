@rest
Feature: Test all Get operations for policyContexts in Sparkta Swagger API

	Background: Setup Sparkta REST client
		Given I send requests to '${SPARKTA_HOST}:${SPARKTA_API_PORT}'
		
	Scenario: Get all policyContexts when none available
		When I send a 'GET' request to '/policyContext'
		Then the service response status must be '200'.
	
	Scenario: Get all policyContexts when one available
		Given I send a 'POST' request to '/policyContext' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | policy1 |
		| fragments | DELETE | N/A |
		| id | DELETE | N/A |
		Then the service response status must be '200' and its response must contain the text '"policyName":"policy1"'
		And I save element '$.policyId' in attribute 'previousPolicyID'
		When I send a 'GET' request to '/policyContext'
		Then the service response status must be '200' and its response must contain the text '"id":"!{previousPolicyID}"'
		
	Scenario: Clean up
		When I send a 'DELETE' request to '/policy/!{previousPolicyID}'
		Then the service response status must be '200'.
		