/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Action } from '@ngrx/store';

export const RESET_WIZARD = '[Wizard] Reset wizard';
export const GET_MENU_TEMPLATES = '[Wizard] Get menu templates';
export const GET_MENU_TEMPLATES_COMPLETE = '[Wizard] Get menu templates complete';
export const GET_MENU_TEMPLATES_ERROR = '[Wizard] Get menu templates error';
export const MODIFY_WORKFLOW = '[Wizard] Modify workflow';
export const MODIFY_WORKFLOW_COMPLETE = '[Wizard] Modify workflow complete';
export const MODIFY_WORKFLOW_ERROR = '[Wizard] Modify workflow error';
export const SELECTED_CREATION_ENTITY = '[Wizard] Selected creation entity';
export const DESELECTED_CREATION_ENTITY = '[Wizard] Deselected creation entity';
export const TOGGLE_ENTITY_DETAILS = '[Wizard] Toggle entity details';
export const CHANGE_WORKFLOW_NAME = '[Wizard] Change workflow name';
export const SELECT_ENTITY = '[Wizard] Select entity';
export const UNSELECT_ENTITY = '[Wizard] Unselect entity';
export const MODIFY_IS_PIPELINES_NODE_EDITION = '[Wizard] Modify pipelines node edition';
export const EDIT_ENTITY = '[Wizard] Edit entity';
export const HIDE_EDIT_ENTITY = '[Wizard] Hide edit entity';
export const CREATE_NODE_RELATION = '[Wizard] Create node relation';
export const CREATE_NODE_RELATION_COMPLETE = '[Wizard] Create node relation complete';
export const CREATE_NODE_RELATION_ERROR = '[Wizard] Create node relation error';
export const DELETE_NODE_RELATION = '[Wizard] delete node relation';
export const CREATE_ENTITY = '[Wizard] create entity';
export const CREATE_ENTITY_COMPLETE = '[Wizard] create entity complete';
export const DELETE_ENTITY = '[Wizard] Delete entity';
export const SAVE_WORKFLOW_POSITIONS = '[Wizard] Save workflow positions';
export const SAVE_EDITOR_POSITION = '[Wizard] Save editor position';
export const SHOW_EDITOR_CONFIG = '[Wizard] Show editor config';
export const HIDE_EDITOR_CONFIG = '[Wizard] Hide editor config';
export const SAVE_ENTITY = '[Wizard] Save entity';
export const SAVE_ENTITY_COMPLETE = '[Wizard] Save entity complete';
export const SAVE_ENTITY_ERROR = '[Wizard] Save entity error';
export const SAVE_SETTINGS = '[Wizard] Save settings';
export const SAVE_WORKFLOW = '[Wizard] Save workflow';
export const SAVE_WORKFLOW_COMPLETE = '[Wizard] Save workflow complete';
export const SAVE_WORKFLOW_ERROR = '[Wizard] Save workflow error';
export const SELECT_SEGMENT = '[Wizard] select segment';
export const UNSELECT_SEGMENT = '[Wizard] Unselect segment';
export const DELETE_SEGMENT = '[Wizard] Delete segment';
export const SEARCH_MENU_OPTION = '[Wizard] Search floating menu option';
export const UNDO_CHANGES = '[Wizard] Undo changes';
export const REDO_CHANGES = '[Wizard] Redo changes';
export const HIDE_EDIT = '[Wizard] Hide edit';
export const DUPLICATE_NODE = '[Wizard] Duplicate node';
export const VALIDATE_WORKFLOW = '[Wizard] Validate workflow';
export const VALIDATE_WORKFLOW_COMPLETE = '[Wizard] Validate workflow complete';
export const VALIDATE_WORKFLOW_ERROR = '[Wizard] Validate workflow error';
export const SET_WIZARD_DIRTY = '[Wizard] Set wizard state dirty';
export const SET_WORKFLOW_TYPE = '[Wizard] Set workflow type';
export const SHOW_SETTINGS = '[Wizard] Show settings';
export const HIDE_SETTINGS = '[Wizard] Hide settings';
export const TOGGLE_CROSSDATA_CATALOG = '[Wizard] Toggle crossdata catalog';
export const SHOW_EDGE_OPTIONS = '[Wizard] Show edge options';
export const HIDE_EDGE_OPTIONS = '[Wizard] Hide edge options';
export const SELECT_EDGE_TYPE = '[Wizard] Select edge type';
export const SHOW_NOTIFICATION = '[Wizard] Show notification';
export const SHOW_GLOBAL_ERRORS = '[Wizard] Show global errors';
export const SET_WORKFLOW_ID = '[Wizard] Set workflow Id';
export const SET_MULTISELECTION_MODE = '[Wizard] Set multiselection mode';
export const COPY_NODES = '[Wizard] Copy nodes action';
export const PASTE_NODES = '[Wizard] Paste nodes action';
export const PASTE_NODES_COMPLETE = '[Wizard] Paste nodes complete action';
export const SELECT_MULTIPLE_STEPS = '[Wizard] Select multiple steps';

