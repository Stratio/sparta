@web @rest
Feature: Test adding a new policy in Sparkta GUI

	Background: Setup Sparkta GUI
		Given I set web base url to '${SPARKTA_HOST}:${SPARKTA_PORT}'
		Given I send requests to '${SPARKTA_HOST}:${SPARKTA_API_PORT}'
	
	Scenario: Add a new policy
		# Browse to policies
		Given I browse to '/#/dashboard/policies'
		Then I wait '2' seconds
		
		# Press add message
		Given '1' element exists with 'css:div[data-qa="policy-first-message"]'
		When I click on the element on index '0'
		Then I wait '2' seconds
		And we are in page '/#/dashboard/policies/new'
		
		# Try with empty Name and Description
		Given '1' element exists with 'css:button[data-qa="policy-description-next-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:span[data-qa="policy-description-name-error-required"]'
		And '1' element exists with 'css:span[data-qa="policy-description-description-error-required"]'
		
		# Try with empty Spark streaming window
		Given '1' element exists with 'css:input[data-qa="policy-description-spark-streaming-window"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="policy-description-next-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:span[data-qa="policy-description-spark-streaming-window-error-required"]'
		
		# Try with empty Checkpoint path
		Given '1' element exists with 'css:input[data-qa="policy-description-checkpoint-path"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="policy-description-next-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:span[data-qa="policy-description-checkpoint-path-error-required"]'
		
		# Select Persist raw data and deselect
		Given '1' element exists with 'css:label[data-qa="policy-description-persist-raw-data"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:input[data-qa="policy-description-raw-data-path"]'
		And '1' element exists with 'css:span[data-qa="policy-description-raw-data-path-error-required"]'
		And '1' element exists with 'css:select[data-qa="policy-description-raw-data-partition-format"]'
		And I select 'year' on the element on index '0'
		And I select 'month' on the element on index '0'
		And I select 'day' on the element on index '0'
		And I select 'hour' on the element on index '0'
		And I select 'minute' on the element on index '0'		
		Given '1' element exists with 'css:label[data-qa="policy-description-persist-raw-data"]'
		Then I click on the element on index '0'
		
		# Select all values in drop down Storage level
		Given '1' element exists with 'css:select[data-qa="policy-description-storage-level-data"]'
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
		Given '1' element exists with 'css:input[data-qa="policy-description-name"]'
		Then I type 'myPolicy' on the element on index '0'
		Given '1' element exists with 'css:input[data-qa="policy-description-description"]'
		Then I type 'my Policy Description' on the element on index '0'
		Given '1' element exists with 'css:input[data-qa="policy-description-spark-streaming-window"]'
		Then I type '6000' on the element on index '0'
		Given '1' element exists with 'css:input[data-qa="policy-description-checkpoint-path"]'
		Then I type '/tmp/checkpoint' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="policy-description-next-button"]'
		When I click on the element on index '0'
		
		# Next screen (we should have no inputs) 2/6 Input
		Given '0' elements exist with 'css:div[data-qa^="policy-input-item"]'
		And '1' element exists with 'css:button[data-qa="policy-new-next-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa^="error-msg"]'
		
		# Go back to screen 1/6 Description
		Given '1' element exist with 'css:button[data-qa="policy-cube-previous-button"]'
		Then I click on the element on index '0'
		
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
		
		# Screen 2/6 (1 input available)
		Given '1' element exists with 'css:button[data-qa="policy-description-next-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa^="policy-input-item"]'
		
		Given '1' element exists with 'css:button[data-qa="policy-new-next-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="error-msg"]'
		
		Given '1' element exists with 'css:div[data-qa="policy-input-item-!{previousFragmentID}"]'
		Then I click on the element on index '0'
		
		Given '1' element exists with 'css:button[data-qa="policy-new-next-button"]'
		Then I click on the element on index '0'
		
		# Screen 3/6 Model
		Given '1' element exists with 'css:button[data-qa="policy-model-add-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="policy-model-error-msg"]'
		
		
		Given '1' element exists with 'css:input[data-qa="policy-model-name"]'
		Then I type 'myModel' on the element on index '0'
		
		# Try with empty Configuration
		Given '1' element exists with 'css:textarea[data-qa="policy-model-configuration-textarea"]'
		Then I clear the content on text input at index '0'
		Given '1' element exists with 'css:button[data-qa="policy-model-add-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="policy-model-error-msg"]'
		
		
		# Fill Configuration
		Given '1' element exists with 'css:textarea[data-qa="policy-model-configuration-textarea"]'
		Then I type '{}' on the element on index '0'
	
		# Add empty output field
		Given '1' element exists with 'css:i[data-qa="policy-model-outputs-add-button"]'
		When I click on the element on index '0'
		Then '0' elements exist with 'css:span[data-qa="policy-model-output-list-0"]'
		# Add output field
		Given '1' element exists with 'css:input[data-qa="policy-model-outputs"]'
		Then I type 'myOutput' on the element on index '0'
		Given '1' element exists with 'css:i[data-qa="policy-model-outputs-add-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:span[data-qa="policy-model-output-list-0"]'
		
		# Add same output field
		Given '1' element exists with 'css:input[data-qa="policy-model-outputs"]'
		Then I type 'myOutput' on the element on index '0'
		Given '1' element exists with 'css:i[data-qa="policy-model-outputs-add-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="policy-modal-error-msg-outputs"]'
		And '1' element exists with 'css:span[data-qa^="policy-model-output-list-"]'
		
		# Add model (Morphline)
		Given '1' element exists with 'css:button[data-qa="policy-model-add-button"]'
		Then I click on the element on index '0'
		
		# Delete model
		Given '1' element exists with 'css:i[data-qa="policy-model-arrow-2"]'
		Then I send 'PAGE_UP'
		Then I click on the element on index '0'
		
		Given '1' element exists with 'css:i[data-qa="policy-model-arrow-1"]'
		Then I click on the element on index '0'
		
		Given '1' element exists with 'css:button[data-qa="policy-model-delete-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:aside[data-qa="confirm-modal"]'
		
		And '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		And I click on the element on index '0'
		
		# Fill in model (Datetime)
		Given '1' element exists with 'css:label[data-qa="policy-model-type-datetime"]'
		Then I click on the element on index '0'
		
		Given '1' element exists with 'css:button[data-qa="policy-model-add-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="policy-model-error-msg"]'
		
		
		Given '1' element exists with 'css:input[data-qa="policy-model-name"]'
		Then I type 'myModel' on the element on index '0'		
		
		# Try with empty Configuration
		Given '1' element exists with 'css:textarea[data-qa="policy-model-configuration-textarea"]'
		Then I clear the content on text input at index '0'
		Given '1' element exists with 'css:button[data-qa="policy-model-add-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="policy-model-error-msg"]'
		
		# Fill Configuration
		Given '1' element exists with 'css:textarea[data-qa="policy-model-configuration-textarea"]'
		Then I type '{}' on the element on index '0'
	
		# Add empty output field
		Given '1' element exists with 'css:input[data-qa="policy-model-outputs"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Given '1' element exists with 'css:i[data-qa="policy-model-outputs-add-button"]'
		When I click on the element on index '0'
		
		Then '0' elements exist with 'css:span[data-qa="policy-model-output-list-0"]'
		# Add output field
		Given '1' element exists with 'css:input[data-qa="policy-model-outputs"]'
		Then I type 'myOutput' on the element on index '0'
		Given '1' element exists with 'css:i[data-qa="policy-model-outputs-add-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:span[data-qa="policy-model-output-list-0"]'
		
		# Add same output field
		Given '1' element exists with 'css:input[data-qa="policy-model-outputs"]'
		Then I type 'myOutput' on the element on index '0'
		Given '1' element exists with 'css:i[data-qa="policy-model-outputs-add-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="policy-modal-error-msg-outputs"]'
		And '1' element exists with 'css:span[data-qa^="policy-model-output-list-"]'
		
		# Add model
		Given '1' element exists with 'css:button[data-qa="policy-model-add-button"]'
		Then I click on the element on index '0'
		
		# Delete model
		Given '1' element exists with 'css:i[data-qa="policy-model-arrow-2"]'
		Then I click on the element on index '0'
		Given '1' element exists with 'css:i[data-qa="policy-model-arrow-1"]'
		Then I click on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="policy-model-delete-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:aside[data-qa="confirm-modal"]'
		And '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		And I click on the element on index '0'
		
		# Fill in model (Type)
		Given '1' element exists with 'css:label[data-qa="policy-model-type-type"]'
		Then I click on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="policy-model-add-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="policy-model-error-msg"]'
		
		Given '1' element exists with 'css:input[data-qa="policy-model-name"]'
		Then I type 'myModel' on the element on index '0'
		
		# Try with empty Configuration
		Given '1' element exists with 'css:textarea[data-qa="policy-model-configuration-textarea"]'
		Then I clear the content on text input at index '0'
		Given '1' element exists with 'css:button[data-qa="policy-model-add-button"]'
		When I click on the element on index '0'
		#Then '1' element exists with 'css:div[data-qa="policy-modal-error-msg-accordion"]'
		Then '1' element exists with 'css:div[data-qa="policy-model-error-msg"]'
		
		# Fill Configuration
		Given '1' element exists with 'css:textarea[data-qa="policy-model-configuration-textarea"]'
		Then I type '{}' on the element on index '0'
	
		# Add empty output field
		Given '1' element exists with 'css:input[data-qa="policy-model-outputs"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Given '1' element exists with 'css:i[data-qa="policy-model-outputs-add-button"]'
		When I click on the element on index '0'
		Then '0' elements exists with 'css:span[data-qa="policy-model-output-list-0"]'
		# Add output field
		Given '1' element exists with 'css:input[data-qa="policy-model-outputs"]'
		Then I type 'myOutput' on the element on index '0'
		Given '1' element exists with 'css:i[data-qa="policy-model-outputs-add-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:span[data-qa="policy-model-output-list-0"]'
		
		# Add same output field
		Given '1' element exists with 'css:input[data-qa="policy-model-outputs"]'
		Then I type 'myOutput' on the element on index '0'
		Given '1' element exists with 'css:i[data-qa="policy-model-outputs-add-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="policy-modal-error-msg-outputs"]'
		And '1' element exists with 'css:span[data-qa^="policy-model-output-list-"]'
		
		# Add model
		Given '1' element exists with 'css:button[data-qa="policy-model-add-button"]'
		Then I click on the element on index '0'
		
		# Continue
		Given '1' element exists with 'css:button[data-qa="policy-model-next-button"]'
		Then I click on the element on index '0'
		
		# Screen 4/6 Cubes
		# Try NO CUBES
		Given '1' element exists with 'css:button[data-qa="policy-cube-next-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="policy-cube-accordion-error-msg"]'
		
		# Try empty Name
		Given '1' element exists with 'css:button[data-qa="policy-cube-add-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="policy-cube-error-msg"]'
		
		
		# Fill in Name
		Given '1' element exists with 'css:input[data-qa="policy-cube-name"]'
		Then I type 'myCube' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="policy-cube-add-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="policy-cube-error-msg"]'
		
		
		# Try empty Time dimension
		Given '1' element exists with 'css:input[data-qa="policy-cube-checkpoint-time-dimension"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="policy-cube-add-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="policy-cube-error-msg"]'
		# Fill value
		Given '1' element exists with 'css:input[data-qa="policy-cube-checkpoint-time-dimension"]'
		Then I type 'minute' on the element on index '0'
				
		# Try all values in drop down menu
		Given '1' element exists with 'css:select[data-qa="policy-cube-checkpoint-granularity"]'
		Then I select '5 seconds' on the element on index '0'
		And I select '10 seconds' on the element on index '0'
		And I select '15 seconds' on the element on index '0'
		And I select 'Minute' on the element on index '0'
		And I select 'Hour' on the element on index '0'
		And I select 'Day' on the element on index '0'
		And I select 'Month' on the element on index '0'
		And I select 'Year' on the element on index '0'
		
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
		Then '1' element exists with 'css:select[data-qa="dimension-modal-precision"]'
		And '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="error-msg"]'
		# Change to GeoHash type
		Given '1' element exists with 'css:select[data-qa="dimension-modal-type"]'
		When I select 'GeoHash' on the element on index '0'
		Then '1' element exists with 'css:select[data-qa="dimension-modal-precision"]'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="error-msg"]'
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
		Given '16' elements exist with 'css:div[data-qa^="policy-cube-functionlist-"]'
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
		# Try with empty config
		Given '1' element exists with 'css:textarea[data-qa="operator-modal-config"]'
		Then I clear the content on text input at index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="error-msg"]'
		# Fill Config
		Given '1' element exists with 'css:textarea[data-qa="operator-modal-config"]'
		Then I type '{}' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa^="policy-cube-operatorlist-"]'
		# Create a second one with same name
		Given '16' elements exist with 'css:div[data-qa^="policy-cube-functionlist-"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:aside[data-qa="operator-modal"]'
		Given '1' element exists with 'css:input[data-qa="operator-modal-name"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Then I type 'myOperator' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="error-msg"]'
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
		Given '1' element exists with 'css:button[data-qa="policy-cube-add-button"]'
		Then I click on the element on index '0'
		Then '2' elements exist with 'css:i[data-qa^="policy-cube-arrow-"]'
		
		## Delete cube
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
		
		# Add cube
		# Fill Name
		Given '1' element exists with 'css:input[data-qa="policy-cube-name"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Then I type 'myCube' on the element on index '0'

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
		Given '16' elements exist with 'css:div[data-qa^="policy-cube-functionlist-"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:aside[data-qa="operator-modal"]'	
		# Try with empty Name
		Given '1' element exists with 'css:input[data-qa="operator-modal-name"]'
		Then I send 'HOME, SHIFT + END, DELETE' on the element on index '0'
		Then I type 'myOperator' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa^="policy-cube-operatorlist-"]'
		
		# Add cube
		Given '1' element exists with 'css:button[data-qa="policy-cube-add-button"]'
		Then I click on the element on index '0'
		Then '2' elements exist with 'css:i[data-qa^="policy-cube-arrow-"]'
		
		# Continue to NEXT SCREEN
		Given '1' element exists with 'css:button[data-qa="policy-cube-next-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:button[data-qa="policy-outputs-next-button"]'
		
		# Screen 5/6
		# There should be no ouputs
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="error-msg"]'
		
		# Go back to screen 4/6 Description
		Given '1' element exist with 'css:button[data-qa="policy-cube-previous-button"]'
		Then I click on the element on index '0'
		
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
		
		# Screen 5/6 (1 output available)
		Given '1' element exists with 'css:button[data-qa="policy-cube-next-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa^="policy-output-item"]'
		
		Given '1' element exists with 'css:button[data-qa="policy-outputs-next-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="error-msg"]'
		
		Given '1' element exists with 'css:div[data-qa="policy-output-item-!{previousFragmentID_2}"]'
		Then I click on the element on index '0'
		
		Given '1' element exists with 'css:button[data-qa="policy-outputs-next-button"]'
		Then I click on the element on index '0'

		# Screen 6/6
		# Press previous
		Given '1' element exists with 'css:button[data-qa="policy-cube-previous-button"]'
		Then I click on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="policy-outputs-next-button"]'
		Then I click on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="policy-save-button"]'
		Then I click on the element on index '0'
		
		Given '1' element exists with 'css:aside[data-qa="confirm-modal"]'
		And '1' element exists with 'css:button[data-qa="modal-ok-button"]'
		Then I click on the element on index '0'
		And '1' element exists with 'css:i[data-qa^="policy-context-menu-"]'
		
		# Try to add the same policy
		Given '1' element exists with 'css:button[data-qa="policies-new-button"]'
		Then I click on the element on index '0'
		
		# Fill in everything and click Continue
		Given '1' element exists with 'css:input[data-qa="policy-description-name"]'
		Then I type 'myPolicy' on the element on index '0'
		Given '1' element exists with 'css:input[data-qa="policy-description-description"]'
		Then I type 'my Policy Description' on the element on index '0'
		Given '1' element exists with 'css:button[data-qa="policy-description-next-button"]'
		When I click on the element on index '0'
		Then '1' element exists with 'css:div[data-qa="error-msg"]'
		
		Given '1' element exists with 'css:a[data-qa="dashboard-menu-policies"]'
		Then I click on the element on index '0'		
		
		Scenario: Delete fragments
		When I send a 'DELETE' request to '/fragment/input/!{previousFragmentID}'
		Then the service response status must be '200'.
		When I send a 'GET' request to '/fragment/input'
		Then the service response status must be '200' and its response must contain the text '[]'
		When I send a 'DELETE' request to '/fragment/output/!{previousFragmentID_2}'
		Then the service response status must be '200'.
		When I send a 'GET' request to '/fragment/output'
		Then the service response status must be '200' and its response must contain the text '[]'

		