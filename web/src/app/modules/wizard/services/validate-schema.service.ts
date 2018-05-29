/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
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
}


