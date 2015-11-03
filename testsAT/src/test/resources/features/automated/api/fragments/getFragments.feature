@rest
Feature: Test all GET operations for fragments in Sparkta Swagger API
	
	Background: Setup Sparkta REST client
		Given I send requests to '${SPARKTA_HOST}:${SPARKTA_API_PORT}'
		
	Scenario: Get all available fragments with empty type
		When I send a 'GET' request to '/fragment/'
		Then the service response status must be '404' and its response must contain the text 'The requested resource could not be found.'
	
	Scenario: Get all available fragments of type invalid
		When I send a 'GET' request to '/fragment/invalid'	
		Then the service response status must be '500' and its response must contain the text 'The fragment type must be input|output'
	
	Scenario: Get all available fragments of type input with no fragments
		When I send a 'GET' request to '/fragment/input'
		Then the service response status must be '200' and its response must contain the text '[]'
		
	Scenario: Get all available fragments of type output with no fragments
		When I send a 'GET' request to '/fragment/output'
		Then the service response status must be '200' and its response must contain the text '[]'
	
	Scenario: Get all available fragments with empty type and with name name
		When I send a 'GET' request to '/fragment//name'
		Then the service response status must be '404' and its response must contain the text 'The requested resource could not be found.'
	
	Scenario: Get all available fragments of type input with empty name
		When I send a 'GET' request to '/fragment/input/'
		Then the service response status must be '404' and its response must contain the text 'The requested resource could not be found.'
	
	Scenario: Get all available fragments with empty type and with empty name
		When I send a 'GET' request to '/fragment//'
		Then the service response status must be '404' and its response must contain the text 'The requested resource could not be found.'
	
	Scenario: Get all available fragments of type invalid with name name
		When I send a 'GET' request to '/fragment/invalid/name'
		Then the service response status must be '500' and its response must contain the text 'The fragment type must be input|output'
	
	Scenario: Get all available fragments of type input with name name
		When I send a 'GET' request to '/fragment/input/name/name'
		Then the service response status must be '404' and its response must contain the text 'No fragment of type input with name name'
		
	Scenario: Get all available fragments of type output with name name
		When I send a 'GET' request to '/fragment/output/name/name'
		Then the service response status must be '404' and its response must contain the text 'No fragment of type output with name name'
		
	Scenario: Get all available fragments of type input
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| name | UPDATE | inputfragment1 |
		| fragmentType | UPDATE | input |
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID'
		When I send a 'GET' request to '/fragment/input'
		Then the service response status must be '200' and its response length must be '1'
	
	Scenario: Get all available fragments of type output
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| name | UPDATE | outputfragment1 |
		| fragmentType | UPDATE | output |
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID_2'
		When I send a 'GET' request to '/fragment/output'
		Then the service response status must be '200' and its response length must be '1'
		
	Scenario: Get all available fragments of type input with name inputFragment1
		When I send a 'GET' request to '/fragment/input/name/inputfragment1'
		Then the service response status must be '200'.
		
	Scenario: Get all available fragments of type output with name outputFragment1
		When I send a 'GET' request to '/fragment/output/name/outputfragment1'
		Then the service response status must be '200'.
	
	Scenario: Clean everything up
		When I send a 'DELETE' request to '/fragment/input/!{previousFragmentID}'
		Then the service response status must be '200'.
		When I send a 'DELETE' request to '/fragment/output/!{previousFragmentID_2}'
		Then the service response status must be '200'.