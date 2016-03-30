@rest
Feature: Test all PUT operations for policies in Sparta Swagger API

	Background: Setup Sparta REST client
		Given I send requests to '${SPARTA_HOST}:${SPARTA_API_PORT}'

	Scenario: Update a policy using empty parameter
		When I send a 'PUT' request to '/policy' as 'json'
		Then the service response status must be '400' and its response must contain the text 'Request entity expected but not supplied'

	Scenario: Update a policy when no policies available
		When I send a 'PUT' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | nonexistingpolicy |
		| fragments | DELETE | N/A |
		| id | UPDATE | nonExistingID |
		Then the service response status must be '404'.
		
	Scenario: Update a non-existing policy when policies available
		# Add a policy
		When I send a 'POST' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | validpolicy |
		| fragments | DELETE | N/A |
		| id | DELETE | N/A |
		Then the service response status must be '200'.
		And I save element '$.id' in environment variable 'previousPolicyID'
		# Check that is listed
		When I send a 'GET' request to '/policy/all'	
		Then the service response status must be '200' and its response length must be '1'
		# Update non existing policy
		When I send a 'PUT' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | nonexistingpolicy |
		| fragments | DELETE | N/A |
		| id | UPDATE | nonExistingID |
		Then the service response status must be '404'.

	# There is no validation
	# This test will fail
	# Issue: 834
	Scenario: Update a existing policy with invalid info: no input
		When I send a 'PUT' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | validpolicy |
		| fragments | DELETE | N/A |
		| id | UPDATE | !{previousPolicyID} |
		| input | DELETE | N/A |
		Then the service response status must be '500' and its response must contain the text 'It is mandatory to define one input in the policy.'
		
	# There is no validation
	# This test will fail
	# Issue: 834
	Scenario: Update a existing policy with invalid info: no outputs
		When I send a 'PUT' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | validpolicy |
		| fragments | DELETE | N/A |
		| id | UPDATE | !{previousPolicyID} |
		| outputs | DELETE | N/A |
		Then the service response status must be '404' and its response must contain the text 'The policy needs at least one output'
	
	# There is no validation
	# This test will fail
	# Issue: 834
	Scenario: Update a existing policy with invalid info: one input and one input fragment
		# Create fragment
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| fragmentType | UPDATE | input |
		| name | UPDATE | inputfragment1 |
		Then the service response status must be '200'.
		And I save element '$.id' in environment variable 'previousFragmentID'
		# Try to update policy
		When I send a 'PUT' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | validpolicy |
		| fragments[0].id | UPDATE | !{previousFragmentID} |
		| fragments[0].name | UPDATE | inputfragment1 |
		| fragments[0].fragmentType | UPDATE | input |
		| fragments[1] | DELETE | N/A |
		| id | UPDATE | !{previousPolicyID} |
		Then the service response status must be '500' and its response must contain the text 'Only one input is allowed in the policy.'
	
	# There is no validation
	# This test will fail
	# Issue: 834
	Scenario: Update a policy with missing name inside cubes
		When I send a 'PUT' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | validpolicy |
		| fragments | DELETE | N/A |
		| id | UPDATE | !{previousPolicyID} |
		| cubes[0].name | DELETE | N/A |
		Then the service response status must be '400' and its response must contain the text 'No usable value for name'
		
	# There is no validation
	# This test will fail
	# Issue: 834
	Scenario: Update a policy with missing dimensions inside cubes
		When I send a 'PUT' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | validpolicy |
		| fragments | DELETE | N/A |
		| id | UPDATE | !{previousPolicyID} |
		| cubes[0].dimensions | DELETE | N/A |
		Then the service response status must be '404' and its response must contain the text 'There is at least one cube without dimensions'
	
	Scenario: Update a policy with missing operators inside cubes
		When I send a 'PUT' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | validpolicy |
		| fragments | DELETE | N/A |
		| id | UPDATE | !{previousPolicyID} |
		| cubes[0].operators | DELETE | N/A |
		Then the service response status must be '200'.
		
	Scenario: Update a existing policy
		When I send a 'PUT' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | newvalidpolicy |
		| fragments | DELETE | N/A |
		| id | UPDATE | !{previousPolicyID} |	
		Then the service response status must be '200'.
	
	Scenario: Clean everything up
		When I send a 'DELETE' request to '/policy/!{previousPolicyID}'
		Then the service response status must be '200'.
		When I send a 'DELETE' request to '/fragment/input/!{previousFragmentID}'
		Then the service response status must be '200'.
