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
import { InitializeSchemaService, ValidateSchemaService } from 'app/services';

@Injectable()
export class WizardEditorService {

    getNewEntityName(entityType: string, entities: Array<any>, index: number = 0): string {
        let name = entityType;
        if (index > 0) {
            name += '(' + index + ')';
        }
        let valid = true;
        entities.forEach((ent: any) => {
            if (ent.name === name) {
                valid = false;
            }
        });

        if (!valid) {
            index++;
            return this.getNewEntityName(entityType, entities, index);
        } else {
            return name;
        }
    }

    initializeEntity(entityData: any, entities: any): any {
        let entity: any = {};
        if (entityData.type === 'template') {
            entity = Object.assign({}, entityData.data);
            // outputs havent got writer
            if (entityData.stepType !== 'Output') {
                entity.writer = this.initializeSchemaService.getDefaultWriterModel();
            }
            console.log(entityData);
            entity.name = this.getNewEntityName(entityData.data.classPrettyName, entities);
        } else {
            entity = this.initializeSchemaService.setDefaultEntityModel(entityData.value, entityData.stepType, true);
            entity.name = this.getNewEntityName(entityData.value.classPrettyName, entities);
        }
        entity.stepType = entityData.stepType;
        // validation of the model
        const errors = this.validateSchemaService.validateEntity(entity, entityData.stepType, entityData.value);
        if (errors && errors.length) {
            entity.hasErrors = true;
            entity.errors = errors;
            entity.createdNew = true; // grey box
        }
        entity.created = true; // shows created fadeIn animation
        return entity;
    }

    constructor(private initializeSchemaService: InitializeSchemaService, private validateSchemaService: ValidateSchemaService){}
}
