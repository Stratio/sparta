@web @rest
Feature: Test adding a new policy in Sparta GUI

	Background: Setup Sparta GUI
		Given I set web base url to '${SPARTA_HOST}:${SPARTA_PORT}'
		Given I send requests to '${SPARTA_HOST}:${SPARTA_API_PORT}'

	Scenario: Add a new policy
		# Create input fragment
        Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
        | id | DELETE | N/A |
        | fragmentType | UPDATE | input |
        | name | UPDATE | flumeinput |
        | element.type | UPDATE | Flume |
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
        Then the service response status must be '200'.
        And I save element '$.id' in environment variable 'previousFragmentID_2'
        When I send a 'GET' request to '/fragment/output'
        Then the service response status must be '200' and its response length must be '1'
		
		# Browse to policies
		Given I browse to '/#/dashboard/policies'
		Then I wait '2' seconds
		
		# Press add message
		Given '1' element exists with 'css:div[data-qa="policy-first-message"]'
		When I click on the element on index '0'
		Then I wait '1' second
        And '1' element exists with 'css:aside[data-qa="policy-creation-modal"]'
		
		# Try with empty Name and Description
		Given '1' element exists with 'css:button[data-qa="policy-description-next-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:span[data-qa="policy-name-error-required"]'
		And '1' element exists with 'css:span[data-qa="policy-description-error-required"]'
		
		# Try with empty Spark streaming window
		Given '1' element exists with 'css:input[data-qa="policy-spark-streaming-window-sparkStreamingWindowNumber"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="policy-description-next-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:span[data-qa="policy-spark-streaming-window-sparkStreamingWindowNumber-error-required"]'

		# Select all values in drop down Spark Streaming Window units
		Given '1' element exists with 'css:select[data-qa="policy-spark-streaming-window-sparkStreamingWindowTime"]'
		Then I select 'Milliseconds' on the element on index '0'
		Then I select 'Minutes' on the element on index '0'
		Then I select 'Hours' on the element on index '0'
		Then I select 'Days' on the element on index '0'
		Then I select 'Seconds' on the element on index '0'
		
		# Try with empty Checkpoint path
		Given '1' element exists with 'css:input[data-qa="policy-checkpoint-path"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="policy-description-next-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:span[data-qa="policy-checkpoint-path-error-required"]'

	   # Try with Max Query Execution Time without units
		Given '1' element exists with 'css:input[data-qa="policy-remember-number"]'
		Then I type '1' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="policy-description-next-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="policy-remember-time-error"]'

	  # Try with empty Max Query Execution Time with units
		Given '1' element exists with 'css:input[data-qa="policy-remember-number"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Given '1' element exists with 'css:select[data-qa="policy-remember-time"]'
		Then I select 'Milliseconds' on the element on index '0'
		Then I select 'Minutes' on the element on index '0'
		Then I select 'Hours' on the element on index '0'
		Then I select 'Days' on the element on index '0'
		Then I select 'Seconds' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="policy-description-next-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:span[data-qa="policy-remember-number-error-required"]'
		Given '1' element exists with 'css:select[data-qa="policy-remember-time"]'
		Then I select 'Please select an option' on the element on index '0'
		
		# Select Persist raw data and deselect
		Given '1' element exists with 'css:label[data-qa="policy-raw-data"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:input[data-qa="raw-data-path"]'
		And '1' element exists with 'css:span[data-qa="raw-data-path-error-required"]'
		Given '1' element exists with 'css:label[data-qa="policy-raw-data"]'
		Then I click on the element on index '0'
		
		# Select all values in drop down Storage level
		Given '1' element exists with 'css:select[data-qa="policy-storage-level"]'
		Then I select 'DISK_ONLY' on the element on index '0'
		Then I select 'DISK_ONLY_2' on the element on index '0'
		Then I select 'MEMORY_ONLY' on the element on index '0'
		Then I select 'MEMORY_ONLY_2' on the element on index '0'
		Then I select 'MEMORY_ONLY_SER' on the element on index '0'
		Then I select 'MEMORY_ONLY_SER_2' on the element on index '0'
		Then I select 'MEMORY_AND_DISK' on the element on index '0'
		Then I select 'MEMORY_AND_DISK_2' on the element on index '0'
		Then I select 'MEMORY_AND_DISK_SER' on the element on index '0'
		Then I select 'MEMORY_AND_DISK_SER_2' on the element on index '0'
		
		# Fill in everything and click Continue
		Given '1' element exists with 'css:input[data-qa="policy-name"]'
		Then I type 'myPolicy' on the element on index '0'
		Given '1' element exists with 'css:input[data-qa="policy-description"]'
		Then I type 'my Policy Description' on the element on index '0'
		Given '1' element exists with 'css:input[data-qa="policy-spark-streaming-window-sparkStreamingWindowNumber"]'
		Then I type '6' on the element on index '0'
		Given '1' element exists with 'css:input[data-qa="policy-checkpoint-path"]'
		Then I type '/tmp/checkpoint' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="policy-description-next-button"]'
		When I click on the element on index '0'
		
		# Next screen (we should have 1 input) Input, try to go to next screen without selecting input
		Given '1' elements exist with 'css:div[data-qa^="policy-input-item"]'
		And '1' element exists with 'css:button[data-qa="policy-next-step-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="error-msg"]'
		
		# Screen 2/6 (1 input available)
		Given '1' element exists with 'css:div[data-qa="policy-input-item-!{previousFragmentID}"]'
		Then I click on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="policy-next-step-button"]'
		Then I click on the element on index '0'
		
		# Screen Transformation
		# Try to go to cubes
		Given '1' element exists with 'css:button[data-qa="policy-next-step-button"]'
		When I click on the element on index '0'
		Then '2' element exists with 'css:div[data-qa="error-msg"]'
		
		# Try ingestion with empty output fields
		Given '1' element exists with 'css:aside[item-qa-tag="policy-model"]'
		Given '1' element exists with 'css:button[data-qa="policy-model-add-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:span[data-qa="policy-model-outputs-error-length"]'
		
		# Add output field
		Given '1' element exists with 'css:input[data-qa="policy-model-outputs"]'
		Then I type 'myOutput2' on the element on index '0'
		Given '1' element exists with 'css:i[data-qa="policy-model-outputs-add-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:i[data-qa="policy-model-output-list-0-remove"]'
	  	And '1' element exists with 'css:select[data-qa="policy-model-output-list-0-type"]'
		
		# Add same output field
		Given '1' element exists with 'css:input[data-qa="policy-model-outputs"]'
		Then I type 'myOutput2' on the element on index '0'
		Given '1' element exists with 'css:i[data-qa="policy-model-outputs-add-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:span[data-qa="policy-model-outputs-error-duplicated"]'
		And '1' element exists with 'css:label[data-qa^="policy-model-output-list-"]'

	    # Add second output field
	  	Given '1' element exists with 'css:input[data-qa="policy-model-outputs"]'
		Then I type 'myOutput' on the element on index '0'
		Given '1' element exists with 'css:i[data-qa="policy-model-outputs-add-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:i[data-qa="policy-model-output-list-1-remove"]'
	  	And '1' element exists with 'css:select[data-qa="policy-model-output-list-1-type"]'
	    And '2' element exists with 'css:label[data-qa^="policy-model-output-list-"]'

	    # Delete first output field
	    Given '1' element exists with 'css:i[data-qa="policy-model-output-list-0-remove"]'
		When I click on the element on index '0'
	    Then '1' element exists with 'css:i[data-qa="policy-model-output-list-0-remove"]'
	  	And '1' element exists with 'css:select[data-qa="policy-model-output-list-0-type"]'
	    And '1' element exists with 'css:label[data-qa^="policy-model-output-list-"]'
		
		# Add transformation
		Given '1' element exists with 'css:button[data-qa="policy-model-add-button"]'
		Then I click on the element on index '0'
		And '1' element exists with 'css:i[data-qa="policy-model-arrow-1"]'
		
		# Delete model
		Given '1' element exists with 'css:i[data-qa="policy-model-arrow-1"]'
		Then I click on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="policy-model-delete-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:aside[data-qa="confirm-modal"]'
		And '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		And I click on the element on index '0'
		
		# Create new transformation (Morphline)
		Given '1' element exists with 'css:button[data-qa="policy-model-add-new-transformation-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:i[data-qa="policy-model-arrow-1"]'
		Given '1' element exists with 'css:label[data-qa="policy-model-type-morphlines"]'
		Then I click on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="policy-model-add-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:span[data-qa="policy-model-outputs-error-length"]'
		
		# Try with empty Configuration
		Given '1' element exists with 'css:textarea[data-qa="policy-model-configuration-textarea"]'
		Then I clear the content on text input at index '0'
	    Given I send 'PAGE_DOWN'
	    And I wait '1' seconds
		Given '1' element exists with 'css:button[data-qa="policy-model-add-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="policy-model-configuration-textarea-error"]'
		
		# Fill Configuration
		Given '1' element exists with 'css:textarea[id="model-configuration"]'
		Then I type '{}' on the element on index '0'
	
		# Add empty output field
		Given '1' element exists with 'css:input[data-qa="policy-model-outputs"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Given '1' element exists with 'css:i[data-qa="policy-model-outputs-add-button"]'
		When I click on the element on index '0'
		Then '0' elements exist with 'css:i[data-qa="policy-model-output-list-0-remove"]'

		# Add output field
		Given '1' element exists with 'css:input[data-qa="policy-model-outputs"]'
		Then I type 'myOutput' on the element on index '0'
		Given '1' element exists with 'css:i[data-qa="policy-model-outputs-add-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:i[data-qa="policy-model-output-list-0-remove"]'
		
		# Add same output field
		Given '1' element exists with 'css:input[data-qa="policy-model-outputs"]'
		Then I type 'myOutput' on the element on index '0'
		Given '1' element exists with 'css:i[data-qa="policy-model-outputs-add-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:span[data-qa="policy-model-outputs-error-duplicated"]'
		And '1' element exists with 'css:label[data-qa^="policy-model-output-list-"]'

		Given '1' element exists with 'css:select[data-qa="policy-description-raw-data-partition-format"]'
		Then I select 'Your raw event' on the element on index '0'
		
		# Add model
		Given I send 'PAGE_DOWN'
	    And I wait '1' seconds
		And '1' element exists with 'css:button[data-qa="policy-model-add-button"]'
		Then I click on the element on index '0'
		
		# Delete model
		Given I send 'PAGE_UP'
	    And I wait '1' seconds
        Given '1' element exists with 'css:i[data-qa="policy-model-arrow-1"]'
        Then I click on the element on index '0'
        Given I send 'PAGE_DOWN'
	    And I wait '1' seconds
        And '1' element exists with 'css:button[data-qa="policy-model-delete-button"]'
        When I click on the element on index '0'
        Then '1' element exists with 'css:aside[data-qa="confirm-modal"]'
        And '1' element exists with 'css:button[data-qa="modal-ok-button"]'
        And I click on the element on index '0'

		# Create new transformation (Geo)
        Given '1' element exists with 'css:button[data-qa="policy-model-add-new-transformation-button"]'
        When I click on the element on index '0'
        Then '1' element exists with 'css:i[data-qa="policy-model-arrow-1"]'
        Given '1' element exists with 'css:label[data-qa="policy-model-type-geo"]'
        Then I click on the element on index '0'
        And I send 'PAGE_DOWN'
	    And I wait '1' seconds
        Given '1' element exists with 'css:button[data-qa="policy-model-add-button"]'
        When I click on the element on index '0'
        Then '1' element exists with 'css:span[data-qa="policy-model-outputs-error-length"]'

        # Try with empty Configuration
        Given '1' element exists with 'css:textarea[data-qa="policy-model-configuration-textarea"]'
        Then I clear the content on text input at index '0'
	    Given I send 'PAGE_DOWN'
	    And I wait '1' seconds
        Given '1' element exists with 'css:button[data-qa="policy-model-add-button"]'
        When I click on the element on index '0'
        Then '1' element exists with 'css:div[data-qa="policy-model-configuration-textarea-error"]'

        # Fill Configuration
        Given '1' element exists with 'css:textarea[id="model-configuration"]'
        Then I type '{ "latitude": "lat", "longitude": "long" }' on the element on index '0'

        # Add empty output field
        Given '1' element exists with 'css:input[data-qa="policy-model-outputs"]'
        Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
        Given '1' element exists with 'css:i[data-qa="policy-model-outputs-add-button"]'
        When I click on the element on index '0'
        Then '0' elements exist with 'css:i[data-qa="policy-model-output-list-0-remove"]'

        # Add output field
        Given '1' element exists with 'css:input[data-qa="policy-model-outputs"]'
        Then I type 'myOutput' on the element on index '0'
		And '0' element exists with 'css:i[data-qa="policy-description-raw-data-partition-format"]'
        Given '1' element exists with 'css:i[data-qa="policy-model-outputs-add-button"]'
        When I click on the element on index '0'
        Then '1' element exists with 'css:i[data-qa="policy-model-output-list-0-remove"]'
		Then '1' element exists with 'css:option[value="string"]'
		Then '0' element exists with 'css:option[value="long"]'
		Then '0' element exists with 'css:option[value="double"]'
		Then '0' element exists with 'css:option[value="integer"]'
		Then '0' element exists with 'css:option[value="boolean"]'
		Then '0' element exists with 'css:option[value="date"]'
		Then '0' element exists with 'css:option[value="datetime"]'
		Then '0' element exists with 'css:option[value="timestamp"]'
		Then '0' element exists with 'css:option[value="arraydouble"]'
		Then '0' element exists with 'css:option[value="arraystring"]'
		Then '0' element exists with 'css:option[value="text"]'

		# Add same output field
        Given '1' element exists with 'css:input[data-qa="policy-model-outputs"]'
        Then I type 'myOutput' on the element on index '0'
        Given '1' element exists with 'css:i[data-qa="policy-model-outputs-add-button"]'
        When I click on the element on index '0'
        Then '1' element exists with 'css:span[data-qa="policy-model-outputs-error-duplicated"]'
        And '1' element exists with 'css:label[data-qa^="policy-model-output-list-"]'

        # Add model
        Given '1' element exists with 'css:button[data-qa="policy-model-add-button"]'
        Then I click on the element on index '0'

        # Delete model
        Given '1' element exists with 'css:i[data-qa="policy-model-arrow-1"]'
        Then I click on the element on index '0'
        And I send 'PAGE_DOWN'
	    And I wait '1' seconds
        Given '1' element exists with 'css:button[data-qa="policy-model-delete-button"]'
        When I click on the element on index '0'
        Then '1' element exists with 'css:aside[data-qa="confirm-modal"]'
        And '1' element exists with 'css:button[data-qa="modal-ok-button"]'
        And I click on the element on index '0'

		# Create new transformation (Date Time)
		Given '1' element exists with 'css:button[data-qa="policy-model-add-new-transformation-button"]'
        When I click on the element on index '0'
        Then '1' element exists with 'css:i[data-qa="policy-model-arrow-1"]'
        Given '1' element exists with 'css:label[data-qa="policy-model-type-datetime"]'
        Then I click on the element on index '0'
        Given '1' element exists with 'css:button[data-qa="policy-model-add-button"]'
        When I click on the element on index '0'
        Then '1' element exists with 'css:span[data-qa="policy-model-outputs-error-length"]'

		# Add empty output field
        Given '1' element exists with 'css:input[data-qa="policy-model-outputs"]'
        Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
        Given '1' element exists with 'css:i[data-qa="policy-model-outputs-add-button"]'
        When I click on the element on index '0'
        Then '0' elements exist with 'css:i[data-qa="policy-model-output-list-0-remove"]'

        # Add output field
        Given '1' element exists with 'css:input[data-qa="policy-model-outputs"]'
        Then I type 'myOutput' on the element on index '0'
        Given '1' element exists with 'css:i[data-qa="policy-model-outputs-add-button"]'
        When I click on the element on index '0'
        Then '1' element exists with 'css:i[data-qa="policy-model-output-list-0-remove"]'

        # Add same output field
        Given '1' element exists with 'css:input[data-qa="policy-model-outputs"]'
        Then I type 'myOutput' on the element on index '0'
        Given '1' element exists with 'css:i[data-qa="policy-model-outputs-add-button"]'
        When I click on the element on index '0'
        Then '1' element exists with 'css:span[data-qa="policy-model-outputs-error-duplicated"]'
        And '1' element exists with 'css:label[data-qa^="policy-model-output-list-"]'

		Given '1' element exists with 'css:select[data-qa="policy-description-raw-data-partition-format"]'
        Then I select 'Your raw event' on the element on index '0'

        # Add model
		Given I send 'PAGE_DOWN'
	    And I wait '1' seconds
        And '1' element exists with 'css:button[data-qa="policy-model-add-button"]'
        When I click on the element on index '0'
		Then '1' element exists with 'css:i[data-qa="policy-model-arrow-1"]'
		
		# Add trigger
		Given '1' element exists with 'css:button[data-qa="policy-model-add-new-trigger-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:input[data-qa="trigger-name"]'
		And I send 'END'
	    And I wait '1' seconds
		# Empty name and empty sql
		Given '1' element exists with 'css:button[data-qa="policy-trigger-add-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:span[data-qa="trigger-name-error-required"]'
		And '1' element exists with 'css:div[data-qa="trigger-sql-error"]'
		# Fill
		Given '1' element exists with 'css:input[data-qa="trigger-name"]'
		Then I type 'triggerTransformation' on the element on index '0'
		Given '1' element exists with 'css:textarea[data-qa="trigger-sql"]'
		Then I type 'select * from stream' on the element on index '0'
		Given '1' element exists with 'css:select[data-qa="trigger-output-select"]'
		Then I select 'printoutput' on the element on index '0' 
		# Save
		Given '1' element exists with 'css:button[data-qa="policy-trigger-add-button"]'
        When I click on the element on index '0'
        Then '1' elements exist with 'css:i[data-qa="policy-trigger-arrow-1"]'

		# Go to cubes
        Given '1' element exists with 'css:button[data-qa="policy-next-step-button"]'
        Then I click on the element on index '0'

		# Screen 4/6 Cubes
		# Try NO CUBES
		Given I send 'END'
	    And I wait '1' seconds
		And '1' element exists with 'css:button[data-qa="policy-save-button"]'
		When I click on the element on index '0'
		And I send 'HOME'
	    And I wait '1' seconds
		Then '1' element exists with 'css:div[data-qa="error-msg"]'
		
		# Try empty Name
		Given '1' element exists with 'css:input[data-qa="cube-name"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Given I send 'END'
	    And I wait '1' seconds
		And '1' element exists with 'css:button[data-qa="policy-cube-add-button"]'
        When I click on the element on index '0'
		Then '1' element exists with 'css:span[data-qa="cube-name-error-required"]'
		And '1' element exists with 'css:div[data-qa="policy-cube-error-msg"]'
		And '1' element exists with 'css:span[data-qa="cube-name-error-required"]'
		
		# Fill in Name
		Given '1' element exists with 'css:input[data-qa="cube-name"]'
		Then I type 'myCube' on the element on index '0'

		# Add output
		Given '1' element exists with 'css:select[data-qa="cube-output-select"]'
		Then I select 'printoutput' on the element on index '0'
		
		# Add Fields
		Given '1' element exists with 'css:div[data-qa^="policy-cube-outputlist-"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="dimension-modal"]'
		# Try with empty name
		Given '1' element exists with 'css:input[data-qa="dimension-modal-name"]'
		When I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Then '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:span[data-qa="dimension-modal-name-error-required"]'
		Given '1' element exists with 'css:input[data-qa="dimension-modal-name"]'
		Then I type 'myDimension' on the element on index '0'
		# Change to DateTime type
		Given '1' element exists with 'css:select[data-qa="dimension-modal-type"]'
		When I select 'DateTime' on the element on index '0'
		Then '1' element exists with 'css:input[data-qa="policy-cube-dimension-dateTime-precisionNumber"]'
		And '1' element exists with 'css:select[data-qa="policy-cube-dimension-dateTime-precisionTime"]'
		# Change to GeoHash type
		Given '1' element exists with 'css:select[data-qa="dimension-modal-type"]'
		When I select 'GeoHash' on the element on index '0'
		Then '1' element exists with 'css:select[data-qa="dimension-modal-precision"]'
		# Change to Default type
		Given '1' element exists with 'css:select[data-qa="dimension-modal-type"]'
		When I select 'Default' on the element on index '0'
		Then '0' element exists with 'css:select[data-qa="dimension-modal-precision"]'
		# Create
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Check one dimension has been added
		Then '1' element exists with 'css:div[data-qa^="policy-cube-dimensionslist-"]'
		# Create a second one with same name
		Given '1' element exists with 'css:div[data-qa^="policy-cube-outputlist-"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="dimension-modal"]'
		Given '1' element exists with 'css:input[data-qa="dimension-modal-name"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		And I type 'myDimension' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="error-msg"]'
		Given '1' element exists with 'css:input[data-qa="dimension-modal-name"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		And I type 'myDimension2' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		Then '2' element exists with 'css:div[data-qa^="policy-cube-dimensionslist-"]'
		# Delete first dimension created
		When I click on the element on index '1'
		Then '1' element exists with 'css:aside[data-qa="confirm-modal"]'
		And '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa^="policy-cube-dimensionslist-"]'
		
		# Add Functions
		Given '17' elements exist with 'css:div[data-qa^="policy-cube-functionlist-"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:aside[data-qa="operator-modal"]'	
		# Try with empty Name
		Given '1' element exists with 'css:input[data-qa="operator-modal-name"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:span[data-qa="operator-modal-name-error-required"]'
		# Fill Name
		Given '1' element exists with 'css:input[data-qa="operator-modal-name"]'
		Then I type 'myOperator' on the element on index '0'
		# Create
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa^="policy-cube-operatorlist-"]'
		# Create a second one with same name
		Given '17' elements exist with 'css:div[data-qa^="policy-cube-functionlist-"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:aside[data-qa="operator-modal"]'
		Given '1' element exists with 'css:input[data-qa="operator-modal-name"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Then I type 'myOperator2' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		Then '2' elements exist with 'css:div[data-qa^="policy-cube-operatorlist-"]'
		# Delete one operator
		Given I click on the element on index '1'
		Then '1' element exists with 'css:aside[data-qa="confirm-modal"]'
		And '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa^="policy-cube-operatorlist-"]'
		
		# Add cube
		Given I send 'END'
	    And I wait '1' seconds
		And '1' element exists with 'css:button[data-qa="policy-cube-add-button"]'
		When I click on the element on index '0'
		Then '1' elements exist with 'css:i[data-qa="policy-cube-arrow-1"]'
		
		## Delete cube
		Given '1' element exists with 'css:i[data-qa="policy-cube-arrow-1"]'
		Then I click on the element on index '0'
		And I send 'END'
	    And I wait '1' seconds
		Given '1' element exists with 'css:button[data-qa="policy-cube-delete-button"]'
		Then I click on the element on index '0'
		And '1' element exists with 'css:aside[data-qa="confirm-modal"]'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		Then I click on the element on index '0'
		
		# Add cube
		Given '1' element exists with 'css:button[data-qa="policy-model-add-new-transformation-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:aside[item-qa-tag="policy-cube"]' 
		# Fill Name
		Given '1' element exists with 'css:input[data-qa="cube-name"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Then I type 'myCube' on the element on index '0'
		# Add output
        Given '1' element exists with 'css:select[data-qa="cube-output-select"]'
        Then I select 'printoutput' on the element on index '0'

		# Add Field
		Given '1' element exists with 'css:div[data-qa^="policy-cube-outputlist-"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="dimension-modal"]'
		Given '1' element exists with 'css:input[data-qa="dimension-modal-name"]'
		When I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Then I type 'myDimension' on the element on index '0'
		# Create
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		# Check one dimension has been added
		Then '1' element exists with 'css:div[data-qa^="policy-cube-dimensionslist-"]'

		# Add Function
		Given '17' elements exist with 'css:div[data-qa^="policy-cube-functionlist-"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:aside[data-qa="operator-modal"]'	
		# Try with empty Name
		Given '1' element exists with 'css:input[data-qa="operator-modal-name"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Then I type 'myOperator' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa^="policy-cube-operatorlist-"]'

		# Add trigger
		Given '1' element exists with 'css:button[data-qa="policy-model-add-new-trigger-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:input[data-qa="trigger-name"]'
		And I send 'PAGE_DOWN'
	    And I wait '1' seconds
		# Empty name and empty sql
		Given '1' element exists with 'css:button[data-qa="policy-trigger-add-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:span[data-qa="trigger-name-error-required"]'
		And '1' element exists with 'css:div[data-qa="trigger-sql-error"]'
		# Fill
		Given '1' element exists with 'css:input[data-qa="trigger-name"]'
		Then I type 'triggerCube' on the element on index '0'
		Given '1' element exists with 'css:textarea[data-qa="trigger-sql"]'
		Then I type 'select * from myCube' on the element on index '0'
		Given '1' element exists with 'css:select[data-qa="trigger-output-select"]'
		Then I select 'printoutput' on the element on index '0'
		# Save
        Given '1' element exists with 'css:button[data-qa="policy-trigger-add-button"]'
        When I click on the element on index '0'
        Then '1' elements exist with 'css:i[data-qa="policy-trigger-arrow-1"]'
		
		# Add cube
		Given I send 'END'
	    And I wait '1' seconds
		And '1' element exists with 'css:button[data-qa="policy-cube-add-button"]'
		Then I click on the element on index '0'
		Then '1' elements exist with 'css:i[data-qa="policy-cube-arrow-1"]'
		
		# Save policy
		Given '1' element exists with 'css:button[data-qa="policy-save-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:aside[data-qa="confirm-modal"]'
        Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
        When I click on the element on index '0'
		Then '1' element exists with 'css:i[data-qa^="policy-context-menu-"]'
		And '1' element exists with 'css:button[data-qa="policies-new-policy-button"]'
		Given I send a 'GET' request to '/policy/findByName/myPolicy'
		Then the service response status must be '200'.
		And I save element '$.id' in environment variable 'previousPolicyID'

		# Screen Policies list
		# Try to add the same policy
		Given '1' element exists with 'css:button[data-qa="policies-new-policy-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:aside[data-qa="policy-creation-modal"]'
		Given '1' element exists with 'css:input[data-qa="policy-name"]'
		Then I type 'myPolicy' on the element on index '0'
		Given '1' element exists with 'css:input[data-qa="policy-description"]'
		Then I type 'my Policy Description' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="policy-description-next-button"]'
        When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="error-msg"]'
		Given '1' element exists with 'css:i[data-qa="modal-cancel-icon"]'
		Then I click on the element on index '0'

	Scenario: Cleanup
		When I send a 'DELETE' request to '/policy/!{previousPolicyID}'
        Then the service response status must be '200'.
		When I send a 'DELETE' request to '/fragment/input/!{previousFragmentID}'
		Then the service response status must be '200'.
		When I send a 'DELETE' request to '/fragment/output/!{previousFragmentID_2}'
        Then the service response status must be '200'.

