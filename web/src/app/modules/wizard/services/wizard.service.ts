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

import { Injectable } from '@angular/core';

import { batchInputsObject, streamingInputsObject } from 'data-templates/inputs';
import { batchOutputsObject, streamingOutputsObject } from 'data-templates/outputs';
import { batchTransformationsObject, streamingTransformationsObject } from 'data-templates/transformations';

@Injectable()
export class WizardService {

    private _workflowType: string;

    public get workflowType() {
        return this._workflowType;
    }

    public set workflowType(workflowType: string) {
        this._workflowType = workflowType;
    }

    constructor() { }


    getInputs() {
        return this._workflowType === 'Streaming' ? streamingInputsObject : batchInputsObject;
    }

    getOutputs() {
        return this._workflowType === 'Streaming' ? streamingOutputsObject : batchOutputsObject;
    }

    getTransformations() {
        return this._workflowType === 'Streaming' ? streamingTransformationsObject : batchTransformationsObject;
    }

    getInputsNames() {
        return this._workflowType === 'Streaming' ? streamingInputsObject : batchInputsObject;
    }

    getOutputsNames() {
        return this._workflowType === 'Streaming' ? streamingOutputsObject : batchOutputsObject;
    }

    getTransformationsNames() {
        return this._workflowType === 'Streaming' ? streamingTransformationsObject : batchTransformationsObject;
    }
}
