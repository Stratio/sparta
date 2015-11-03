@web @rest
Feature: Test filtering inputs in Sparkta GUI
		
	Background: Setup Sparkta GUI
		Given I set web base url to '${SPARKTA_HOST}:${SPARKTA_PORT}'
		Given I send requests to '${SPARKTA_HOST}:${SPARKTA_API_PORT}'
		
	Scenario: Try to filter existing inputs
		# Create one input fragment
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| fragmentType | UPDATE | input |
		| name | UPDATE | websocketinput |
		| element.type | UPDATE | Websocket |
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID'
		When I send a 'GET' request to '/fragment/input'
		Then the service response status must be '200' and its response length must be '1'
		
		# Create second input fragment
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| fragmentType | UPDATE | input |
		| name | UPDATE | flumeinput |
		| element.type | UPDATE | Flume |
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID_2'
		When I send a 'GET' request to '/fragment/input'
		Then the service response status must be '200' and its response length must be '2'

		Given I browse to '/#/dashboard/inputs'
		Then I wait '1' second
		And '2' element exists with 'css:span[data-qa^="input-context-menu"]'
		
		# Filtering by type
		# Check that new entry has been created in drop down menu
		Given '1' element exists with 'css:select[data-qa="input-filter-type"]'
		And I select 'Websocket (1)' on the element on index '0'
		Then I wait '2' seconds
		And '1' element exists with 'css:span[data-qa^="input-context-menu"]'
		
		Given '1' element exists with 'css:select[data-qa="input-filter-type"]'
		And I select 'Flume (1)' on the element on index '0'
		Then I wait '2' seconds
		Then '1' element exists with 'css:span[data-qa^="input-context-menu"]'
		
		Given '1' element exists with 'css:select[data-qa="input-filter-type"]'
		And I select 'All Types' on the element on index '0'
		Then I wait '2' seconds
		Then '2' element exists with 'css:span[data-qa^="input-context-menu"]'		
		
		# Filtering by name
		# only one matching
		Given '1' element exists with 'css:input[data-qa="input-filter-name"]'
		When I type 'flumeinput' on the element on index '0'
		Then I wait '1' second
		And '1' element exists with 'css:span[data-qa^="input-context-menu"]'
		
		# none matching
		Given '1' element exists with 'css:input[data-qa="input-filter-name"]'
		When I send 'HOME, SHIFT + END, DELETE' on the element on index '0'	
		And I type 'fragment' on the element on index '0'
		Then I wait '1' second
		And '0' element exists with 'css:span[data-qa^="input-context-menu"]'
		
		# both matching
		Given '1' element exists with 'css:input[data-qa="input-filter-name"]'
		When I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		And I type 'input' on the element on index '0'
		Then I wait '1' second
		And '2' element exists with 'css:span[data-qa^="input-context-menu"]'
		
		# Delete flume input and create a second web socket one
		When I send a 'DELETE' request to '/fragment/input/!{previousFragmentID_2}'
		Then the service response status must be '200'.
		When I send a 'GET' request to '/fragment/input'
		Then the service response status must be '200' and its response length must be '1'
		# Create one input fragment
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| fragmentType | UPDATE | input |
		| name | UPDATE | websocketinput2 |
		| element.type | UPDATE | Websocket |
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID_2'
		When I send a 'GET' request to '/fragment/input'
		Then the service response status must be '200' and its response length must be '2'
		
		# Reload page
		Given I browse to '/#/dashboard/inputs'
		Then I wait '1' second
		# Web socket option now must have 2 elements
		Given '1' element exists with 'css:select[data-qa="input-filter-type"]'
		And I select 'Websocket (2)' on the element on index '0'
		Then '2' element exists with 'css:span[data-qa^="input-context-menu"]'
		
		# Delete web socket input and check that number decreases
		When I send a 'DELETE' request to '/fragment/input/!{previousFragmentID_2}'
		Then the service response status must be '200'.
		When I send a 'GET' request to '/fragment/input'
		Then the service response status must be '200' and its response length must be '1'
		# Reload page
		Given I browse to '/#/dashboard/inputs'
		Then I wait '1' second
		Given '1' element exists with 'css:select[data-qa="input-filter-type"]'
		And I select 'Websocket (1)' on the element on index '0'
		Then I wait '2' seconds
		Then '1' element exists with 'css:span[data-qa^="input-context-menu"]'
		
		# Delete everything
		When I send a 'DELETE' request to '/fragment/input/!{previousFragmentID}'
		Then the service response status must be '200'.
		When I send a 'GET' request to '/fragment/input'
		Then the service response status must be '200' and its response must contain the text '[]'