@rest
Feature: Test all POST operations for policyContexts in Sparta Swagger API

	Background: Setup Sparta REST client
		Given I send requests to '${SPARTA_HOST}:${SPARTA_API_PORT}'
		Given I send requests to '${SPARTA_HOST}:${SPARTA_API_PORT}'

	Scenario: Add a policyContext with empty parameter
		When I send a 'POST' request to '/policyContext' as 'json'
		Then the service response status must be '400' and its response must contain the text 'Request entity expected but not supplied'
	
	Scenario: Add a policyContext with no name
		Given I send a 'POST' request to '/policyContext' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | DELETE | N/A |
		| fragments | DELETE | N/A |
		| id | DELETE | N/A |
		Then the service response status must be '400' and its response must contain the text 'No usable value for name'
	
	Scenario: Add a policyContext with no input
		Given I send a 'POST' request to '/policyContext' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | policyContextNoInput |
		| fragments | DELETE | N/A |
		| id | DELETE | N/A |
		| input | DELETE | N/A |	
		Then the service response status must be '500' and its response must contain the text 'It is mandatory to define one input in the policy.'
		
	Scenario: Add a policyContext with no outputs
		Given I send a 'POST' request to '/policyContext' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | policyContextNoOutputs |
		| fragments | DELETE | N/A |
		| id | DELETE | N/A |
		| outputs | DELETE | N/A |	
		Then the service response status must be '404' and its response must contain the text 'The policy needs at least one output'
		
	Scenario: Add a policyContext with non-existing fragment
		When I send a 'POST' request to '/policyContext' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | policyContextNonExistingFragment |
		| fragments[1] | DELETE | N/A |
		| fragments[0].id | DELETE | N/A |
		| id | DELETE | N/A |	
		Then the service response status must be '500'
		
	Scenario: Add a policy context with 2 existing input fragments
		# Create first input fragment
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| name | UPDATE | inputfragment1 |
		| fragmentType | UPDATE | input |
		Then the service response status must be '200'
		And I save element '$.id' in environment variable 'previousFragmentID'
		# Create second input fragment
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| name | UPDATE | inputfragment2 |
		| fragmentType | UPDATE | input |
		Then the service response status must be '200'
		And I save element '$.id' in environment variable 'previousFragmentID_2'
		# Create policy referencing these input fragments
		When I send a 'POST' request to '/policyContext' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | policyContext2InputFragments |
		| fragments[0].id | UPDATE | !{previousFragmentID} |
		| fragments[0].name | UPDATE | inputfragment1 |
		| fragments[0].fragmentType | UPDATE | input |
		| fragments[1].id | UPDATE | !{previousFragmentID_2} |
		| fragments[1].name | UPDATE | inputfragment2 |
		| fragments[1].fragmentType | UPDATE | input |
		| id | DELETE | N/A |
		| input | DELETE | N/A |
		Then the service response status must be '500' and its response must contain the text 'Only one input is allowed in the policy.'

	Scenario: Add a policy context with input and one input fragment
		When I send a 'POST' request to '/policyContext' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | policyContext1Input1Fragment |
		| fragments[0].id | UPDATE | !{previousFragmentID} |
		| fragments[0].name | UPDATE | inputfragment1 |
		| fragments[0].fragmentType | UPDATE | input |
		| fragments[1] | DELETE | N/A |
		| id | DELETE | N/A |
		Then the service response status must be '500' and its response must contain the text 'Only one input is allowed in the policy.'

	Scenario: Add a policyContext
		When I send a 'POST' request to '/policyContext' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | policyContextValid |
		| fragments | DELETE | N/A |
		| id | DELETE | N/A |
		Then the service response status must be '200'
		And I save element '$.policyId' in environment variable 'previousPolicyID'
		# One policyContext created
		When I send a 'GET' request to '/policyContext'
		Then the service response status must be '200' and its response must contain the text '"id":"!{previousPolicyID}"'
		# One policy created
		When I send a 'GET' request to '/policy/all'
		Then the service response status must be '200' and its response length must be '1'

	Scenario: Add same policyContext
		When I send a 'POST' request to '/policyContext' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | policyContextValid |
		| fragments | DELETE | N/A |
		| id | DELETE | N/A |
		Then the service response status must be '500'

	Scenario: Add a policyContext with existing fragment
		When I send a 'POST' request to '/policyContext' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | policyContext1InputFragment |
		| fragments[0].id | UPDATE | !{previousFragmentID} |
		| fragments[0].name | UPDATE | inputfragment1 |
		| fragments[0].fragmentType | UPDATE | input |
		| fragments[1] | DELETE | N/A |
		| id | DELETE | N/A |
		| input | DELETE | N/A |
		Then the service response status must be '200' and its response must contain the text '"policyName":"policyContext1InputFragment"'
		# One policy created
		When I send a 'GET' request to '/policy/all'
		Then the service response status must be '200' and its response length must be '2'
	 	# Delete fragments
		When I send a 'DELETE' request to '/fragment'
		Then the service response status must be '200'
		When I send a 'DELETE' request to '/policy'
		Then the service response status must be '200'

	Scenario: Add a policy context with 2 existing output fragments
	  # Create first output fragment
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
			| id | DELETE | N/A |
			| name | UPDATE | outputfragment1 |
			| fragmentType | UPDATE | output |
		Then the service response status must be '200'
		And I save element '$.id' in environment variable 'previousFragmentID'
	  # Create second output fragment
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
			| id | DELETE | N/A |
			| name | UPDATE | outputfragment2 |
			| fragmentType | UPDATE | output |
		Then the service response status must be '200'
		And I save element '$.id' in environment variable 'previousFragmentID_2'
	  # Create policy using these output fragments
		When I send a 'POST' request to '/policyContext' based on 'schemas/policies/policy.conf' as 'json' with:
			| fragments[0].id | UPDATE | !{previousFragmentID} |
			| fragments[0].name | UPDATE | outputfragment1 |
			| fragments[0].fragmentType | UPDATE | output |
			| fragments[1].id | UPDATE | !{previousFragmentID_2} |
			| fragments[1].name | UPDATE | outputfragment2 |
			| fragments[1].fragmentType | UPDATE | output |
			| id | DELETE | N/A |
			| outputs | DELETE | N/A |
			| name | UPDATE | policyContextTwoOutputFragment |
	  # One policy created
		When I send a 'GET' request to '/policy/all'
		Then the service response status must be '200' and its response length must be '2'
		 # Delete fragments
		When I send a 'DELETE' request to '/fragment'
		Then the service response status must be '200'
		When I send a 'DELETE' request to '/policy'
		Then the service response status must be '200'

	Scenario: Clean up
		When I send a 'DELETE' request to '/fragment'
		Then the service response status must be '200'
		When I send a 'DELETE' request to '/policy'
		Then the service response status must be '200'
