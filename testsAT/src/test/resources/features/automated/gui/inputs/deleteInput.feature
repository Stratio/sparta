@web @rest
Feature: Test deleting an input in Sparkta GUI
		
	Background: Setup Sparkta GUI
		Given I set web base url to '${SPARKTA_HOST}:${SPARKTA_PORT}'
		Given I send requests to '${SPARKTA_HOST}:${SPARKTA_API_PORT}'
		
	Scenario: Try to delete an existing input
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| fragmentType | UPDATE | input |
		| name | UPDATE | inputfragment1 |
		| element.type | UPDATE | Flume |
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID'
		When I send a 'GET' request to '/fragment/input'
		Then the service response status must be '200' and its response length must be '1'

		Given I browse to '/#/dashboard/inputs'
		Then I wait '1' second
		Given '1' element exists with 'css:span[data-qa="input-context-menu-!{previousFragmentID}"]'
		Then I click on the element on index '0'
		And I wait '1' second
		Given '1' element exists with 'css:st-menu-element[data-qa="input-context-menu-!{previousFragmentID}-delete"]'
		When I click on the element on index '0'
		Then I wait '1' second
		And '1' element exists with 'css:aside[data-qa="delete-modal"]'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		Then I click on the element on index '0'
		And I wait '1' second
		And '0' element exists with 'css:span[data-qa="input-context-menu-!{previousFragmentID}"]'