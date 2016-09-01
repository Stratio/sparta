@rest
Feature: Test all Get operations for policyContexts in Sparta Swagger API

	Background: Setup Sparta REST client
		Given I send requests to '${SPARTA_HOST}:${SPARTA_API_PORT}'
		
	Scenario: Get all policyContexts when none available
		When I send a 'GET' request to '/policyContext'
		Then the service response status must be '200'.
	
	Scenario: Get all policyContexts when one available
		Given I send a 'POST' request to '/policyContext' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | policyContextAvailable |
		| fragments | DELETE | N/A |
		| outputs[1] | DELETE | N/A |
		| id | DELETE | N/A |
		Then the service response status must be '200' and its response must contain the text '"policyName":"policyContextAvailable"'
		And I save element '$.policyId' in environment variable 'previousPolicyID'
		When I send a 'GET' request to '/policyContext'
		Then the service response status must be '200' and its response must contain the text '"id":"!{previousPolicyID}"'
		
	Scenario: Clean up
		When I send a 'DELETE' request to '/policy/!{previousPolicyID}'
		Then the service response status must be '200'.
	 	# Delete fragments
	 	When I send a 'GET' request to '/fragment/input/name/name'
	  	Then the service response status must be '200'.
	  	And I save element '$.id' in environment variable 'previousFragmentID'
	  	When I send a 'DELETE' request to '/fragment/input/!{previousFragmentID}'
	  	Then the service response status must be '200'.
	  	When I send a 'GET' request to '/fragment/output/name/name'
	  	Then the service response status must be '200'.
	  	And I save element '$.id' in environment variable 'previousFragmentID_2'
	  	When I send a 'DELETE' request to '/fragment/output/!{previousFragmentID_2}'
	  	Then the service response status must be '200'.
	  	When I send a 'DELETE' request to '/fragment'
      Then the service response status must be '200'.
