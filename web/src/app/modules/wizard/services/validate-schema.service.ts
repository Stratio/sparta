/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import { writerTemplate } from 'data-templates/index';
import { WizardService } from './wizard.service';
import {StepType} from '@models/enums';


@Injectable()
export class ValidateSchemaService {

  constructor(private _wizardService: WizardService) { }

  /**
   * @name validateEntity
   * @namespace ValidateSchemaService
   *
   * @param model: Current entity model
   * @param stepType: Step type: Input | Output | Transformation
   * @param schema(optional): Entity form schema definition
   *
   * Get the step schema and validates the model without paint the form, with the same rules and validations from the schema
   */
  validateEntity(model: any, stepType?: string, schema?: any) {
    if (!schema) {
      if (model.stepType && !stepType) {
        stepType = model.stepType;
      }
      switch (stepType) {
        case 'Input':
          return this.validate(this._wizardService.getInputs()[model.classPrettyName].properties, model.configuration)
            .concat(this.validate(writerTemplate, model.writer));
        case 'Output':
          return this.validate(this._wizardService.getOutputs()[model.classPrettyName].properties, model.configuration);
        case 'Transformation':
          return this.validate(this._wizardService.getTransformations()[model.classPrettyName].properties, model.configuration)
            .concat(this.validate(writerTemplate, model.writer));
        case 'Algorithm':
        case 'Preprocessing':
          return this.validate(this._wizardService.getPipelinesTemplates(stepType)[model.classPrettyName].properties, model.configuration);
        default:
          break;
      }
    } else {
      // if its an output skip writer validation (outputs has not writer)
      if (stepType === StepType.Input || stepType === StepType.Transformation) {
        return this.validate(schema.properties, model.configuration).concat(this.validate(writerTemplate, model.writer));
      } else {
        return this.validate(schema.properties, model.configuration);

      }
    }
  }

  /**
   *
   * @param schema: Form schema definition
   * @param model: Current entity model
   *
   * Validates the model without paint the form with the same rules and validations than this one
   */
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
              propertyId: prop.propertyId,
              propertyName: prop.propertyName,
              type: 'required',
              message: `Field ${prop.propertyName} is required.`
            });
          }
        } else {
          if (!value || !value.length) {
            errors.push({
              propertyId: prop.propertyId,
              propertyName: prop.propertyName,
              type: 'required',
              message: `Field ${prop.propertyName} is required.`
            });
          }
        }
      }
      // check regex validation
      if (prop.regexp && prop.propertyType !== 'boolean' && prop.propertyType !== 'switch') {
        const re: RegExp = new RegExp(prop.regexp);
        if (!re.test(value)) {
          errors.push({
            propertyId: prop.propertyId,
            propertyName: prop.propertyName,
            type: 'regex',
            message: `Field ${prop.propertyName} does not satisfy defined pattern.`
          });
        }
      }
    });
    return errors;
  }
}
