/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { cloneDeep as _cloneDeep } from 'lodash';

import * as wizardActions from './../actions/wizard';
import { FloatingMenuModel } from '@app/shared/components/floating-menu/floating-menu.component';
import { streamingInputsNames, batchInputsNames } from 'data-templates/inputs';
import { streamingTransformationsNames, batchTransformationsNames } from 'data-templates/transformations';
import { streamingOutputsNames, batchOutputsNames } from 'data-templates/outputs';
import { Engine, StepType } from '@models/enums';

export interface State {
   templates: any;
   selectedCreationEntity: any;
   entityCreationMode: boolean;
   workflowType: string;
   menuOptions: Array<FloatingMenuModel>;
   floatingMenuSearch: string;
   notification: {
      type: string;
      message: string;
   };
};

const initialState: State = {
   templates: {},
   selectedCreationEntity: null,
   entityCreationMode: false,
   floatingMenuSearch: '',
   workflowType: '',
   menuOptions: [{
      name: 'Input',
      icon: 'icon-login',
      value: 'action',
      subMenus: []
   },
   {
      name: 'Transformation',
      value: 'action',
      icon: 'icon-shuffle',
      subMenus: []
   },
   {
      name: 'Output',
      value: 'action',
      icon: 'icon-logout',
      subMenus: []
   }],
   notification: {
      type: '',
      message: ''
   }
};

export function reducer(state: State = initialState, action: any): State {
   switch (action.type) {
      case wizardActions.RESET_WIZARD: {
         return initialState;
      }
      case wizardActions.SEARCH_MENU_OPTION: {
         return {
            ...state,
            floatingMenuSearch: action.payload
         };
      }
      case wizardActions.SELECTED_CREATION_ENTITY: {
         return {
            ...state,
            selectedCreationEntity: action.payload,
            entityCreationMode: true
         };
      }
      case wizardActions.DESELECTED_CREATION_ENTITY: {
         return {
            ...state,
            selectedCreationEntity: null,
            entityCreationMode: false
         };
      }
      case wizardActions.SET_WORKFLOW_TYPE: {
         return {
            ...state,
            workflowType: action.payload,
            menuOptions: _cloneDeep(initialState.menuOptions).map((option: any) => {
               switch (option.name) {
                  case StepType.Input:
                     option.subMenus = option.subMenus.concat(action.payload === Engine.Streaming ?
                        streamingInputsNames : batchInputsNames);
                     return option;
                  case StepType.Output:
                     option.subMenus = option.subMenus.concat(action.payload === Engine.Streaming ?
                        streamingOutputsNames : batchOutputsNames);
                     return option;
                  case StepType.Transformation:
                     const transformations: any[] = action.payload === Engine.Streaming ?
                        streamingTransformationsNames : batchTransformationsNames;
                     const categories = {};
                     const nocategory = [];
                     transformations.forEach(transformation => {
                        if (transformation.value.category && transformation.value.category.length) {
                           if (categories[transformation.value.category]) {
                              categories[transformation.value.category].subMenus.push(transformation);
                           } else {
                              categories[transformation.value.category] = {
                                 name: transformation.value.category,
                                 value: '',
                                 subMenus: [
                                    transformation
                                 ]
                              };
                           }
                        } else {
                           nocategory.push(transformation);
                        }
                     });
                     option.subMenus = option.subMenus.concat(
                        (Object.keys(categories).map(key => categories[key]) as Array<any>).sort((a, b) => {
                           if (a.level < b.level) {
                              return -1;
                           }
                           if (a.level > b.level) {
                              return 1;
                           }
                           return 0;
                        }))
                        .concat(nocategory);
                     return option;
                  default:
                     return option;
               }
            })
         };
      }
      case wizardActions.GET_MENU_TEMPLATES_COMPLETE: {
         const menuOptions: any = _cloneDeep(state.menuOptions);
         const type = [StepType.Input, StepType.Transformation, StepType.Output];
         menuOptions.forEach((option, index) => {
            const templateGroup = action.payload[type[index].toLowerCase()];
            if (!templateGroup.length) {
               option.subMenus.splice(index, 1);
            }
            const templates = templateGroup.filter((template: any) =>
               template.executionEngine === state.workflowType)
               .map((template: any) => ({
                  name: template.name,
                  type: 'template',
                  data: template,
                  stepType: type[index]
               }));
            if (templates && templates.length) {
               option.subMenus = [{
                  name: 'Templates',
                  value: '',
                  subMenus: templates
               }, ...option.subMenus];
            }
         });
         return {
            ...state,
            menuOptions: menuOptions,
            templates: action.payload
         };
      }
      case wizardActions.DUPLICATE_NODE: {
         return {
            ...state,
            selectedCreationEntity: action.payload,
            entityCreationMode: true
         };
      }
      case wizardActions.SHOW_NOTIFICATION: {
         return {
            ...state,
            notification: action.payload
         };
      }
      default:
         return state;
   }
}
export const getWorkflowType = (state: State) => state.workflowType;
export const getTemplates = (state: State) => state.templates;
export const getMenuOptions = (state: State) => {
   if (state.floatingMenuSearch.length) {
      let menu: any = [];
      const matchString = state.floatingMenuSearch.toLowerCase();
      state.menuOptions.forEach((option: any) => {
         const icon = option.icon;
         const options: any = [];
         option.subMenus.forEach((type: any) => {
            if (!type.subMenus && type.name.toLowerCase().indexOf(matchString) !== -1) {
               options.push(Object.assign({}, type, {
                  icon: icon
               }));
            }
         });
         menu = menu.concat(options);
      });
      return menu;
   } else {
      return state.menuOptions;
   }
};
export const isCreationMode: any = (state: State) => {
   return {
      active: state.entityCreationMode,
      data: state.selectedCreationEntity
   };
};
