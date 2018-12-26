/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { cloneDeep as _cloneDeep } from 'lodash';

import * as debugActions from './../actions/debug';
import * as wizardActions from './../actions/wizard';
import { settingsTemplate } from 'data-templates/index';
import { InitializeSchemaService } from 'app/services';
import { WizardNode, WizardEdge, EdgeOption } from '@app/wizard/models/node';

export interface State {
  editionMode: boolean;
  workflowId: string;
  workflowGroup: string;
  workflowVersion: number;
  draggableMode: boolean;
  multiselectionMode: boolean;
  loading: boolean;
  nodes: Array<WizardNode>;
  edges: Array<WizardEdge>;
  redoStates: any;
  undoStates: any;
  pristineWorkflow: boolean;
  savedWorkflow: boolean;
  validationErrors: {
    valid: boolean;
    messages: Array<string>;
  };
  serverStepValidations: any;
  entityNameValidation: boolean;
  showSettings: boolean;
  editionConfig: boolean;
  editionConfigType: any;
  editionConfigData: any;
  editionSaved: boolean;
  selectedEntities: Array<string>;
  isPipelineEdition: boolean;
  showEntityDetails: boolean;
  selectedEdge: WizardEdge;
  svgPosition: any;
  settings: any;
  isShowedCrossdataCatalog: boolean;
  edgeOptions: EdgeOption;
  debugFile: string;
}

