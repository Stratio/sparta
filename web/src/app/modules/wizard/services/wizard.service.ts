/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';

import { batchInputsObject, streamingInputsObject } from 'data-templates/inputs';
import { batchOutputsObject, streamingOutputsObject } from 'data-templates/outputs';
import { batchTransformationsObject, streamingTransformationsObject } from 'data-templates/transformations';
import { homeGroup } from '@app/shared/constants/global';
import { PipelineType, Engine } from '@models/enums';
import { batchPreprocessingObject, streamingPreprocessingObject } from 'data-templates/pipelines/pipelines-preprocessing';
import { batchAlgorithmObject, streamingAlgorithmObject } from 'data-templates/pipelines/pipelines-algorithm';

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

  static getEntitiesSteps(category: any, matchString: string, parentIcon?: string) {
    let menu: any = [];
    const options: any = [];
    category.forEach((categoryType: any) => {
      const icon = parentIcon || categoryType.icon;
      if (!categoryType.subMenus) {
        if (categoryType.name.toLowerCase().indexOf(matchString) !== -1) {
          options.push(Object.assign({}, categoryType, {
            icon: icon
          }));
        }
      } else {
        menu = menu.concat(WizardService.getEntitiesSteps(categoryType.subMenus, matchString, icon));
      }
    });
    return menu.concat(options);
  }

  /**
   * Return input list form schemas
   */
  getInputs() {
    return this._workflowType === Engine.Streaming ? streamingInputsObject : batchInputsObject;
  }

  getOutputs() {
    return this._workflowType === Engine.Streaming ? streamingOutputsObject : batchOutputsObject;
  }

  getTransformations() {
    return this._workflowType === Engine.Streaming ? streamingTransformationsObject : batchTransformationsObject;
  }

  getInputsNames() {
    return this._workflowType === Engine.Streaming ? streamingInputsObject : batchInputsObject;
  }

  getOutputsNames() {
    return this._workflowType === Engine.Streaming ? streamingOutputsObject : batchOutputsObject;
  }

  getTransformationsNames() {
    return this._workflowType === Engine.Streaming ? streamingTransformationsObject : batchTransformationsObject;
  }

  getPipelinesTemplates(pipelineType: string) {
    if (pipelineType === PipelineType.Algorithm) {
      return this._workflowType === Engine.Streaming ? streamingAlgorithmObject : batchAlgorithmObject;
    } else {
      return this._workflowType === Engine.Streaming ? streamingPreprocessingObject : batchPreprocessingObject;
    }
  }

  getWorkflowModel(state: any) {
    const wizard = state.wizard.wizard;
    const entities = state.wizard.entities;
    return Object.assign({
      id: wizard.workflowId && wizard.workflowId.length ? wizard.workflowId : undefined,
      version: wizard.workflowVersion,
      executionEngine: entities.workflowType,
      uiSettings: {
        position: wizard.svgPosition
      },
      pipelineGraph: {
        nodes: wizard.nodes,
        edges: wizard.edges
      },
      group: wizard.workflowGroup && wizard.workflowGroup.id ?
        wizard.workflowGroup : state.workflowsManaging ? state.workflowsManaging.workflowsManaging.currentLevel : homeGroup,
      settings: wizard.settings.advancedSettings
    }, wizard.settings.basic);
  }
}
