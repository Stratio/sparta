@rest
Feature: Test all DELETE operations for fragments in Sparkta Swagger API

	Background: Setup Sparkta REST client
		Given I send requests to '${SPARKTA_HOST}:${SPARKTA_API_PORT}'

	Scenario: Delete a fragment with type empty and name invalid with empty list
		When I send a 'DELETE' request to '/fragment//invalid'
		Then the service response status must be '405' and its response must contain the text 'HTTP method not allowed, supported methods: GET'
	
	Scenario: Delete a fragment with type input and empty name with empty list
		When I send a 'DELETE' request to '/fragment/input/'
		Then the service response status must be '405' and its response must contain the text 'HTTP method not allowed, supported methods: GET'
	
	Scenario: Delete a fragment with empty type and empty name with empty list
		When I send a 'DELETE' request to '/fragment//'
		Then the service response status must be '405' and its response must contain the text 'HTTP method not allowed, supported methods: GET'
	
	Scenario: Delete a fragment with type invalid and name invalid with empty list
		When I send a 'DELETE' request to '/fragment/invalid/invalid'
		Then the service response status must be '500'.
		
	Scenario: Delete a fragment with type input and name invalid with empty list
		When I send a 'DELETE' request to '/fragment/input/invalid'
		Then the service response status must be '404'.
	
	Scenario: Delete a fragment with type input and name invalid with non-empty list
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| name | UPDATE | inputfragment1 |
		| fragmentType | UPDATE | input |
		When I send a 'DELETE' request to '/fragment/input/invalid'	
		Then the service response status must be '404'.
	
	Scenario: Delete a fragment with type input and name inputfragment1
		When I send a 'GET' request to '/fragment/input/name/inputfragment1'
		Given I save element '$.id' in attribute 'previousFragmentID'
		When I send a 'DELETE' request to '/fragment/input/!{previousFragmentID}'
		Then the service response status must be '200'.
		When I send a 'GET' request to '/fragment/input'
		Then the service response status must be '200' and its response length must be '0'
	
	Scenario: Delete a fragment with type output and name outputfragment1
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| name | UPDATE | outputfragment1 |
		| fragmentType | UPDATE | output |
		And I save element '$.id' in attribute 'previousFragmentID'
		When I send a 'DELETE' request to '/fragment/output/!{previousFragmentID}'	
		Then the service response status must be '200'.
		When I send a 'GET' request to '/fragment/output'
		Then the service response status must be '200' and its response length must be '0'
	
	Scenario: Delete a fragment with type input and name inputfragment1 referenced by policy
		# Create fragment
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| name | UPDATE | inputfragment1 |
		| fragmentType | UPDATE | input |
		# Save fragment id
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID'
		# Create policy referencing previously created fragment
		When I send a 'POST' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| fragments[0].id | UPDATE | !{previousFragmentID} |
		| fragments[0].name | UPDATE | inputfragment1 |
		| fragments[0].fragmentType | UPDATE | input |
		| fragments[1] | DELETE | N/A |
		| id | DELETE | N/A |
		| input | DELETE | N/A |
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousPolicyID'
		# Delete fragment
		When I send a 'DELETE' request to '/fragment/input/!{previousFragmentID}'
		Then the service response status must be '200'.
		# Check that policy has been removed
		When I send a 'GET' request to '/policy/all'
		Then the service response status must be '200' and its response must contain the text '[]'
	
	Scenario: Delete a fragment with type output and name outputfragment1 referenced by policy
		# Create fragment
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| name | UPDATE | outputfragment1 |
		| fragmentType | UPDATE | output |
		# Save fragment id
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID'
		# Create policy referencing previously created fragment
		When I send a 'POST' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| fragments[0].id | UPDATE | !{previousFragmentID} |
		| fragments[0].name | UPDATE | outputfragment1 |
		| fragments[0].fragmentType | UPDATE | output |
		| fragments[1] | DELETE | N/A |
		| id | DELETE | N/A |
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousPolicyID'
		# Delete fragment
		When I send a 'DELETE' request to '/fragment/output/!{previousFragmentID}'
		Then the service response status must be '200'.
		# Check that policy has been removed
		When I send a 'GET' request to '/policy/all'
		Then the service response status must be '200' and its response must contain the text '[]'