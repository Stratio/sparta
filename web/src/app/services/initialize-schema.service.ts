/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import { writerTemplate } from 'data-templates/index';
import { TranslateService } from '@ngx-translate/core';

@Injectable()
export class InitializeSchemaService {

   constructor(private _translate: TranslateService) { }

   public static setDefaultWorkflowSettings(value: any): any {
      const model: any = {};
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
      const model: any = {};
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
      model.supportedDataRelations = value.supportedDataRelations;

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
      model.supportedDataRelations = template.supportedDataRelations;
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

   getHelpOptions(template: any) {
      return this.getSubProperties(template)
         .filter(prop => prop.tooltip && prop.tooltip.length)
         .map(prop => {
            return {
               label: this._translate.instant(prop.propertyName),
               text: prop.tooltip,
               sections: prop.propertyType === 'list' ? this.getHelpOptions(prop.fields) : null
            };
         });
   }

   getSubProperties(property: any) {
      if (property.length) {
         let props = [];
         property.forEach(prop => {
            if (prop.properties) {
               props = props.concat(this.getSubProperties(prop.properties));
            } else {
               props.push(prop);
            }
         });
         return props;
      } else {
         return [property];
      }
   }
}


