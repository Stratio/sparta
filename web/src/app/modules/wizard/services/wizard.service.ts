/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
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
