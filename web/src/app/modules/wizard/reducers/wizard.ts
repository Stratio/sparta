/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { cloneDeep as _cloneDeep } from 'lodash';

import * as wizardActions from './../actions/wizard';
import { settingsTemplate } from 'data-templates/index';
import { InitializeSchemaService } from 'app/services';
import { WizardNode, WizardEdge } from '@app/wizard/models/node';

export interface State {
   editionMode: boolean;
   workflowId: string;
   workflowGroup: string;
   workflowVersion: number;
   loading: boolean;
   nodes: Array<WizardNode>;
   edges: Array<WizardEdge>;
   redoStates: any;
   undoStates: any;
   pristineWorkflow: boolean;
   savedWorkflow: boolean;
   validationErrors: any;
   entityNameValidation: boolean;
   showSettings: boolean;
   editionConfig: boolean;
   editionConfigType: any;
   editionConfigData: any;
   editionSaved: boolean;
   selectedEntity: string;
   showEntityDetails: boolean;
   selectedEdge: WizardEdge;
   svgPosition: any;
   settings: any;
};

const initialState: State = {
   editionMode: false,
   workflowId: '',
   workflowGroup: '',
   workflowVersion: 0,
   loading: true,
   settings: _cloneDeep(InitializeSchemaService.setDefaultWorkflowSettings(settingsTemplate)),
   svgPosition: {
      x: 0,
      y: 0,
      k: 1
   },
   edges: [],
   redoStates: [],
   undoStates: [],
   pristineWorkflow: true,
   nodes: [],
   validationErrors: {},
   savedWorkflow: false,
   editionConfig: false,
   editionConfigType: null,
   editionConfigData: null,
   editionSaved: false,
   entityNameValidation: false,
   selectedEdge: null,
   selectedEntity: '',
   showEntityDetails: false,
   showSettings: false
};

