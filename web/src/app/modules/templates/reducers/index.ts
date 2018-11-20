/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { createSelector } from 'reselect';
import { createFeatureSelector } from '@ngrx/store';

import * as fromRoot from 'reducers';
import * as inputs from './input';
import * as outputs from './output';
import * as transformations from './transformation';

export interface TemplatesState {
   inputs: inputs.State;
   outputs: outputs.State;
   transformations: transformations.State;
}

export interface State extends fromRoot.State {
   templates: TemplatesState;
}

export const reducers = {
   inputs: inputs.reducer,
   outputs: outputs.reducer,
   transformations: transformations.reducer
};

export const getTemplatesState = createFeatureSelector<TemplatesState>('templates');

export const getInputsEntityState = createSelector(
   getTemplatesState,
   state => state.inputs
);

export const getOutputsEntityState = createSelector(
   getTemplatesState,
   state => state.outputs
);

export const getTransformationsEntityState = createSelector(
   getTemplatesState,
   state => state.transformations
);

// inputs
export const getInputList: any = createSelector(getInputsEntityState, inputs.getInputList);
export const getSelectedInputs: any = createSelector(getInputsEntityState, inputs.getSelectedInputs);
export const getSelectedInputDisplayOption: any = createSelector(getInputsEntityState, inputs.getSelectedDisplayOption);
export const getEditedInput: any = createSelector(getInputsEntityState, inputs.getEditedInput);
export const isInputSaved: any = createSelector(getInputsEntityState, inputs.isInputSaved);
export const isInputsLoaded: any = createSelector(getInputsEntityState, (state) => state.loaded);

// outputs
export const getOutputList: any = createSelector(getOutputsEntityState, outputs.getOutputList);
export const getSelectedOutputs: any = createSelector(getOutputsEntityState, outputs.getSelectedOutputs);
export const getSelectedOutputDisplayOption: any = createSelector(getOutputsEntityState, outputs.getSelectedDisplayOption);
export const getEditedOutput: any = createSelector(getOutputsEntityState, outputs.getEditedOutput);
export const isOutputSaved: any = createSelector(getOutputsEntityState, outputs.isOutputSaved);
export const isOutputsLoaded: any = createSelector(getOutputsEntityState, (state) => state.loaded);


// transformations
export const getTransformationList: any = createSelector(getTransformationsEntityState, transformations.getTransformationList);
export const getSelectedTransformations: any = createSelector(getTransformationsEntityState, transformations.getSelectedTransformations);
export const getSelectedTransformationDisplayOption: any = createSelector(getTransformationsEntityState, transformations.getSelectedDisplayOption);
export const getEditedTransformation: any = createSelector(getTransformationsEntityState, transformations.getEditedTransformation);
export const isTransformationSaved: any = createSelector(getTransformationsEntityState, transformations.isTransformationSaved);
export const isTransformationsLoaded: any = createSelector(getTransformationsEntityState, (state) => state.loaded);


