@web @rest
Feature: Test all expected elements are present in Sparta GUI for policies

	Background: Setup Sparta GUI
		Given My app is running in '${SPARTA_HOST}:${SPARTA_PORT}'
		And I send requests to '${SPARTA_HOST}:${SPARTA_API_PORT}'

	Scenario: Check all expected elements are available for policies
		Given I browse to '/#/dashboard/policies'
		Then I wait '2' seconds
		And '1' element exists with 'css:div[data-qaref="dashboard-menu-inputs"]'
		And '1' element exists with 'css:div[data-qaref="dashboard-menu-outputs"]'
		And '1' element exists with 'css:div[data-qaref="dashboard-menu-policies"]'
		And '1' element exists with 'css:div[data-qa="policy-first-message"]'
		
		# Press message and close modal
                Given '1' element exists with 'css:div[data-qa="policy-first-message"]'
                When I click on the element on index '0'
                Then '1' element exists with 'css:i[data-qa="modal-cancel-icon"]'
                Given '1' element exists with 'css:i[data-qa="modal-cancel-icon"]'
                Then I click on the element on index '0'
                And I wait '1' second
                And '0' elements exist with 'css:i[data-qa="modal-cancel-icon"]'
		And '1' element exists with 'css:div[data-qa="policy-first-message"]'

		# Add a valid policy and reload
                When I send a 'POST' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
                | name | UPDATE | policy1 |
                | fragments | DELETE | N/A |
                | id | DELETE | N/A |
                Then the service response status must be '200'
                And I save element '$.id' in environment variable 'previousPolicyID'
		Given I browse to '/#/dashboard/policies'
                Then I wait '2' seconds

		# Press add button and cancel operation
                Given '1' element exists with 'css:button[data-qa="policies-new-policy-button"]'
                When I click on the element on index '0'
                Then '1' element exists with 'css:i[data-qa="modal-cancel-icon"]'
                Given '1' element exists with 'css:i[data-qa="modal-cancel-icon"]'
                Then I click on the element on index '0'
                And I wait '1' second
                And '0' elements exist with 'css:i[data-qa="modal-cancel-icon"]'

	Scenario: Delete policy created
		When I send a 'DELETE' request to '/policy/!{previousPolicyID}'
                Then the service response status must be '200'
