/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Injectable } from '@angular/core';

import { batchInputsObject, streamingInputsObject } from 'data-templates/inputs';
import { batchOutputsObject, streamingOutputsObject } from 'data-templates/outputs';
import { batchTransformationsObject, streamingTransformationsObject } from 'data-templates/transformations';
import { StepType, Engine } from '@models/enums';
import { WizardService } from '@app/wizard/services/wizard.service';

@Injectable()
export class InitializeWorkflowService {

  constructor(private _wizardService: WizardService) { }

  public getInitializedWorkflow(workflow: any) {
    if (!workflow.uiSettings || !workflow.uiSettings.position || !workflow.uiSettings.position.x) {
      workflow.uiSettings = {};
      workflow.uiSettings.position = {
        x: 0,
        y: 0,
        k: 1
      };
    }
    const nodes = workflow.pipelineGraph.nodes;
    this._initializeNodes(nodes);
    const idsMap = nodes.reduce((acc, node) => {
      acc[node.name] = node.id;
      return acc;
    }, {});
    const writers: any = {};
    nodes.forEach(node => {
      if (node.outputsWriter.length) {
        writers[node.id] = node.outputsWriter.reduce((acc, writer) => {
          acc[idsMap[writer.outputStepName]] = writer;
          return acc;
        }, {});
      }
    });

    return { workflow, writers };
  }

  private _initializeNodes(nodes) {
    let x = 40; // default node positions
    let y = 100;
    let h = true;
    if (nodes && nodes.length) {
      nodes.forEach((node: any) => {
        node.id = this._wizardService.generateStepID();
        if (!node.uiConfiguration || !node.uiConfiguration.position) {
          node.uiConfiguration = {};
          node.uiConfiguration.position = {
            x: x,
            y: y
          };

          h ? x += 200 : y += 120;
          h = !h;
        }
      });
    }
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
    if (workflowType === Engine.Streaming) {
      switch (nodeElement.stepType) {
        case StepType.Input:
          return streamingInputsObject[nodeElement.classPrettyName];
        case StepType.Output:
          return streamingOutputsObject[nodeElement.classPrettyName];
        case StepType.Transformation:
          return streamingTransformationsObject[nodeElement.classPrettyName];
      }
    } else {
      switch (nodeElement.stepType) {
        case StepType.Input:
          return batchInputsObject[nodeElement.classPrettyName];
        case StepType.Output:
          return batchOutputsObject[nodeElement.classPrettyName];
        case StepType.Transformation:
          return batchTransformationsObject[nodeElement.classPrettyName];
      }
    }
  }
}


