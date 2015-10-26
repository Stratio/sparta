@web @rest
Feature: Test adding a new Cassandra output in Sparkta GUI
		
	Background: Setup Sparkta GUI
		Given I set web base url to '${SPARKTA_HOST}:${SPARKTA_PORT}'
		Given I send requests to '${SPARKTA_HOST}:${SPARKTA_API_PORT}'
		
	Scenario: Try to add a new input
		Given I browse to '/#/dashboard/outputs'
		Then I wait '1' second
		Then '1' element exists with 'css:button[data-qa="outputs-new-button"]'
		When I click on the element on index '0'
		Then I wait '1' second
		And '1' element exists with 'css:aside[data-qa="fragment-details-modal"]'
		
		# Try with empty name
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' element exists with 'css:span[data-qa="fragment-detail-name-error-required"]'

		# Try name with spaces
		Given '1' element exists with 'css:input[data-qa="fragment-detail-name"]'
		Then I type 'valid Flume Input' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' element exists with 'css:span[data-qa="fragment-detail-name-error-pattern"]'
		Given '1' element exists with 'css:input[data-qa="fragment-detail-name"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		
		# Try with port using letters
		Given '1' element exists with 'css:input[data-qa="fragment-details-cassandra-connectionPort"]'
		Then I type 'port' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' element exists with 'css:span[data-qa="fragment-details-cassandra-connectionPort-error-pattern"]'
		
		# Try with Replication factor using letters
		Given '1' element exists with 'css:input[data-qa="fragment-details-cassandra-replication-factor"]'
		Then I type 'factor' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' element exists with 'css:span[data-qa="fragment-details-cassandra-replication-factor-error-pattern"]'
				
		# Try with Refresh seconds using letters
		Given '1' element exists with 'css:input[data-qa="fragment-details-cassandra-refreshSeconds"]'
		Then I type 'factor' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' element exists with 'css:span[data-qa="fragment-details-cassandra-refreshSeconds-error-pattern"]'
		
		# Try empty refresh seconds	
		Given '1' element exists with 'css:input[data-qa="fragment-details-cassandra-refreshSeconds"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# NO error message should appear
		Then '0' element exists with 'css:span[data-qa="fragment-details-cassandra-refreshSeconds-error-pattern"]'
				
		# Try with empty Contact Point
		Given '1' element exists with 'css:input[data-qa="fragment-details-cassandra-connectionHost"]'
		When I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		# Try with empty Port
		Given '1' element exists with 'css:input[data-qa="fragment-details-cassandra-connectionPort"]'
		When I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		# Try with empty Keyspace
		Given '1' element exists with 'css:input[data-qa="fragment-details-cassandra-keyspace"]'
		When I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		# Try with empty Replication factor
		Given '1' element exists with 'css:input[data-qa="fragment-details-cassandra-replication-factor"]'
		When I send 'HOME, SHIFT + END, DELETE' on the element on index '0'		
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' elements exist with 'css:span[data-qa="fragment-details-cassandra-connectionHost-error-required"]'
		And '1' elements exist with 'css:span[data-qa="fragment-details-cassandra-connectionPort-error-required"]'
		And '1' elements exist with 'css:span[data-qa="fragment-details-cassandra-keyspace-error-required"]'
		And '1' elements exist with 'css:span[data-qa="fragment-details-cassandra-replication-factor-error-required"]'
		
		# Try with invalid port number
		Given '1' element exists with 'css:input[data-qa="fragment-details-cassandra-connectionPort"]'
		Then I type '66666' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Error message should appear
		Then '1' elements exist with 'css:span[data-qa="fragment-details-cassandra-connectionPort-error-pattern"]'
				
		# Try all drop-down options
		# Keyspace class
		Given '1' element exists with 'css:select[data-qa="fragment-details-cassandra-keyspaceClass"]'
		Then I select 'NetworkTopologyStrategy' on the element on index '0'
		And I select 'SimpleStrategy' on the element on index '0'
		# Date format
		Given '1' element exists with 'css:select[data-qa="fragment-details-cassandra-dateFormat"]'
		Then I select 'yyyy-mm-dd HH:mm:ss' on the element on index '0'
		And I select 'yyyy-mm-dd HH:mmZ' on the element on index '0'
		And I select 'yyyy-mm-dd HH:mm:ssZ' on the element on index '0'
		And I select 'yyyy-mm-dd'T'HH:mm' on the element on index '0'
		And I select 'yyyy-mm-dd'T'HH:mmZ' on the element on index '0'
		And I select 'yyyy-mm-dd'T'HH:mm:ss' on the element on index '0'
		And I select 'yyyy-mm-dd'T'HH:mm:ssZ' on the element on index '0'
		And I select 'yyyy-mm-dd' on the element on index '0'
		And I select 'yyyy-mm-ddZ' on the element on index '0'
		And I select 'yyyy-mm-dd HH:mm' on the element on index '0'
		
		# Select and deselect Compact storage
		Given '1' element exists with 'css:label[data-qa="fragment-details-cassandra-compactStorage"]'
		Then I click on the element on index '0'
		And I click on the element on index '0'
		
		# Fill in name field
		Given '1' element exists with 'css:input[data-qa="fragment-detail-name"]'
		Then I type 'validCassandraOutput' on the element on index '0'
		# Fill in Contact point field
		Given '1' element exists with 'css:input[data-qa="fragment-details-cassandra-connectionHost"]'
		Then I type 'localhost' on the element on index '0'
		# Fill in port field
		Given '1' element exists with 'css:input[data-qa="fragment-details-cassandra-connectionPort"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		And I type '9042' on the element on index '0'
		# Fill in Keyspace field
		Given '1' element exists with 'css:input[data-qa="fragment-details-cassandra-keyspace"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		And I type 'myKeyspace' on the element on index '0'
		# Fill in Replication factor field
		Given '1' element exists with 'css:input[data-qa="fragment-details-cassandra-replication-factor"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		And I type '1' on the element on index '0'
		
		# Create
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		
		# Check that output fragment has been created
		# Retrieve output fragment id using api
		When I send a 'GET' request to '/fragment/output/name/validcassandraoutput'
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID'
		# Check that an output element has been created
		Then '1' element exists with 'css:span[data-qa="output-context-menu-!{previousFragmentID}"]'
		
		# Add same output fragment
		Then '1' element exists with 'css:button[data-qa="outputs-new-button"]'
		When I click on the element on index '0'
		Then I wait '1' second
		And '1' element exists with 'css:aside[data-qa="fragment-details-modal"]'
		# Fill in name field
		Given '1' element exists with 'css:input[data-qa="fragment-detail-name"]'
		Then I type 'validCassandraOutput' on the element on index '0'
		# Create
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:span[translate="_INPUT_ERROR_100_"]'
		
		# Cancel operation
		Given '1' element exists with 'css:button[data-qa="modal-cancel-button"]'
		Then I click on the element on index '0'
		# Check pop up is closed
		And I wait '1' second
		Then '0' element exists with 'css:button[data-qa="modal-cancel-button"]'
		
		# Delete output fragment created
		Given '1' element exists with 'css:span[data-qa="output-context-menu-!{previousFragmentID}"]'
		Then I click on the element on index '0'
		And I wait '1' second
		Given '1' element exists with 'css:st-menu-element[data-qa="output-context-menu-!{previousFragmentID}-delete"]'
		When I click on the element on index '0'
		Then I wait '1' second
		And '1' element exists with 'css:aside[data-qa="delete-modal"]'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		Then I click on the element on index '0'
		And I wait '1' second
		And '0' element exists with 'css:span[data-qa="output-context-menu-!{previousFragmentID}"]'