export function reducer(state: State = initialState, action: any): State {
   switch (action.type) {
      case wizardActions.RESET_WIZARD: {
         return action.payload ? {
            ...initialState,
            settings: {
               ...initialState.settings,
               basic: {
                  ...initialState.settings.basic,
                  name: ''
               }
            }
         } : initialState;
      }
      case wizardActions.SAVE_ENTITY_ERROR: {
         return {
            ...state,
            entityNameValidation: action.payload
         };
      }
      case wizardActions.SELECT_ENTITY: {
         return {
            ...state,
            selectedEntity: action.payload
         };
      }
      case wizardActions.UNSELECT_ENTITY: {
         return {
            ...state,
            selectedEntity: ''
         };
      }
      case wizardActions.TOGGLE_ENTITY_DETAILS: {
         return {
            ...state,
            showEntityDetails: !state.showEntityDetails
         };
      }
      case wizardActions.CREATE_NODE_RELATION_COMPLETE: {
         return {
            ...state,
            edges: [...state.edges, action.payload],
            undoStates: getUndoState(state),
            pristineWorkflow: false,
            redoStates: []
         };
      }
      case wizardActions.DELETE_NODE_RELATION: {
         return {
            ...state,
            edges: state.edges.filter((edge: any) =>
                  edge.origin !== action.payload.origin || edge.destination !== action.payload.destination),
            undoStates: getUndoState(state),
            pristineWorkflow: false,
            selectedEdge: null,
            redoStates: []
         };
      }
      case wizardActions.DELETE_ENTITY: {
         return {
            ...state,
            selectedEntity: '',
            nodes: state.nodes.filter((node: any) => state.selectedEntity !== node.name),
            pristineWorkflow: false,
            edges: state.edges.filter((edge: any) =>  state.selectedEntity !== edge.origin && state.selectedEntity !== edge.destination),
            undoStates: getUndoState(state),
            redoStates: []
         };
      }
      case wizardActions.SHOW_EDITOR_CONFIG: {
         return {
            ...state,
            editionConfig: true,
            editionConfigType: action.payload,
            editionSaved: false,
            selectedEntity: ''
         };
      }
      case wizardActions.HIDE_EDITOR_CONFIG: {
         return {
            ...state,
            editionConfig: false
         };
      }
      case wizardActions.CREATE_ENTITY: {
         return {
            ...state,
            nodes: [...state.nodes, action.payload],
            undoStates: getUndoState(state),
            pristineWorkflow: false,
            redoStates: []
         };
      }
      case wizardActions.SAVE_WORKFLOW_POSITIONS: {
         return {
            ...state,
            nodes: action.payload
         };
      }
      case wizardActions.SAVE_EDITOR_POSITION: {
         return {
            ...state,
            svgPosition: action.payload
         };
      }
      case wizardActions.SAVE_ENTITY_COMPLETE: {
         return {
            ...state,
            pristineWorkflow: false,
            nodes: state.nodes.map(node => node.name === action.payload.oldName ? action.payload.data : node),
            edges: state.edges.map(edge => {
               if (edge.origin === action.payload.oldName) {
                  return {
                     origin: action.payload.data.name,
                     destination: edge.destination
                  };
               } else if (edge.destination === action.payload.oldName) {
                  return {
                     origin: edge.origin,
                     destination: action.payload.data.name
                  };
               } else {
                  return edge;
               }
            }),
            undoStates: getUndoState(state),
            redoStates: [],
            editionSaved: true
         };
      }
      case wizardActions.SAVE_SETTINGS: {
         return {
            ...state,
            pristineWorkflow: false,
            settings: action.payload
         };
      }
      case wizardActions.EDIT_ENTITY: {
         const findEntity = state.nodes.find(node => node.name === state.selectedEntity);
         return {
            ...state,
            editionConfig: true,
            editionConfigType: {
               stepType: findEntity.stepType,
               data: findEntity
            },
            editionSaved: false
         };
      }
      case wizardActions.CHANGE_WORKFLOW_NAME: {
         return {
            ...state,
            pristineWorkflow: false,
            settings: {
               ...state.settings,
               basic: {
                  ...state.settings.basic,
                  name: action.payload
               }
            }
         };
      }
      case wizardActions.MODIFY_WORKFLOW_COMPLETE: {
         const workflow = action.payload;
         return {
            ...state,
            loading: false,
            svgPosition: workflow.uiSettings.position,
            nodes: workflow.pipelineGraph.nodes,
            edges: workflow.pipelineGraph.edges,
            workflowId: workflow.id,
            workflowGroup: workflow.group,
            workflowVersion: workflow.version,
            settings: {
               basic: {
                  name: workflow.name,
                  description: workflow.description,
                  tags: workflow.tags
               },
               advancedSettings: workflow.settings
            }
         };
      }
      case wizardActions.SAVE_WORKFLOW: {
         return {
            ...state,
            loading: true
         };
      }
      case wizardActions.SAVE_WORKFLOW_COMPLETE: {
         return {
            ...state,
            workflowId: action.payload,
            savedWorkflow: true,
            pristineWorkflow: true,
            loading: false
         };
      }
      case wizardActions.SAVE_WORKFLOW_ERROR: {
         return {
            ...state,
            loading: false
         };
      }

      case wizardActions.SELECT_SEGMENT: {
         return {
            ...state,
            selectedEdge: action.payload
         };
      }
      case wizardActions.UNSELECT_SEGMENT: {
         return {
            ...state,
            selectedEdge: null
         };
      }
      case wizardActions.UNDO_CHANGES: {
         if (state.undoStates.length) {
            const undoState = state.undoStates[0];
            return {
               ...state,
               nodes: _cloneDeep(undoState.nodes),
               edges: _cloneDeep(undoState.edges),
               redoStates: getRedoState(state),
               undoStates: state.undoStates.slice(1)
            };
         } else {
            return { ...state };
         }
      }
      case wizardActions.SET_WORKFLOW_TYPE: {
         return {
            ...state,
            loading: false
         }
      }
      case wizardActions.REDO_CHANGES: {
         if (state.redoStates.length) {
            const redoState = state.redoStates[0];
            return {
               ...state,
               nodes: _cloneDeep(redoState.nodes),
               edges: _cloneDeep(redoState.edges),
               undoStates: getUndoState(state),
               redoStates: state.redoStates.slice(1)
            };
         } else {
            return { ...state };
         }
      }
      case wizardActions.VALIDATE_WORKFLOW_COMPLETE: {
         return {
            ...state,
            validationErrors: action.payload
         };
      }

      case wizardActions.SET_WIZARD_DIRTY: {
         return {
            ...state,
            pristineWorkflow: false
         };
      }
      case wizardActions.SHOW_SETTINGS: {
         return {
            ...state,
            showSettings: true
         };
      }
      case wizardActions.HIDE_SETTINGS: {
         return {
            ...state,
            showSettings: false
         };
      }
      default:
         return state;
   }
}

function getUndoState(state: any) {
   const newUndoState = {
      nodes: _cloneDeep(state.nodes),
      edges: _cloneDeep(state.edges)
   };
   return [ newUndoState, ...state.undoStates.filter((value: any, index: number) => index < 4) ];
}

function getRedoState(state: any) {
   const newRedoState = {
      nodes: _cloneDeep(state.nodes),
      edges: _cloneDeep(state.edges)
   };
   return [ newRedoState, ...state.redoStates.filter((value: any, index: number) =>  index < 4) ];
}

export const getSelectedEntityData = (state: State) => state.nodes.find((node: any) => node.name === state.selectedEntity);
export const getWorkflowHeaderData = (state: State) => ({
   name: state.settings.basic.name,
   version: state.workflowVersion
});
export const areUndoRedoEnabled = (state: State) => ({
      undo: state.undoStates.length ? true : false,
      redo: state.redoStates.length ? true : false
});
export const getEditionConfigMode = (state: State) => {
   return {
      isEdition: state.editionConfig,
      editionType: state.editionConfigType
   };
};
