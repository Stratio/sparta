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

import { Injectable } from '@angular/core';
import { ValidationModel, ValidationErrorModel } from 'app/models/validation-schema.model';
import { inputs } from 'data-templates/inputs';
import { outputs } from 'data-templates/outputs';
import { transformations } from 'data-templates/transformations';
import * as settingsTemplate from 'data-templates/settings.json';
import * as writerTemplate from 'data-templates/writer.json';

@Injectable()
export class ValidateSchemaService {

    private writerSchema: any;

    constructor() {
        this.writerSchema = writerTemplate;
    }

    validateEntity(model: any, stepType: string, schema?: any) {
        if (!schema) {
            switch (stepType) {
                case 'Input':

                case 'Output':

                case 'Transformation':

                default:
                    break;
            }
        } else {
            if(stepType === 'Output') {
                return this.validate(schema.properties, model.configuration);
            } else {
                return this.validate(schema.properties, model.configuration).concat(this.validate(this.writerSchema, model.writer));
            }
        }
    }

    validateSettings() {

    }

    validate(schema: any, model: any): Array<any> {
        const errors: Array<any> = [];
        schema.forEach((prop: any) => {
            const value = model[prop.propertyId];

            if (prop.required) {
                if (prop.propertyType === 'number') {
                    if (!value) {
                        errors.push({
                            propertyName: prop.propertyId,
                            type: 'required'
                        });
                    }
                } else {
                    if (!value || !value.length) {
                        errors.push({
                            propertyName: prop.propertyId,
                            type: 'required'
                        });
                    }
                }
            }

            if (prop.regexp) {
                const re: RegExp = new RegExp(prop.regexp);
                if (!re.test(value)) {
                    errors.push({
                        propertyName: prop.propertyId,
                        type: 'regex'
                    });
                }
            }
        });

        return errors;
    }

    getTemplate(schema: any, templateType: string) {

    }
}