export class GetMenuTemplatesAction implements Action {
  readonly type = GET_MENU_TEMPLATES;
}

export class GetMenuTemplatesCompleteAction implements Action {
  readonly type = GET_MENU_TEMPLATES_COMPLETE;
  constructor(public payload: any) { }
}

export class GetMenuTemplatesErrorAction implements Action {
  readonly type = GET_MENU_TEMPLATES_ERROR;
}

export class ResetWizardAction implements Action {
  readonly type = RESET_WIZARD;
  constructor(public payload: any) { }
}

export class ModifyWorkflowAction implements Action {
  readonly type = MODIFY_WORKFLOW;
  constructor(public payload: any) { }
}

export class ModifyWorkflowCompleteAction implements Action {
  readonly type = MODIFY_WORKFLOW_COMPLETE;
  constructor(public payload: any) { }
}

export class ModifyWorkflowErrorAction implements Action {
  readonly type = MODIFY_WORKFLOW_ERROR;
  constructor(public payload: any) { }
}

export class SelectedCreationEntityAction implements Action {
  readonly type = SELECTED_CREATION_ENTITY;
  constructor(public payload: any) { }
}

export class DeselectedCreationEntityAction implements Action {
  readonly type = DESELECTED_CREATION_ENTITY;
}

export class ChangeWorkflowNameAction implements Action {
  readonly type = CHANGE_WORKFLOW_NAME;
  constructor(public payload: any) { }
}

export class EditEntityAction implements Action {
  readonly type = EDIT_ENTITY;
}

export class HideEditEntityAction implements Action {
  readonly type = HIDE_EDIT;
}

export class SelectEntityAction implements Action {
  readonly type = SELECT_ENTITY;
  constructor(public payload: any, public isPipelinesEdition: boolean) { }
}

export class UnselectEntityAction implements Action {
  readonly type = UNSELECT_ENTITY;
}

export class ModifyIsPipelinesNodeEdition implements Action {
  readonly type = MODIFY_IS_PIPELINES_NODE_EDITION;
  constructor(public payload: boolean) { }
}

export class ToggleDetailSidebarAction implements Action {
  readonly type = TOGGLE_ENTITY_DETAILS;
}

export class CreateNodeRelationAction implements Action {
  readonly type = CREATE_NODE_RELATION;
  constructor(public payload: any) { }
}

export class CreateNodeRelationCompleteAction implements Action {
  readonly type = CREATE_NODE_RELATION_COMPLETE;
  constructor(public payload: any) { }
}

export class CreateNodeRelationErrorAction implements Action {
  readonly type = CREATE_NODE_RELATION_ERROR;
  constructor(public payload: any) { }
}

export class DeleteNodeRelationAction implements Action {
  readonly type = DELETE_NODE_RELATION;
  constructor(public payload: any) { }
}

export class DeleteEntityAction implements Action {
  readonly type = DELETE_ENTITY;
}

export class CreateEntityAction implements Action {
  readonly type = CREATE_ENTITY;
  constructor(public payload: any) { }
}

export class CreateEntityCompleteAction implements Action {
  readonly type = CREATE_ENTITY_COMPLETE;
  constructor(public payload: any) { }
}

export class SaveWorkflowPositionsAction implements Action {
  readonly type = SAVE_WORKFLOW_POSITIONS;
  constructor(public payload: any) { }
}

export class SaveEditorPosition implements Action {
  readonly type = SAVE_EDITOR_POSITION;
  constructor(public payload: any) { }
}

export class ShowEditorConfigAction implements Action {
  readonly type = SHOW_EDITOR_CONFIG;
  constructor(public payload: any) { }
}

export class HideEditorConfigAction implements Action {
  readonly type = HIDE_EDITOR_CONFIG;
}

export class SaveSettingsAction implements Action {
  readonly type = SAVE_SETTINGS;
  constructor(public payload: any) { }
}

export class SaveEntityAction implements Action {
  readonly type = SAVE_ENTITY;
  constructor(public payload: any) { }
}

export class SaveEntityCompleteAction implements Action {
  readonly type = SAVE_ENTITY_COMPLETE;
  constructor(public payload: any) { }
}

export class SaveEntityErrorAction implements Action {
  readonly type = SAVE_ENTITY_ERROR;
  constructor(public payload: any) { }
}

export class SaveWorkflowAction implements Action {
  readonly type = SAVE_WORKFLOW;
  constructor(public payload: any) { }
}

export class SaveWorkflowCompleteAction implements Action {
  readonly type = SAVE_WORKFLOW_COMPLETE;
  constructor(public payload: any) { }
}

export class SaveWorkflowErrorAction implements Action {
  readonly type = SAVE_WORKFLOW_ERROR;
  constructor(public payload: any) { }
}

export class SelectSegmentAction implements Action {
  readonly type = SELECT_SEGMENT;
  constructor(public payload: any) { }
}

export class UnselectSegmentAction implements Action {
  readonly type = UNSELECT_SEGMENT;
}

