/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import {WizardNode} from '@app/wizard/models/node';
import {cloneDeep as _cloneDeep} from 'lodash';

@Injectable() export class NodeHelpersService {
  private _parentNode: any;
  private _nodes: Array<any>;
  private _edges: Array<any>;

  public get parentNode() {
    return this._parentNode;
  }
  public set parentNode(parentNode: any) {
    this._parentNode = parentNode;
  }

  public get nodes() {
    return this._nodes;
  }
  public set nodes(nodes: Array<any>) {
    this._nodes = nodes;
  }

  public get edges() {
    return this._edges;
  }
  public set edges(edges: Array<any>) {
    this._edges = edges;
  }

  constructor() {}

  static _compare(a, b) {
    if (a.label < b.label) {
      return -1;
    }
    if (a.label > b.label) {
      return 1;
    }
    return 0;
  }

  private _processPrevOutputs(node: WizardNode, output: string[] = []): string[] {
    const fromEdge = this.edges.find(e => e.destination === node.name);
    if (fromEdge) {
      const prevNode = this.nodes.find(e => e.name === fromEdge.origin);
      output.push(prevNode.configuration.outputCol);
      this._processPrevOutputs(prevNode, output);
    }
    return output;
  }

  getInputFields(node: any) {
    let _output: Array<any> = [];
    if (this.parentNode.schemas) {
      const mlPipelineNodeData = _cloneDeep(this.parentNode.editionType.data);
      const mlPipelineNodeInputs = _cloneDeep(this.parentNode.schemas.inputs);
      if (mlPipelineNodeInputs && Array.isArray(mlPipelineNodeInputs) && mlPipelineNodeInputs.length) {
        if (mlPipelineNodeInputs.length === 1) {
          const inputFields = mlPipelineNodeInputs.pop().result.schema.fields;
          _output = inputFields.map(field => {
            return {
              label: field.name,
              value: field.name
            };
          });
        } else {
          console.error(new Error('Found more than 1 input from step ' + mlPipelineNodeData.name));
        }
      } else {
        console.error(new Error('No inputs found in step ' + mlPipelineNodeData.name));
      }
    }

    const prevOutputs = this._processPrevOutputs(node);
    if (prevOutputs.length) {
      const newExternalInputs = prevOutputs.map(e => {
        return {
          label: e,
          value: e
        };
      });
      _output = (_output.length) ?
        _output.concat(newExternalInputs).sort(NodeHelpersService._compare) :
        newExternalInputs.sort(NodeHelpersService._compare);
    }
    return _output;
  }
}
