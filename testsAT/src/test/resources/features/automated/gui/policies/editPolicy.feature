@web @rest
Feature: Test editting a policy in Sparkta GUI

	Background: Setup Sparkta GUI
		Given I set web base url to '${SPARKTA_HOST}:${SPARKTA_PORT}'
		Given I send requests to '${SPARKTA_HOST}:${SPARKTA_API_PORT}'
			
	Scenario: Edit a policy
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
	
		# Browse to policies
		Given I browse to '/#/dashboard/policies'
		Then I wait '2' seconds
		And '1' element exists with 'css:i[data-qa^="policy-context-menu-"]'
		And '1' element exists with 'css:i[data-qa="policy-context-menu-!{previousPolicyID}"]'
		
		# Press menu
		Given I click on the element on index '0'
		Then I wait '1' second
		And '1' element exists with 'css:st-menu-element[data-qa="policy-context-menu-!{previousPolicyID}-edit"]'
		And I click on the element on index '0'
		
		# Modify name and continue
		Given '1' element exists with 'css:input[data-qa="policy-description-name"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		And I type 'myPolicyEdited' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="policy-description-next-button"]'
		When I click on the element on index '0'
		
		# 2/6 Continue
		Given '1' element exists with 'css:button[data-qa="policy-new-next-button"]'
		When I click on the element on index '0'
		
		# 3/6 Delete model, add new one and continue
		# Delete
		Given '1' element exists with 'css:i[data-qa="policy-model-arrow-2"]'
		Then I click on the element on index '0'
		Given '1' element exists with 'css:i[data-qa="policy-model-arrow-1"]'
		Then I click on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="policy-model-delete-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:aside[data-qa="confirm-modal"]'
		And '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		And I click on the element on index '0'		
		# Add
		Given '1' element exists with 'css:input[data-qa="policy-model-name"]'
		Then I type 'myModel2' on the element on index '0'
		Given '1' element exists with 'css:input[data-qa="policy-model-outputs"]'
		Then I type 'myOutput2' on the element on index '0'
		Given '1' element exists with 'css:i[data-qa="policy-model-outputs-add-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:span[data-qa="policy-model-output-list-0"]'
		Given '1' element exists with 'css:button[data-qa="policy-model-add-button"]'
		Then I click on the element on index '0'
		# Continue
		Given '1' element exists with 'css:button[data-qa="policy-model-next-button"]'
		Then I click on the element on index '0'
		
		# 4/6 Delete cube, add new one and continue
		# Delete
		Given '1' element exists with 'css:i[data-qa="policy-cube-arrow-2"]'
		Then I send 'PAGE_UP'
		And I wait '5' seconds
		And I click on the element on index '0'
		Given '1' element exists with 'css:i[data-qa="policy-cube-arrow-1"]'
		Then I click on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="policy-cube-delete-button"]'
		Then I click on the element on index '0'
		And '1' element exists with 'css:aside[data-qa="confirm-modal"]'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		Then I click on the element on index '0'
		# Add Field
		Given '1' element exists with 'css:div[data-qa^="policy-cube-outputlist-"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:input[data-qa="dimension-modal-name"]'
		Given '1' element exists with 'css:input[data-qa="dimension-modal-name"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		And I type 'myDimension2' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa^="policy-cube-dimensionslist-"]'
		# Add Function
		Given '16' elements exist with 'css:div[data-qa^="policy-cube-functionlist-"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:aside[data-qa="operator-modal"]'
		Given '1' element exists with 'css:input[data-qa="operator-modal-name"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Then I type 'myOperator2' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		Then '1' elements exist with 'css:div[data-qa^="policy-cube-operatorlist-"]'
		# Add
		Given '1' element exists with 'css:button[data-qa="policy-cube-add-button"]'
		Then I click on the element on index '0'
		Then '2' elements exist with 'css:i[data-qa^="policy-cube-arrow-"]'
		# Continue
		Given '1' element exists with 'css:button[data-qa="policy-cube-next-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:button[data-qa="policy-outputs-next-button"]'
		
		# 5/6 Continue
		Given '1' element exists with 'css:button[data-qa="policy-outputs-next-button"]'
		Then I click on the element on index '0'
		
		# 6/6 Continue
		Given '1' element exists with 'css:button[data-qa="policy-save-button"]'
		Then I click on the element on index '0'
		
		Given '1' element exists with 'css:aside[data-qa="confirm-modal"]'
		And '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		Then I click on the element on index '0'
		And '1' element exists with 'css:i[data-qa="policy-context-menu-!{previousPolicyID}"]'
		
		Scenario: Delete policy and fragments
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