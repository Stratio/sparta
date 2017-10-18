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

import { InitializeSchemaService } from '../initialize-schema.service';
import * as settingsTemplate from 'data-templates/settings.json';
import * as kafkaTemplate from 'data-templates/inputs/kafka.json';
import * as printTemplate from 'data-templates/outputs/print.json';

const initializeSchemaService: InitializeSchemaService = new InitializeSchemaService();
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

describe('initialize-schema.service', function () {

    it('should be able to initialize an entity', () => {
        const template: any = <any>kafkaTemplate;
        const model = initializeSchemaService.setDefaultEntityModel(kafkaTemplate, 'Input', true);
        expect(model.classPrettyName).toBe(template.classPrettyName);
        expect(model.description).toBe(template.description);
    });

    it('should be able to initialize entity writer if it is not an output', () => {
        const model = initializeSchemaService.setDefaultEntityModel(kafkaTemplate, 'Input', true);
        expect(model.writer).toBeDefined();
    });

    it('should not initialize the writer if it is an output', () => {
        const model = initializeSchemaService.setDefaultEntityModel(printTemplate, 'Output', true);
        expect(model.writer).toBeUndefined();
    });

    it('should be able to initize workflow settings model', () => {
        const template: any = <any>settingsTemplate;
        spyOn(InitializeSchemaService, 'getCategoryModel');
        const model = InitializeSchemaService.setDefaultWorkflowSettings(template);
        expect(Object.keys(model.advancedSettings).length).toBe(template.advancedSettings.length);
        expect(InitializeSchemaService.getCategoryModel).toHaveBeenCalled();
    });

    it('should initialize caterory settings model', () => {
        const model = InitializeSchemaService.getCategoryModel(schema);
        expect(model.prop1).toBeDefined();
        expect(model.prop2).toBeDefined();
        expect(model.subcategory).toBeDefined();
        expect(model.subcategory.prop3).toBe('val');
    });
});
