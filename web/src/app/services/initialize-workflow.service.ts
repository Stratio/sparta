/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Injectable } from '@angular/core';

import { batchInputsObject, streamingInputsObject } from 'data-templates/inputs';
import { batchOutputsObject, streamingOutputsObject } from 'data-templates/outputs';
import { batchTransformationsObject, streamingTransformationsObject } from 'data-templates/transformations';
import { StepType } from '@models/enums';

@Injectable()
export class InitializeWorkflowService {

  getInitializedWorkflow(workflow: any) {
    console.log("init")
    if (!workflow.uiSettings || !workflow.uiSettings.position || !workflow.uiSettings.position.x) {
      workflow.uiSettings = {};
      workflow.uiSettings.position = {
        x: 0,
        y: 0,
        k: 1
      };
    }
    const nodes = workflow.pipelineGraph.nodes;
    let x = 40; // default node positions
    let y = 100;
    let h = true;
    if (nodes && nodes.length) {
      this.setSupportedDataRelations(workflow.executionEngine, nodes);
      if (!nodes[0].uiConfiguration) {
        nodes.map((node: any) => {
          node.uiConfiguration = {};
          node.uiConfiguration.position = {
            x: x,
            y: y
          };

          h ? x += 200 : y += 120;
          h = !h;
        });
      }
    }
    return workflow;
  }

  // old workflow versions
  setSupportedDataRelations(engine: string, nodes: Array<any>) {
    nodes.forEach(nodeElement => {
      const nodeSchema = this._getNodeSchema(engine, nodeElement);
      if (nodeSchema.supportedDataRelations) {
        nodeElement.supportedDataRelations = nodeSchema.supportedDataRelations;
      }
      return nodeElement;
    });
  }


  private _getNodeSchema(workflowType: string, nodeElement: any) {
    if (workflowType === 'Streaming') {
      switch (nodeElement.stepType) {
        case StepType.Input:
          return streamingInputsObject[nodeElement.classPrettyName]
        case StepType.Output:
          return streamingOutputsObject[nodeElement.classPrettyName]
        case StepType.Transformation:
          return streamingTransformationsObject[nodeElement.classPrettyName]
      }
    } else {
      switch (nodeElement.stepType) {
        case StepType.Input:
          return batchInputsObject[nodeElement.classPrettyName]
        case StepType.Output:
          return batchOutputsObject[nodeElement.classPrettyName]
        case StepType.Transformation:
          return batchTransformationsObject[nodeElement.classPrettyName]
      }
    }
  }
}


