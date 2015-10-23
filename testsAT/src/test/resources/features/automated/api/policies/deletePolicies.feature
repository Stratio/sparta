@rest
Feature: Test all DELETE operations for policies in Sparkta Swagger API

	Background: Setup Sparkta REST client
		Given I send requests to '${SPARKTA_HOST}:${SPARKTA_API_PORT}'
		
	Scenario: Delete a policy when no policies available
		When I send a 'DELETE' request to '/policy/nonExistingId'
		Then the service response status must be '404'.
	
	Scenario: Delete a non-existing policy when policies available
		When I send a 'POST' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | policy1 |
		| fragments | DELETE | N/A |
		| id | DELETE | N/A |
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousPolicyID'
		# Check that is listed
		When I send a 'GET' request to '/policy/all'	
		Then the service response status must be '200' and its response length must be '1'
		When I send a 'DELETE' request to '/policy/nonExistingId'
		Then the service response status must be '404'.
		
	Scenario: Delete a existing policy
		When I send a 'DELETE' request to '/policy/!{previousPolicyID}'
		Then the service response status must be '200'.
		When I send a 'GET' request to '/policy/all'
		Then the service response status must be '200' and its response must contain the text '[]'

	Scenario: Delete a existing policy with fragments
		# Create fragment
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| name | UPDATE | inputfragment1 |
		| fragmentType | UPDATE | input |
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID'
		# Create policy
		When I send a 'POST' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | policy1InputFragment |
		| fragments[0].id | UPDATE | !{previousFragmentID} |
		| fragments[0].name | UPDATE | inputfragment1 |
		| fragments[0].fragmentType | UPDATE | input |
		| fragments[1] | DELETE | N/A |
		| id | DELETE | N/A |
		| input | DELETE | N/A |
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousPolicyID'
		When I send a 'GET' request to '/policy/all'	
		Then the service response status must be '200' and its response length must be '1'
		# Delete policy
		When I send a 'DELETE' request to '/policy/!{previousPolicyID}'
		Then the service response status must be '200'.
		When I send a 'GET' request to '/policy/all'
		Then the service response status must be '200' and its response must contain the text '[]'
		# Delete fragment
		When I send a 'DELETE' request to '/fragment/input/!{previousFragmentID}'
		Then the service response status must be '200'.
	
	Scenario: Delete a policy with empty parameter
		Given I send a 'DELETE' request to '/policy/'
		Then the service response status must be '405' and its response must contain the text 'HTTP method not allowed, supported methods: GET'