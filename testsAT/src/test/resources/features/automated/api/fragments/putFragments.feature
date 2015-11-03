@rest
Feature: Test all PUT operations for fragments in Sparkta Swagger API

	Background: Setup Sparkta REST client
		Given I send requests to '${SPARKTA_HOST}:${SPARKTA_API_PORT}'
		
		Scenario: Update a fragment using empty parameter
		Given I send a 'PUT' request to '/fragment' as 'json'
		Then the service response status must be '400' and its response must contain the text 'Request entity expected but not supplied'

	Scenario: Update a fragment when no fragments available
		Given I send a 'PUT' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| fragmentType | UPDATE | input |
		Then the service response status must be '404'.
		
	Scenario: Update a non-existing fragment
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| fragmentType | UPDATE | input |
		| name | UPDATE | inputfragment1 |
		Then the service response status must be '200'.
		Given I save element '$.id' in attribute 'previousFragmentID'
		Given I send a 'PUT' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| fragmentType | UPDATE | input |
		Then the service response status must be '404'.
	
	Scenario: Update a fragment with missing name
		Given I send a 'PUT' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | UPDATE | !{previousFragmentID} |
		| fragmentType | UPDATE | input |
		| name | DELETE | N/A |
		Then the service response status must be '400' and its response must contain the text 'No usable value for name'
	
	Scenario: Update a fragment with missing fragmentType
		Given I send a 'PUT' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | UPDATE | !{previousFragmentID} |
		| fragmentType | DELETE | N/A |
		| name | DELETE | inputfragment1 |
		Then the service response status must be '400' and its response must contain the text 'No usable value for fragmentType'
		
	Scenario: Update a fragment with missing description
		Given I send a 'PUT' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | UPDATE | !{previousFragmentID} |
		| fragmentType | UPDATE | input |
		| name | UPDATE | inputfragment1 |
		| description | DELETE | N/A |
		Then the service response status must be '400' and its response must contain the text 'No usable value for description'
	
	Scenario: Update a fragment with missing short description
		Given I send a 'PUT' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | UPDATE | !{previousFragmentID} |
		| fragmentType | UPDATE | input |
		| name | UPDATE | inputfragment1 |
		| shortDescription | DELETE | N/A |	
		Then the service response status must be '400' and its response must contain the text 'No usable value for shortDescription'
			
	Scenario: Update a fragment with missing element
		Given I send a 'PUT' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | UPDATE | !{previousFragmentID} |
		| fragmentType | UPDATE | input |
		| name | UPDATE | inputfragment1 |
		| element | DELETE | N/A |		
		Then the service response status must be '400' and its response must contain the text 'No usable value for element'
	
	Scenario: Update a fragment with incorrect type
		Given I send a 'PUT' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | UPDATE | !{previousFragmentID} |
		| fragmentType | UPDATE | invalid |
		| name | UPDATE | inputfragment1 |
		Then the service response status must be '500' and its response must contain the text 'The fragment type must be input|output'	
	
	Scenario: Update a existing fragment
		Given I send a 'PUT' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | UPDATE | !{previousFragmentID} |
		| fragmentType | UPDATE | input |
		| name | UPDATE | inputfragment2 |
		Then the service response status must be '200'.
		
	Scenario: Clean everything up
		When I send a 'DELETE' request to '/fragment/input/!{previousFragmentID}'
		Then the service response status must be '200'.