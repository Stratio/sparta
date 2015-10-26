@web @rest
Feature: Test deleting an output with a policy referencing it in Sparkta GUI
		
	Background: Setup Sparkta GUI
		Given I set web base url to '${SPARKTA_HOST}:${SPARKTA_PORT}'
		Given I send requests to '${SPARKTA_HOST}:${SPARKTA_API_PORT}'
		
	Scenario: Try to delete an existing input
		# Create input fragment
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| fragmentType | UPDATE | input |
		| name | UPDATE | flumeinput |
		| element.type | UPDATE | Flume |
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID'
		When I send a 'GET' request to '/fragment/input'
		Then the service response status must be '200' and its response length must be '1'
		
		# Create output fragment	
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| fragmentType | UPDATE | output |
		| name | UPDATE | printoutput |
		| element.type | UPDATE | Print |
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID_2'
		When I send a 'GET' request to '/fragment/output'
		Then the service response status must be '200' and its response length must be '1'
		
		# Create policy using these fragments
		When I send a 'POST' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| fragments[0].id | UPDATE | !{previousFragmentID} |
		| fragments[0].name | UPDATE | myInputFragment |
		| fragments[0].fragmentType | UPDATE | input |
		| fragments[1].id | UPDATE | !{previousFragmentID_2} |
		| fragments[1].name | UPDATE | myOutputFragment |
		| fragments[1].fragmentType | UPDATE | output |
		| id | DELETE | N/A |
		| input | DELETE | N/A |
		| outputs | DELETE | N/A |
		| name | UPDATE | myPolicy |
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousPolicyID'
		# Check list of policies
		When I send a 'GET' request to '/policy/all'	
		Then the service response status must be '200' and its response length must be '1'

		Given I browse to '/#/dashboard/outputs'
		Then I wait '1' second
		Given '1' element exists with 'css:span[data-qa="output-context-menu-!{previousFragmentID_2}"]'
		Then I click on the element on index '0'
		And I wait '1' second
		Given '1' element exists with 'css:st-menu-element[data-qa="output-context-menu-!{previousFragmentID_2}-delete"]'
		When I click on the element on index '0'
		Then I wait '1' second
		And '1' element exists with 'css:aside[data-qa="delete-modal"]'
		And a text 'This output is being used in the following policies' exists
		And a text 'If you want to delete this output, please change first the output of the policies' exists
		And '1' element exists with 'css:li[data-qa="delete-modal-policy-!{previousPolicyID}"]'
		And '0' element exists with 'css:button[data-qa="modal-ok-button"]'
		
		Given '1' element exists with 'css:button[data-qa="modal-cancel-button"]'
		Then I click on the element on index '0'
		And I wait '1' second
		And '1' element exists with 'css:span[data-qa="output-context-menu-!{previousFragmentID_2}"]'
		
		# Go to Policies dashboard and check that the policy still exists
		Given I browse to '/#/dashboard/policies'
		Then I wait '1' second
		And '1' element exists with 'css:i[data-qa="policy-context-menu-!{previousPolicyID}"]'
		
		Scenario: Delete everything
		When I send a 'DELETE' request to '/policy/!{previousPolicyID}'
		Then the service response status must be '200'.
		When I send a 'GET' request to '/policy/all'
		Then the service response status must be '200' and its response must contain the text '[]'
		When I send a 'DELETE' request to '/fragment/input/!{previousFragmentID}'
		Then the service response status must be '200'.
		When I send a 'GET' request to '/fragment/input'
		Then the service response status must be '200' and its response must contain the text '[]'
		When I send a 'DELETE' request to '/fragment/output/!{previousFragmentID_2}'
		Then the service response status must be '200'.
		When I send a 'GET' request to '/fragment/output'
		Then the service response status must be '200' and its response must contain the text '[]'