export class SearchFloatingMenuAction implements Action {
  readonly type = SEARCH_MENU_OPTION;
  constructor(public payload: any) { }
}

export class UndoChangesAction implements Action {
  readonly type = UNDO_CHANGES;
}

export class RedoChangesAction implements Action {
  readonly type = REDO_CHANGES;
}

export class DuplicateNodeAction implements Action {
  readonly type = DUPLICATE_NODE;
  constructor(public payload: any) { }
}

export class ValidateWorkflowAction implements Action {
  readonly type = VALIDATE_WORKFLOW;
}

export class ValidateWorkflowCompleteAction implements Action {
  readonly type = VALIDATE_WORKFLOW_COMPLETE;
  constructor(public payload: any) { }
}

export class ValidateWorkflowErrorAction implements Action {
  readonly type = VALIDATE_WORKFLOW_ERROR;
}

export class SetWizardStateDirtyAction implements Action {
  readonly type = SET_WIZARD_DIRTY;
}

export class SetWorkflowTypeAction implements Action {
  readonly type = SET_WORKFLOW_TYPE;
  constructor(public payload: any) { }
}

export class ShowSettingsAction implements Action {
  readonly type = SHOW_SETTINGS;
}

export class HideSettingsAction implements Action {
  readonly type = HIDE_SETTINGS;
}

export class ToggleCrossdataCatalogAction implements Action {
  readonly type = TOGGLE_CROSSDATA_CATALOG;
}

export class ShowEdgeOptionsAction implements Action {
  readonly type = SHOW_EDGE_OPTIONS;
  constructor(public payload: any) { }
}

export class HideEdgeOptionsAction implements Action {
  readonly type = HIDE_EDGE_OPTIONS;
}

export class SelectEdgeTypeAction implements Action {
  readonly type = SELECT_EDGE_TYPE;
  constructor(public payload: any) { }
}

export class ShowNotificationAction implements Action {
  readonly type = SHOW_NOTIFICATION;
  constructor(public payload: any) { }
}

export class ShowGlobalErrorsAction implements Action {
  readonly type = SHOW_GLOBAL_ERRORS;
}

export class SetWorkflowIdAction implements Action {
  readonly type = SET_WORKFLOW_ID;
  constructor(public workflowId: string) { }
}

export class SetMultiselectionModeAction implements Action {
  readonly type = SET_MULTISELECTION_MODE;
  constructor(public active: boolean) { }
}

export class CopyNodesAction implements Action {
  readonly type = COPY_NODES;
}

export class PasteNodesAction implements Action {
  readonly type = PASTE_NODES;
}

export class PasteNodesCompleteAction implements Action {
  readonly type = PASTE_NODES_COMPLETE;
  constructor(public payload: any) { }
}

export class SelectMultipleStepsAction implements Action {
  readonly type = SELECT_MULTIPLE_STEPS;
  constructor(public stepNames: Array<string>) {}
}

export type Actions =
  ResetWizardAction |
  GetMenuTemplatesAction |
  GetMenuTemplatesCompleteAction |
  GetMenuTemplatesErrorAction |
  ModifyWorkflowAction |
  ModifyWorkflowCompleteAction |
  ModifyWorkflowErrorAction |
  SelectedCreationEntityAction |
  DeselectedCreationEntityAction |
  ChangeWorkflowNameAction |
  EditEntityAction |
  HideEditEntityAction |
  SelectEntityAction |
  UnselectEntityAction |
  ModifyIsPipelinesNodeEdition |
  ToggleDetailSidebarAction |
  CreateNodeRelationAction |
  CreateNodeRelationErrorAction |
  CreateNodeRelationCompleteAction |
  DeleteNodeRelationAction |
  DeleteEntityAction |
  SaveWorkflowPositionsAction |
  ShowEditorConfigAction |
  HideEditorConfigAction |
  SaveEntityAction |
  SaveEntityCompleteAction |
  SaveEntityErrorAction |
  SaveSettingsAction |
  SaveWorkflowAction |
  SaveWorkflowCompleteAction |
  SaveWorkflowErrorAction |
  SearchFloatingMenuAction |
  UndoChangesAction |
  RedoChangesAction |
  CreateEntityAction |
  CreateEntityCompleteAction |
  DuplicateNodeAction |
  ValidateWorkflowAction |
  ValidateWorkflowCompleteAction |
  ValidateWorkflowErrorAction |
  SetWizardStateDirtyAction |
  SetWorkflowTypeAction |
  HideSettingsAction |
  ShowSettingsAction |
  ToggleCrossdataCatalogAction |
  ShowEdgeOptionsAction |
  HideEdgeOptionsAction |
  SelectEdgeTypeAction |
  ShowNotificationAction |
  ShowGlobalErrorsAction |
  SetWorkflowIdAction |
  SetMultiselectionModeAction |
  CopyNodesAction |
  PasteNodesAction |
  PasteNodesCompleteAction |
  SelectMultipleStepsAction;
