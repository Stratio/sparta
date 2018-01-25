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
import { writerTemplate } from 'data-templates/index';
import { WizardService } from './wizard.service';


@Injectable()
export class ValidateSchemaService {


    constructor(private wizardService: WizardService) { }

    validateEntity(model: any, stepType: string, schema?: any) {
        if (!schema) {
            switch (stepType) {
                case 'Input':
                    return this.validate(this.wizardService.getInputs()[model.classPrettyName].properties, model.configuration)
                        .concat(this.validate(writerTemplate, model.writer));
                case 'Output':
                    return this.validate(this.wizardService.getOutputs()[model.classPrettyName].properties, model.configuration);
                case 'Transformation':
                    return this.validate(this.wizardService.getTransformations()[model.classPrettyName].properties, model.configuration)
                        .concat(this.validate(writerTemplate, model.writer));
                default:
                    break;
            }
        } else {
            // if its an output skip writer validation (outputs has not writer)
            if (stepType === 'Output') {
                return this.validate(schema.properties, model.configuration);
            } else {
                return this.validate(schema.properties, model.configuration).concat(this.validate(writerTemplate, model.writer));
            }
        }
    }

    validateSettings() {

    }

    validate(schema: any, model: any): Array<any> {
        const errors: Array<any> = [];

        schema.forEach((prop: any) => {
            const value = model[prop.propertyId];
            let disabled = false;

            // if there are not validations skip this input
            if (!prop.regexp && !prop.required) {
                return;
            }

            // check if the input is disabled
            if (prop.visible && prop.visible[0].length) {
                prop.visible[0].forEach((condition: any) => {
                    if (model[condition.propertyId] !== condition.value) {
                        disabled = true;
                    }
                });
            }

            if (disabled) {
                return;
            }

            // check required validation
            if (prop.required && prop.propertyType !== 'boolean' && prop.propertyType !== 'switch') {
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

            // check regex validation
            if (prop.regexp && prop.propertyType !== 'boolean' && prop.propertyType !== 'switch') {
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

    validateWorkflow(nodes: any, edges: any) {

        edges.map((edge: any) => {

        });

        // function validateNode()
    }
}


