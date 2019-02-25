/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import { cloneDeep as _cloneDeep } from 'lodash';
import { StepType, Engine } from '@models/enums';
import { streamingInputsNames, batchInputsNames } from 'data-templates/inputs';
import { streamingTransformationsNames, batchTransformationsNames } from 'data-templates/transformations';
import { streamingOutputsNames, batchOutputsNames } from 'data-templates/outputs';

@Injectable()
export class StepMenuService {
  static getStepMenu(options, engine: string) {
    const menuOptions = _cloneDeep(options);
    const finalOptions = options.map((option: any) => {
      switch (option.name) {
        case StepType.Input:
          option.subMenus = engine === Engine.Streaming ? streamingInputsNames : batchInputsNames;
          return option;
        case StepType.Output:
          option.subMenus = engine === Engine.Streaming ? streamingOutputsNames : batchOutputsNames;
          return option;
        case StepType.Transformation:
          const transformations = engine === Engine.Streaming ? streamingTransformationsNames : batchTransformationsNames;
          option.subMenus = StepMenuService._categorizeMenuOptions(transformations);
          return option;
        default:
          return option;
      }
    });

    if (engine === Engine.Batch) {
      finalOptions.push({
        name: 'AI Pipelines',
        stepType: 'Output',
        value: batchOutputsNames.find(element => element.name === 'MlPipeline').value,
        icon: 'icon-atom'
      });
    }
    return finalOptions;
  }

  private static _categorizeMenuOptions(menuOptions: Array<any>) {
    const categories = {};
    const nocategory = [];
    menuOptions.forEach(option => {
      if (option.value.category && option.value.category.length) {
        if (categories[option.value.category]) {
          categories[option.value.category].subMenus.push(option);
        } else {
          categories[option.value.category] = {
            name: option.value.category,
            value: '',
            subMenus: [option]
          };
        }
      } else {
        nocategory.push(option);
      }
    });
    const finalOptions = StepMenuService._orderMenu(Object.keys(categories).map(key => categories[key]));
    finalOptions.concat(nocategory);
    return finalOptions;
  }

  private static _orderMenu(menu) {
    return menu.sort((a, b) => {
      if (a.level < b.level) {
        return -1;
      }
      if (a.level > b.level) {
        return 1;
      }
      return 0;
    });
  }
}
