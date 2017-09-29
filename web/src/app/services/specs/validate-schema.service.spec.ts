///
/// Copyright (C) 2015 Stratio (http://stratio.com)
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///         http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

import { ValidateSchemaService } from '../validate-schema.service';
import * as settingsTemplate from 'data-templates/settings.json';
import * as kafkaTemplate from 'data-templates/inputs/kafka.json';

const validateSchemaService: ValidateSchemaService = new ValidateSchemaService();
const schema = [
    {
        propertyId: 'prop1',
        default: 'val'
    },
    {
        propertyId: 'prop2',
        default: 'val'
    },
    {
        name: 'subcategory',
        properties: [
            {
                propertyId: 'prop3',
                default: 'val'
            }
        ]
    }
];

describe('validate-schema.service', function () {

    it('should be able to initializa an entity', () => {
        const template: any = <any>kafkaTemplate;
        const model = validateSchemaService.setDefaultEntityModel(kafkaTemplate);

        expect(model.classPrettyName).toBe(template.classPrettyName);
        expect(model.description).toBe(template.description);
        expect(model.stepType).toBe(template.stepType);
    });

    it('should be able to initize workflow settings model', () => {
        const template: any = <any>settingsTemplate;
        spyOn(ValidateSchemaService, 'getCategoryModel');
        const model = ValidateSchemaService.setDefaultWorkflowSettings(template);
        expect(Object.keys(model.advancedSettings).length).toBe(template.advancedSettings.length);
        expect(ValidateSchemaService.getCategoryModel).toHaveBeenCalled();
    });

    it('should initialize caterory settings model', () => {
        const model = ValidateSchemaService.getCategoryModel(schema);
        expect(model.prop1).toBeDefined();
        expect(model.prop2).toBeDefined();
        expect(model.subcategory).toBeDefined();
        expect(model.subcategory.prop3).toBe('val');
    });
});
