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


@Injectable()
export class InitializeSchemaService {

    public static setDefaultWorkflowSettings(value: any): any {
        let model: any = {};
        model.basic = {
            name: 'workflow-name',
            description: ''
        };
        model.advancedSettings = {};
        value.advancedSettings.map((category: any) => {
            model.advancedSettings[category.name] = this.getCategoryModel(category.properties);
        });
        return model;
    }


    public static getCategoryModel(value: any): any {
        let model: any = {};
        value.map((prop: any) => {
            if (prop.properties) {
                model[prop.name] = this.getCategoryModel(prop.properties);
            } else {
                model[prop.propertyId] = prop.default ? prop.default : null;
            }
        });
        return model;
    }


    setDefaultEntityModel(workflowtype: string, value: any, stepType: string, writerOptions = false): any {
        const model: any = {};
        model.configuration = {};
        model.supportedEngines = value.supportedEngines;
        model.executionEngine = workflowtype;
        value.properties.map((prop: any) => {
            model.configuration[prop.propertyId] = prop.default ? prop.default : null;
        });
        model.classPrettyName = value.classPrettyName;
        model.className = value.className;
        //model.description = value.description;
        // arity property for workflow validation
        if (value.arity) {
            model.arity = value.arity;
        }

        model.stepType = stepType;

        if (writerOptions && stepType !== 'Output') {
            model.writer = this.getDefaultWriterModel();
        }

        return model;
    }

    setTemplateEntityModel(template: any): any {
        const model: any = {};
        model.configuration = template.configuration;
        model.supportedEngines = template.supportedEngines;
        model.executionEngine = template.executionEngine;
        model.classPrettyName = template.classPrettyName;
        model.className = template.className;
        model.nodeTemplate = {
            id: template.id,
            name: template.name
        };
        if (template.templateType !== 'output') {
            model.writer = this.getDefaultWriterModel();
        }

        return model;
    }

    getDefaultWriterModel(): any {
        const writerTpl = <any>writerTemplate;
        const writer: any = {};
        writerTpl.map((prop: any) => {
            writer[prop.propertyId] = prop.default ? prop.default : null;
        });
        return writer;
    }




    constructor() {

    }
}


