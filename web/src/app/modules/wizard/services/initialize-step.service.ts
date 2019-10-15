/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Injectable } from '@angular/core';
import { InitializeSchemaService } from 'app/services';
import { ValidateSchemaService } from '@app/wizard/services/validate-schema.service';

@Injectable()
export class InitializeStepService {

  getNewStepName(entityType: string, entities: Array<string>, index: number = 0): string {
    let name = entityType;
    if (index > 0) {
      name += '_' + index;
    }
    let valid = true;
    entities.forEach((ent: any) => {
      if (ent === name) {
        valid = false;
      }
    });

    if (!valid) {
      index++;
      return this.getNewStepName(entityType, entities, index);
    } else {
      return name;
    }
  }

  initializeEntity(workflowType: string, entityData: any, entities: any): any {
    let entity: any = {};
    const names = entities.map(wNode => wNode.name);

    if (entityData.type === 'template') {
      entity = this.initializeSchemaService.setTemplateEntityModel(entityData.data);
      entity.name = this.getNewStepName(entityData.data.name, names);
    } else {
      entity = this.initializeSchemaService.setDefaultEntityModel(workflowType, entityData.value, entityData.stepType, true);
      entity.name = this.getNewStepName(entityData.value.name, names);
      // validation of the model
      const errors = this.validateSchemaService.validateEntity(entity, entityData.stepType, entityData.value);
      if (errors && errors.length) {
        entity.hasErrors = true;
        entity.errors = errors;
        entity.createdNew = true; // grey box
      }
      entity.created = true; // shows created fadeIn animation
    }
    entity.stepType = entityData.stepType;

    return entity;
  }

  constructor(private initializeSchemaService: InitializeSchemaService, private validateSchemaService: ValidateSchemaService) { }
}
