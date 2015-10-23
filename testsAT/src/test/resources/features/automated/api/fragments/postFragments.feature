@rest
Feature: Test all POST operations for fragments in Sparkta Swagger API

	Background: Setup Sparkta REST client
		Given I send requests to '${SPARKTA_HOST}:${SPARKTA_API_PORT}'

	Scenario: Add a fragment with empty parameter
		Given I send a 'POST' request to '/fragment' as 'json'
		Then the service response status must be '400' and its response must contain the text 'Request entity expected but not supplied'

	Scenario: Add a fragment with missing name
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| fragmentType | UPDATE | input |
		| name | DELETE | N/A |
		Then the service response status must be '400' and its response must contain the text 'No usable value for name'
	
	Scenario: Add a fragment with missing fragmentType
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| fragmentType | DELETE | N/A |
		Then the service response status must be '400' and its response must contain the text 'No usable value for fragmentType'
		
	Scenario: Add a fragment with missing description
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| description | DELETE | N/A |
		Then the service response status must be '400' and its response must contain the text 'No usable value for description'
	
	Scenario: Add a fragment with missing short description
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| shortDescription | DELETE | N/A |
		Then the service response status must be '400' and its response must contain the text 'No usable value for shortDescription'
			
	Scenario: Add a fragment with missing element
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| element | DELETE | N/A |
		Then the service response status must be '400' and its response must contain the text 'No usable value for element'
	
	Scenario: Add a fragment with incorrect type
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| fragmentType | UPDATE | invalid |
		Then the service response status must be '500' and its response must contain the text 'The fragment type must be input|output'
	
	Scenario: Add a valid input fragment
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| fragmentType | UPDATE | input |
		| name | UPDATE | inputfragment1 |
		Then the service response status must be '200'.
		When I send a 'GET' request to '/fragment/input'
		Then the service response status must be '200' and its response length must be '1'
	
	Scenario: Add a valid output fragment
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| fragmentType | UPDATE | output |
		| name | UPDATE | outputfragment1 |
		Then the service response status must be '200'.
		When I send a 'GET' request to '/fragment/output'
		Then the service response status must be '200' and its response length must be '1'

	Scenario: Clean everything up
		When I send a 'GET' request to '/fragment/input/name/inputfragment1'
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID'
		When I send a 'DELETE' request to '/fragment/input/!{previousFragmentID}'
		Then the service response status must be '200'.
		When I send a 'GET' request to '/fragment/output/name/outputfragment1'
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID'
		When I send a 'DELETE' request to '/fragment/output/!{previousFragmentID}'
		Then the service response status must be '200'.