@rest
Feature: Test all GET operations for policies in Sparkta Swagger API
	
	Background: Setup Sparkta REST client
		Given I send requests to '${SPARKTA_HOST}:${SPARKTA_API_PORT}'
			
	Scenario: Get all available policies when none available
		When I send a 'GET' request to '/policy/all'
		Then the service response status must be '200' and its response must contain the text '[]'
	
	Scenario: Get a policy by name when none available
		When I send a 'GET' request to '/policy/findByName/nonExistingPolicy'
		Then the service response status must be '404' and its response must contain the text 'No policy with name nonexistingpolicy'
	
	Scenario: Get a policy by id when none available
		When I send a 'GET' request to '/policy/find/nonExistingId'
		Then the service response status must be '404'.
	
	Scenario: Get all policies with a particular fragment when no policies available
		When I send a 'GET' request to '/policy/fragment/input/nonExistingId'
		Then the service response status must be '200' and its response must contain the text '[]'

	Scenario: Run a policy when no policies available
		When I send a 'GET' request to '/policy/run/nonExistingId'
		Then the service response status must be '404'.

	Scenario: Get a non-existing policy by name when policies are available
		When I send a 'POST' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | basicpolicy |
		| fragments | DELETE | N/A |
		| id | DELETE | N/A |
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousPolicyID'
		When I send a 'GET' request to '/policy/findByName/nonExistingPolicy'
		Then the service response status must be '404' and its response must contain the text 'No policy with name nonexistingpolicy'
	
	Scenario: Get a non-existing policy by id
		When I send a 'GET' request to '/policy/find/nonExistingId'
		Then the service response status must be '404'.
	
	Scenario: Get a existing policy by name
		When I send a 'GET' request to '/policy/findByName/basicpolicy'
		Then the service response status must be '200' and its response must contain the text '"id":"!{previousPolicyID}"'
		# Should check that value returned is the expected policy
		
	Scenario: Get a existing policy by id
		When I send a 'GET' request to '/policy/find/!{previousPolicyID}'
		Then the service response status must be '200' and its response must contain the text '"id":"!{previousPolicyID}"'
		# Should check that value returned is the expected policy
		
	Scenario: Run a non-existing policy
		When I send a 'GET' request to '/policy/run/nonExistingId'
		Then the service response status must be '404'.
		
	Scenario: Run a existing policy
		When I send a 'GET' request to '/policy/run/!{previousPolicyID}'
		Then the service response status must be '200' and its response must contain the text '{"message":"Creating new context'
		When I send a 'GET' request to '/policyContext'	
		Then the service response status must be '200' and its response must contain the text '"id":"!{previousPolicyID}"'
		
#	Scenario: Run the same existing policy
#		When I send a 'GET' request to 'policy/run/!{previousPolicyID}'
#		Then the service response status must be '404' and its response must contain the text '{"message":"Creating new context'
#		
	Scenario: Delete policy previously created
		When I send a 'DELETE' request to '/policy/!{previousPolicyID}'
		Then the service response status must be '200'.
	
	Scenario: Get all policies with fragment with incorrect fragment type
		When I send a 'GET' request to '/policy/fragment/invalid/nonExistingId'
		Then the service response status must be '200' and its response must contain the text '[]'
		
	Scenario: Get all policies with non-existing fragment
		When I send a 'GET' request to '/policy/fragment/input/nonExistingId'	
		Then the service response status must be '200' and its response must contain the text '[]'
		
	Scenario: Get all policies with existing fragment
		# Create output fragment 1
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| name | UPDATE | outputfragment1 |
		| fragmentType | UPDATE | output |
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID'
		# Create policy using fragment
		When I send a 'POST' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| fragments[0].id | UPDATE | !{previousFragmentID} |
		| fragments[0].name | UPDATE | outputfragment1 |
		| fragments[0].fragmentType | UPDATE | output |
		| fragments[1] | DELETE | N/A |
		| id | DELETE | N/A |
		| name | UPDATE | policyOneOutputFragment |
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousPolicyID'
		# Get policies with fragmentType input and id !{previousFragmentID}
		When I send a 'GET' request to '/policy/fragment/input/!{previousFragmentID}'
		Then the service response status must be '200' and its response length must be '1'
		
	Scenario: Get all policies with policies available
		When I send a 'GET' request to '/policy/all'	
		Then the service response status must be '200' and its response length must be '1'

	Scenario: Run a policy with 2 existing output fragments
		# Create output fragment 2
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| name | UPDATE | outputfragment2 |
		| fragmentType | UPDATE | output |
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID_2'
		# Create policy using fragments
		When I send a 'POST' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| fragments[0].id | UPDATE | !{previousFragmentID} |
		| fragments[0].name | UPDATE | outputfragment1 |
		| fragments[0].fragmentType | UPDATE | output |
		| fragments[1].id | UPDATE | !{previousFragmentID_2} |
		| fragments[1].name | UPDATE | outputfragment2 |
		| fragments[1].fragmentType | UPDATE | output |
		| id | DELETE | N/A |
		| outputs | DELETE | N/A |
		| name | UPDATE | policyTwoOutputFragment |
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousPolicyID_2'
		# Run policy	
		When I send a 'GET' request to '/policy/run/!{previousPolicyID_2}'
		Then the service response status must be '200' and its response must contain the text '{"message":"Creating new context'
	
	Scenario: Get a policy with empty name
		When I send a 'GET' request to '/policy/findByName/'
		Then the service response status must be '404' and its response must contain the text 'The requested resource could not be found.'

	Scenario: Get a policy with empty id
		When I send a 'GET' request to '/policy/find/'
		Then the service response status must be '404' and its response must contain the text 'The requested resource could not be found.'

	Scenario: Get all policies that contains a fragment with empty type
		When I send a 'GET' request to '/policy/fragment//nonExistingId'
		Then the service response status must be '404' and its response must contain the text 'The requested resource could not be found.'

	Scenario: Get all policies that contains a input fragment with empty id
		When I send a 'GET' request to '/policy/fragment/input/'
		Then the service response status must be '404' and its response must contain the text 'The requested resource could not be found.'

	Scenario: Get all policies that contains a fragment with empty type and id
		When I send a 'GET' request to '/policy/fragment//'
		Then the service response status must be '404' and its response must contain the text 'The requested resource could not be found.'
	
	Scenario: Run an empty policy
		When I send a 'GET' request to '/policy/run/'
		Then the service response status must be '404' and its response must contain the text 'The requested resource could not be found.'
		
	Scenario: Clean everything up
		When I send a 'DELETE' request to '/policy/!{previousPolicyID}'
		Then the service response status must be '200'.
		When I send a 'DELETE' request to '/policy/!{previousPolicyID_2}'
		Then the service response status must be '200'.
		When I send a 'DELETE' request to '/fragment/output/!{previousFragmentID}'
		Then the service response status must be '200'.
		When I send a 'DELETE' request to '/fragment/output/!{previousFragmentID_2}'
		Then the service response status must be '200'.