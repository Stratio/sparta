@web @rest
Feature: Test running a policy in Sparta GUI

	Background: Setup Sparta GUI
		Given I set web base url to '${SPARTA_HOST}:${SPARTA_PORT}'
		Given I send requests to '${SPARTA_HOST}:${SPARTA_API_PORT}'
	
	Scenario: Try to run a policy
		# Create input fragment
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| fragmentType | UPDATE | input |
		| name | UPDATE | websocketinput |
                | element.name | UPDATE | in-WebSocket |
		| element.type | UPDATE | WebSocket |
		| element.configuration.url | ADD | ws://stream.meetup.com/2/rsvps |
		| element.configuration.consumerKey | DELETE | N/A |
		| element.configuration.consumerSecret | DELETE | N/A |
		| element.configuration.accessToken | DELETE | N/A |
		| element.configuration.accessTokenSecret | DELETE | N/A |
		Then the service response status must be '200'.
		And I save element '$.id' in environment variable 'previousFragmentID'
		When I send a 'GET' request to '/fragment/input'
		Then the service response status must be '200' and its response length must be '1'
		
		# Create output fragment	
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| fragmentType | UPDATE | output |
		| name | UPDATE | printoutput |
		| element.type | UPDATE | Print |
		| element.configuration | DELETE | N/A |
		Then the service response status must be '200'.
		And I save element '$.id' in environment variable 'previousFragmentID_2'
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
		| checkpointPath | UPDATE | /tmp/checkpoint |
		| cubes[0].dimensions[0].name | UPDATE | field1 |
		| cubes[0].dimensions[0].field | UPDATE | field1 |
		| cubes[0].dimensions[0].type | UPDATE | Default |
		| cubes[0].dimensions[0].precision | DELETE | N/A |
		| transformations[0].outputFields[0].name | UPDATE | field1 |
		| transformations[0].outputFields[0].type | ADD | string |
		Then the service response status must be '200'.
		And I save element '$.id' in environment variable 'previousPolicyID'
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
		And '1' element exists with 'css:st-menu-element[data-qa="policy-context-menu-!{previousPolicyID}-run"]'
		When I click on the element on index '0'
		And I wait '1' second
		Then a text 'is running!' exists
	    	And I wait '5' seconds
		
		Given '1' element exists with 'css:i[data-qa="policy-context-menu-!{previousPolicyID}"]'
		And I click on the element on index '0'
		And I wait '1' second
		And '1' element exists with 'css:st-menu-element[data-qa="policy-context-menu-!{previousPolicyID}-run"]'
		When I click on the element on index '0'
		Then I wait '1' second
		And a text 'Please stop it and try again later.' exists

	  	# Stop policy
  		Given '1' element exists with 'css:i[data-qa="policy-context-menu-!{previousPolicyID}"]'
		And I click on the element on index '0'
		And I wait '1' second
		And '1' element exists with 'css:st-menu-element[data-qa="policy-context-menu-!{previousPolicyID}-stop"]'
		When I click on the element on index '0'
		Then I wait '1' second
		And a text 'is stopping!' exists

		Scenario: Delete fragments
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