const initialState: State = {
  editionMode: false,
  workflowId: '',
  workflowGroup: '',
  workflowVersion: 0,
  loading: true,
  multiselectionMode: false,
  draggableMode: false,
  serverStepValidations: {},
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
  validationErrors: {
    valid: true,
    messages: []
  },
  savedWorkflow: false,
  editionConfig: false,
  editionConfigType: null,
  editionConfigData: null,
  editionSaved: false,
  entityNameValidation: false,
  selectedEdge: null,
  selectedEntities: [],
  isPipelineEdition: false,
  showEntityDetails: false,
  showSettings: false,
  isShowedCrossdataCatalog: true,
  edgeOptions: {
    clientX: 0,
    clientY: 0,
    active: false
  },
  debugFile: undefined
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
    case wizardActions.SET_MULTISELECTION_MODE: {
      return {
        ...state,
        multiselectionMode: action.active,
      };
    }
    case wizardActions.SET_DRAGGABLE_MODE: {
      return {
        ...state,
        draggableMode: action.active
      };
    }
    case wizardActions.SELECT_ENTITY: {
      let selected;
      if (state.multiselectionMode) {
        if (state.selectedEntities.length && state.selectedEntities.indexOf(action.payload) > -1) {
          selected = state.selectedEntities.filter(entity => entity !== action.payload);
        } else {
          selected = [...state.selectedEntities, action.payload];
        }
      } else {
        selected = [action.payload];
      }
      return {
        ...state,
        selectedEntities: selected,
        isPipelineEdition: action.isPipelinesEdition
      };
    }
    case wizardActions.SELECT_MULTIPLE_STEPS: {
      return {
        ...state,
        selectedEntities: !state.multiselectionMode ? action.stepNames : [...state.selectedEntities, ...action.stepNames].filter((item, pos, arr) => arr.indexOf(item) === pos)
      };
    }
    case wizardActions.UNSELECT_ENTITY: {
      return {
        ...state,
        selectedEntities: [],
        isPipelineEdition: false
      };
    }
    case wizardActions.MODIFY_IS_PIPELINES_NODE_EDITION: {
      return {
        ...state,
        isPipelineEdition: action.payload
      };
    }
    case wizardActions.SET_WORKFLOW_ID: {
      return {
        ...state,
        workflowId: action.workflowId
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
        redoStates: [],
        edgeOptions: {
          active: false
        }
      };
    }
    case wizardActions.DELETE_ENTITY: {
      return {
        ...state,
        selectedEntities: [],
        nodes: state.nodes.filter((node: any) => state.selectedEntities.indexOf(node.name) === -1),
        pristineWorkflow: false,
        edges: state.edges.filter((edge: any) => state.selectedEntities.indexOf(edge.origin) === -1 && state.selectedEntities.indexOf(edge.destination) === -1),
        undoStates: getUndoState(state),
        redoStates: []
      };
    }
    case wizardActions.SHOW_EDITOR_CONFIG: {
      return {
        ...state,
        debugFile: undefined,
        editionConfig: true,
        editionConfigType: action.payload,
        editionSaved: false
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
              destination: edge.destination,
              dataType: edge.dataType
            };
          } else if (edge.destination === action.payload.oldName) {
            return {
              origin: edge.origin,
              destination: action.payload.data.name,
              dataType: edge.dataType
            };
          } else {
            return edge;
          }
        }),
        undoStates: getUndoState(state),
        redoStates: [],
        editionConfig: !action.payload.closeEdition,
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
      const editedEntity = state.selectedEntities[state.selectedEntities.length - 1];
      const findEntity = state.nodes.find(node => node.name === editedEntity);
      return {
        ...state,
        editionConfig: true,
        debugFile: undefined,
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
        editionMode: true,
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
        editionMode: true,
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
        selectedEdge: action.payload,
        selectedEntities: []
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
      };
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
        validationErrors: {
          valid: action.payload.valid,
          messages: action.payload.messages.filter(message => !message.step)
        },
        serverStepValidations: action.payload.messages
          .filter(message => message.step)
          .reduce((acc, el) => {
            const position = acc[el.step];
            if (position) {
              if (!el.subStep) {
                position['errors'].push(el.message);
              } else {
                if (!position['internalErrors']) {
                  position['internalErrors'] = [];
                }
                position['internalErrors'].push(el);
              }
            } else {
              if (!el.subStep) {
                acc[el.step] = {};
                acc[el.step]['errors'] = [el.message];
              } else {
                acc[el.step]['internalErrors'] = [el];
              }
            }
            return acc;
          }, {})
      };
    }

    case wizardActions.SET_WIZARD_DIRTY: {
      return {
        ...state,
        pristineWorkflow: false,
        edgeOptions: {
          active: false
        }
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
    case wizardActions.TOGGLE_CROSSDATA_CATALOG: {
      return {
        ...state,
        isShowedCrossdataCatalog: !state.isShowedCrossdataCatalog
      };
    }
    case wizardActions.SHOW_EDGE_OPTIONS: {
      return {
        ...state,
        edgeOptions: {
          active: true,
          clientX: action.payload.clientX,
          clientY: action.payload.clientY,
          supportedDataRelations: action.payload.supportedDataRelations,
          relation: action.payload.relation,
          edgeType: action.payload.edgeType
        }
      };
    }
    case wizardActions.SELECT_EDGE_TYPE: {
      return {
        ...state,
        edges: state.edges.map((edge: WizardEdge) => {
          const relation = action.payload.relation;
          return relation.initialEntityName === edge.origin && relation.finalEntityName === edge.destination ? {
            ...edge,
            dataType: action.payload.value
          } : edge;
        }),
        selectedEdge: null
      };
    }
    case wizardActions.HIDE_EDGE_OPTIONS: {
      return {
        ...state,
        edgeOptions: {
          active: false
        }
      };
    }
    case wizardActions.SHOW_GLOBAL_ERRORS: {
      return {
        ...state,
        showEntityDetails: true,
        selectedEntities: []
      };
    }
    case debugActions.UPLOAD_DEBUG_FILE_COMPLETE: {
      return {
        ...state,
        debugFile: action.payload
      };
    }
    case wizardActions.PASTE_NODES_COMPLETE: {
      return {
        ...state,
        nodes: [
          ...state.nodes,
          ...action.payload.nodes
        ],
        edges: [
          ...state.edges,
          ...action.payload.edges
        ],
        selectedEntities: action.payload.nodes.map(pastedNodes => pastedNodes.name)
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
  return [newUndoState, ...state.undoStates.filter((value: any, index: number) => index < 4)];
}

function getRedoState(state: any) {
  const newRedoState = {
    nodes: _cloneDeep(state.nodes),
    edges: _cloneDeep(state.edges)
  };
  return [newRedoState, ...state.redoStates.filter((value: any, index: number) => index < 4)];
}

export const getSelectedEntityData = (state: State) => {
  const editedEntity = state.selectedEntities[state.selectedEntities.length - 1];
  return editedEntity ?
    state.nodes.find((node: any) => node.name === editedEntity) : undefined;
};

export const getWorkflowHeaderData = (state: State) => ({
  name: state.settings.basic.name,
  version: state.workflowVersion
});

export const areUndoRedoEnabled = (state: State) => ({
  undo: !!state.undoStates.length,
  redo: !!state.redoStates.length
});

export const getEditionConfigMode = (state: State) => {
  return {
    isEdition: state.editionConfig,
    editionType: state.editionConfigType,
    isPipelinesEdition: state.isPipelineEdition
  };
};


