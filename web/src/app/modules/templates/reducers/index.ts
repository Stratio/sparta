///
/// Copyright (C) 2015 Stratio (http://stratio.com)
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///         http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

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
export const getSelectedTransformationDisplayOption: any = createSelector(getTransformationsEntityState, 
    transformations.getSelectedDisplayOption);
export const getEditedTransformation: any = createSelector(getTransformationsEntityState, transformations.getEditedTransformation);
export const isTransformationSaved: any = createSelector(getTransformationsEntityState, transformations.isTransformationSaved);
export const isTransformationsLoaded: any = createSelector(getTransformationsEntityState, (state) => state.loaded);


