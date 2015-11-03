@web @rest
Feature: Test filtering outputs in Sparkta GUI
		
	Background: Setup Sparkta GUI
		Given I set web base url to '${SPARKTA_HOST}:${SPARKTA_PORT}'
		Given I send requests to '${SPARKTA_HOST}:${SPARKTA_API_PORT}'
		
	Scenario: Try to filter existing outputs
		# Create one output fragment
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| fragmentType | UPDATE | output |
		| name | UPDATE | redisoutput |
		| element.type | UPDATE | Redis |
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID'
		When I send a 'GET' request to '/fragment/output'
		Then the service response status must be '200' and its response length must be '1'
		
		# Create second output fragment
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| fragmentType | UPDATE | output |
		| name | UPDATE | printoutput |
		| element.type | UPDATE | Print |
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID_2'
		When I send a 'GET' request to '/fragment/output'
		Then the service response status must be '200' and its response length must be '2'

		Given I browse to '/#/dashboard/outputs'
		Then I wait '1' second
		And '2' element exists with 'css:span[data-qa^="output-context-menu"]'
		
		# Filtering by type
		# Check that new entry has been created in drop down menu
		Given '1' element exists with 'css:select[data-qa="output-filter-type"]'
		And I select 'Redis (1)' on the element on index '0'
		Then I wait '2' seconds
		And '1' element exists with 'css:span[data-qa^="output-context-menu"]'
		
		Given '1' element exists with 'css:select[data-qa="output-filter-type"]'
		And I select 'Print (1)' on the element on index '0'
		Then I wait '2' seconds
		Then '1' element exists with 'css:span[data-qa^="output-context-menu"]'
		
		Given '1' element exists with 'css:select[data-qa="output-filter-type"]'
		And I select 'All Types' on the element on index '0'
		Then I wait '2' seconds
		Then '2' element exists with 'css:span[data-qa^="output-context-menu"]'		
		
		# Filtering by name
		# only one matching
		Given '1' element exists with 'css:input[data-qa="output-filter-name"]'
		When I type 'redisoutput' on the element on index '0'
		Then I wait '1' second
		And '1' element exists with 'css:span[data-qa^="output-context-menu"]'
		
		# none matching
		Given '1' element exists with 'css:input[data-qa="output-filter-name"]'
		When I send 'HOME, SHIFT + END, DELETE' on the element on index '0'	
		And I type 'fragment' on the element on index '0'
		Then I wait '1' second
		And '0' element exists with 'css:span[data-qa^="input-context-menu"]'
		
		# both matching
		Given '1' element exists with 'css:input[data-qa="output-filter-name"]'
		When I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		And I type 'output' on the element on index '0'
		Then I wait '1' second
		And '2' element exists with 'css:span[data-qa^="output-context-menu"]'
		
		# Delete print output and create a second redis one
		When I send a 'DELETE' request to '/fragment/output/!{previousFragmentID_2}'
		Then the service response status must be '200'.
		When I send a 'GET' request to '/fragment/output'
		Then the service response status must be '200' and its response length must be '1'
		# Create one output fragment
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| fragmentType | UPDATE | output |
		| name | UPDATE | redisoutput2 |
		| element.type | UPDATE | Redis |
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID_2'
		When I send a 'GET' request to '/fragment/output'
		Then the service response status must be '200' and its response length must be '2'
		
		# Reload page
		Given I browse to '/#/dashboard/outputs'
		Then I wait '1' second
		# Redis option now must have 2 elements
		Given '1' element exists with 'css:select[data-qa="output-filter-type"]'
		And I select 'Redis (2)' on the element on index '0'
		Then '2' element exists with 'css:span[data-qa^="output-context-menu"]'
		
		# Delete redis output and check that number decreases
		When I send a 'DELETE' request to '/fragment/output/!{previousFragmentID_2}'
		Then the service response status must be '200'.
		When I send a 'GET' request to '/fragment/output'
		Then the service response status must be '200' and its response length must be '1'
		# Reload page
		Given I browse to '/#/dashboard/outputs'
		Then I wait '1' second
		Given '1' element exists with 'css:select[data-qa="output-filter-type"]'
		And I select 'Redis (1)' on the element on index '0'
		Then I wait '2' seconds
		Then '1' element exists with 'css:span[data-qa^="output-context-menu"]'
		
		# Delete everything
		When I send a 'DELETE' request to '/fragment/output/!{previousFragmentID}'
		Then the service response status must be '200'.
		When I send a 'GET' request to '/fragment/output'
		Then the service response status must be '200' and its response must contain the text '[]'