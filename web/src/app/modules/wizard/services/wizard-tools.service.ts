/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import { WizardEdge, WizardNode } from '@app/wizard/models/node';
import { InitializeStepService } from '@app/wizard/services/initialize-step.service';
import { WizardService } from './wizard.service';

@Injectable()
export class WizardToolsService {

  constructor(private _initializeStepService: InitializeStepService,
    private _wizardService: WizardService) { }

  public getCopiedModel(selectedNodes: Array<string>, workflowNodes: Array<any>, workflowEdges: Array<WizardEdge>, writers: any, workflowType: string) {
    const nodes: Array<any> = workflowNodes.filter(wNode => selectedNodes.indexOf(wNode.name) > -1);
    const edges = workflowEdges.filter(edge => selectedNodes.indexOf(edge.origin) > -1 && selectedNodes.indexOf(edge.destination) > -1);
    const nodesIdsMap = nodes.reduce((acc, node) => {
      acc[node.id] = node;
      return acc;
    }, {});
    const copiedWriters = Object.keys(writers).reduce((acc, key) => {
      // if the writer node is copied
      if (nodesIdsMap[key]) {
        acc[key] = Object.keys(writers[key]).reduce((cWriters, outputId) => {
          if (nodesIdsMap[outputId]) {
            cWriters[outputId] = writers[key][outputId];
          }
          return cWriters;
        }, {});
      }
      return acc;
    }, {});
    return JSON.stringify({
      objectIdType: 'workflow',
      workflowType: workflowType,
      nodes,
      edges,
      writers: copiedWriters
    }, null, 2);
  }

  public normalizeCopiedSteps(nodes: Array<WizardNode>, edges: Array<WizardEdge>, currentStepNames: Array<string>, svgPosition: { x: number; y: number; k: number }, writers?: any) {
    const names = [...currentStepNames];
    const nodesMap: any = {};
    const steps = this._getStepsCopiedPosition(nodes, svgPosition);
    const idsMap = {};
    steps.forEach(wNode => {
      const newName = this._initializeStepService.getNewStepName(wNode.name, names);
      nodesMap[wNode.name] = newName;
      names.push(newName);
      const newId = this._wizardService.generateStepID();
      idsMap[wNode.id] = newId;
      wNode.id = newId;
      wNode.name = newName;
      return {
        ...wNode,
        name: newName
      };
    });
    const normalizedEdges = edges.map(edge => {
      return {
        ...edge,
        origin: nodesMap[edge.origin],
        destination: nodesMap[edge.destination],
      };
    });
    return {
      nodes: steps,
      edges: normalizedEdges,
      writers: writers ? Object.keys(writers).reduce((acc, key) => {
        const currentStepWriters = Object.keys(writers[key]);
        if (currentStepWriters.length) {
          // set new ID
          acc[idsMap[key]] = currentStepWriters.reduce((cWriters, outputId) => {
            cWriters[idsMap[outputId]] = writers[key][outputId];
            return cWriters;
          }, {});
        }
        return acc;
      }, {}) : null
    };
  }

  private _getStepsCopiedPosition(nodes: Array<WizardNode>, svgPosition: { x: number; y: number; k: number }) {
    const coors: any = {};
    nodes.forEach(wNode => {
      const x = wNode.uiConfiguration.position.x;
      const y = wNode.uiConfiguration.position.y;
      if (!coors.x1 || coors.x1 > x) {
        coors.x1 = x;
      }
      if (!coors.x2 || coors.x2 < x) {
        coors.x2 = x;
      }
      if (!coors.y1 || coors.y1 > y) {
        coors.y1 = y;
      }
      if (!coors.y2 || coors.y2 < y) {
        coors.y2 = y;
      }
    });

    const xdiff = coors.x1 + svgPosition.x / svgPosition.k;
    const ydiff = coors.y1 + svgPosition.y / svgPosition.k;

    nodes.forEach(wNode => {
      wNode.uiConfiguration.position.x -= xdiff;
      wNode.uiConfiguration.position.y -= ydiff;
    });
    return nodes;
  }


}
