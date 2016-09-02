@rest
Feature: Test all POST operations for policies in Sparta Swagger API

	Background: Setup Sparta REST client
		Given I send requests to '${SPARTA_HOST}:${SPARTA_API_PORT}'

	Scenario: Add a policy with empty data
		Given I send a 'POST' request to '/policy' as 'json'
		Then the service response status must be '400' and its response must contain the text 'Request entity expected but not supplied'
	
	Scenario: Add a valid policy
		When I send a 'POST' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | policy1 |
		| id | DELETE | N/A |
		Then the service response status must be '200'.
		And I save element '$.id' in environment variable 'previousPolicyID'
		# Check that is listed
		When I send a 'GET' request to '/policy/all'	
		Then the service response status must be '200' and its response length must be '1'

	Scenario: Add the same valid policy
		When I send a 'POST' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | policy1 |
		| id | DELETE | N/A |
		Then the service response status must be '404'.
		# Delete previously created policy
		When I send a 'DELETE' request to '/policy/!{previousPolicyID}'
		Then the service response status must be '200'.

	Scenario: Add a policy with non-existing fragment
		When I send a 'POST' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | policyNonExistingFragment |
		| fragments[1] | DELETE | N/A |
		| fragments[0].id | DELETE | N/A |
		| id | DELETE | N/A |
		Then the service response status must be '500'.
		
	Scenario: Add a policy with 2 existing input fragments
		# Create first input fragment
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| name | UPDATE | inputfragment1 |
		| fragmentType | UPDATE | input |
		Then the service response status must be '200'.
		And I save element '$.id' in environment variable 'previousFragmentID'
		# Create second input fragment
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| name | UPDATE | inputfragment2 |
		| fragmentType | UPDATE | input |
		Then the service response status must be '200'.
		And I save element '$.id' in environment variable 'previousFragmentID_2'
		# Create policy referencing these input fragments
		When I send a 'POST' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | policy2InputFragments |
		| fragments[0].id | UPDATE | !{previousFragmentID} |
		| fragments[0].name | UPDATE | inputfragment1 |
		| fragments[0].fragmentType | UPDATE | input |
		| fragments[1].id | UPDATE | !{previousFragmentID_2} |
		| fragments[1].name | UPDATE | inputfragment2 |
		| fragments[1].fragmentType | UPDATE | input |
		| id | DELETE | N/A |
		| input | DELETE | N/A | 
		Then the service response status must be '500' and its response must contain the text 'Only one input is allowed in the policy.'
		# Delete the second fragment that will not be used later on
		When I send a 'DELETE' request to '/fragment/input/!{previousFragmentID_2}'
		Then the service response status must be '200'.
	
	Scenario: Add a policy with existing input fragment
		When I send a 'POST' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | policy1InputFragment |
		| fragments[0].id | UPDATE | !{previousFragmentID} |
		| fragments[0].name | UPDATE | inputfragment1 |
		| fragments[0].fragmentType | UPDATE | input |
		| fragments[1] | DELETE | N/A |
		| id | DELETE | N/A |
		| input | DELETE | N/A |
		Then the service response status must be '200'.
		And I save element '$.id' in environment variable 'previousPolicyID'
		When I send a 'GET' request to '/policy/all'	
		Then the service response status must be '200' and its response length must be '1'
		# Delete policy
		When I send a 'DELETE' request to '/policy/!{previousPolicyID}'
		Then the service response status must be '200'.
	
	Scenario: Add a policy with input and one input fragment
		When I send a 'POST' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | policy1Input1Fragment |
		| fragments[0].id | UPDATE | !{previousFragmentID} |
		| fragments[0].name | UPDATE | inputfragment1 |
		| fragments[0].fragmentType | UPDATE | input |
		| fragments[1] | DELETE | N/A |
		| id | DELETE | N/A |
		Then the service response status must be '500' and its response must contain the text 'Only one input is allowed in the policy.'
		# Delete fragment
		When I send a 'DELETE' request to '/fragment/input/!{previousFragmentID}'
		Then the service response status must be '200'.
		
	Scenario: Add a policy with 2 existing output fragments
		# Create first output fragment
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| name | UPDATE | outputfragment1 |
		| fragmentType | UPDATE | output |
		Then the service response status must be '200'.
		And I save element '$.id' in environment variable 'previousFragmentID'
		# Create second output fragment
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| name | UPDATE | outputfragment2 |
		| fragmentType | UPDATE | output |
		Then the service response status must be '200'.
		And I save element '$.id' in environment variable 'previousFragmentID_2'
		# Create policy using these output fragments
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
		And I save element '$.id' in environment variable 'previousPolicyID'
		# Check list of policies
		When I send a 'GET' request to '/policy/all'	
		Then the service response status must be '200' and its response length must be '1'
		# Delete fragments
		When I send a 'DELETE' request to '/fragment/output/!{previousFragmentID}'
		Then the service response status must be '200'.
		When I send a 'DELETE' request to '/fragment/output/!{previousFragmentID_2}'
		Then the service response status must be '200'.
		
	Scenario: Add a policy with missing input
		When I send a 'POST' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| id | DELETE | N/A |
		| name | UPDATE | policyMissingInput |
		| input | DELETE | N/A |
		Then the service response status must be '500' and its response must contain the text 'It is mandatory to define one input in the policy.'
	
	Scenario: Add a policy with missing outputs
		When I send a 'POST' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| id | DELETE | N/A |
		| name | UPDATE | policyMissingOutputs |
		| outputs | DELETE | N/A |
		Then the service response status must be '404' and its response must contain the text 'The policy needs at least one output'
	
	Scenario: Add a policy with missing name inside cubes
		When I send a 'POST' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| id | DELETE | N/A |
		| name | UPDATE | policyMissingCubesName |
		| cubes[0].name | DELETE | N/A |
		Then the service response status must be '400' and its response must contain the text 'No usable value for name'
	
	# It makes no sense to have such a policy
	# This test will fail Issue: 924
	Scenario: Add a policy with missing dimensions inside cubes
		When I send a 'POST' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| id | DELETE | N/A |
		| name | UPDATE | policyMissingDimensions |
		| cubes[0].dimensions | DELETE | N/A |
		Then the service response status must be '404' and its response must contain the text 'There is at least one cube without dimensions'
	
	# It makes no sense to have such a policy
	# This test will fail Issue: 924
	Scenario: Add a policy with missing operators inside cubes
		When I send a 'POST' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| id | DELETE | N/A |
		| name | UPDATE | policyMissingOperators |
		| cubes[0].operators | DELETE | N/A |
		Then the service response status must be '200'.	
		And I save element '$.id' in environment variable 'previousPolicyID'
		# Delete created policy
		When I send a 'DELETE' request to '/policy/!{previousPolicyID}'
		Then the service response status must be '200'.	
	
	Scenario: Add a policy with missing name
		When I send a 'POST' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| id | DELETE | N/A |
		| name | DELETE | N/A |
		Then the service response status must be '400' and its response must contain the text 'No usable value for name'

	Scenario: Clean everything up
		When I send a 'DELETE' request to '/fragment'
		Then the service response status must be '200'.
		When I send a 'DELETE' request to '/policy'
		Then the service response status must be '200'.
