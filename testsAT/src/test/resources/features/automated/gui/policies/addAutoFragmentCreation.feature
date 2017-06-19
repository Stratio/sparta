@web @rest
Feature: Test adding a new policy in Sparta GUI

  Background: Setup Sparta GUI
    Given My app is running in '${SPARTA_HOST}:${SPARTA_PORT}'
    Given I send requests to '${SPARTA_HOST}:${SPARTA_API_PORT}'

  Scenario: Add a new policy through api
    When I send a 'POST' request to '/policyContext' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | policy1 |
		| fragments | DELETE | N/A |
		| outputs[1] | DELETE | N/A |
		| id | DELETE | N/A |
	Then the service response status must be '200'
	And I save element '$.policyId' in environment variable 'previousPolicyID'
	And I wait '2' seconds
	# Check that is listed
	When I send a 'GET' request to '/policy/all'
	Then the service response status must be '200' and its response length must be '1'
    # Check that input has been created
    When I send a 'GET' request to '/fragment/input'
	Then the service response status must be '200' and its response length must be '1'
    # Check that output has been created
    When I send a 'GET' request to '/fragment/output'
	Then the service response status must be '200' and its response length must be '1'

    # Browse to policies
    Given I browse to '/#/dashboard/policies'
    Then I wait '2' seconds
    And '1' element exists with 'css:i[data-qa^="policy-context-menu-"]'

    # Browse to inputs
    Given I browse to '/#/dashboard/inputs'
    Then I wait '2' seconds
    And '1' element exists with 'css:span[data-qa^="input-context-menu-"]'
    # Try to edit input
    Then I click on the element on index '0'
	And I wait '1' second
	Given '1' element exists with 'css:st-menu-element[data-qa$="-edit"]'
	When I click on the element on index '0'
	Then I wait '1' second
	And '1' element exists with 'css:aside[data-qa="fragment-details-modal"]'
    # Modify
	Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
	When I click on the element on index '0'

    # Browse to outputs
    Given I browse to '/#/dashboard/outputs'
    Then I wait '2' seconds
    And '1' element exists with 'css:span[data-qa^="output-context-menu-"]'
	# Try to edit output
	Then I click on the element on index '0'
	And I wait '1' second
	Given '1' element exists with 'css:st-menu-element[data-qa$="-edit"]'
	When I click on the element on index '0'
	Then I wait '1' second
	And '1' element exists with 'css:aside[data-qa="fragment-details-modal"]'
	# Modify
	Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
	When I click on the element on index '0'

	# Add same policy with different name, fragments should be reused
	 When I send a 'POST' request to '/policyContext' based on 'schemas/policies/policy.conf' as 'json' with:
		| name | UPDATE | policy2 |
		| fragments | DELETE | N/A |
		| outputs[1] | DELETE | N/A |
		| id | DELETE | N/A |
	 Then the service response status must be '200'
	 And I save element '$.policyId' in environment variable 'previousPolicyID_2'

	# Browse to inputs
    Given I browse to '/#/dashboard/inputs'
    Then I wait '2' seconds
    And '1' element exists with 'css:span[data-qa^="input-context-menu-"]'

	# Browse to outputs
    Given I browse to '/#/dashboard/outputs'
    Then I wait '2' seconds
    And '1' element exists with 'css:span[data-qa^="output-context-menu-"]'

    # Delete policy
    When I send a 'DELETE' request to '/policy/!{previousPolicyID}'
	Then the service response status must be '200'
	When I send a 'DELETE' request to '/policy/!{previousPolicyID_2}'
	Then the service response status must be '200'
	When I send a 'GET' request to '/policy/all'
	Then the service response status must be '200' and its response must contain the text '[]'

	# Delete fragments
  	When I send a 'GET' request to '/fragment/input/name/name'
	Then the service response status must be '200'
	And I save element '$.id' in environment variable 'previousFragmentID'
	When I send a 'DELETE' request to '/fragment/input/!{previousFragmentID}'
	Then the service response status must be '200'
	When I send a 'GET' request to '/fragment/output/name/name'
	Then the service response status must be '200'
	And I save element '$.id' in environment variable 'previousFragmentID_2'
	When I send a 'DELETE' request to '/fragment/output/!{previousFragmentID_2}'
	Then the service response status must be '200'
