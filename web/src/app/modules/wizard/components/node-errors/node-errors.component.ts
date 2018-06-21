/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { ChangeDetectionStrategy, Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { StTreeNode } from '@stratio/egeo';
import { Store } from '@ngrx/store';

import * as debugActions from './../../actions/debug';
import * as fromWizard from './../../reducers';

@Component({
  selector: 'node-errors',
  styleUrls: ['node-errors.styles.scss'],
  templateUrl: 'node-errors.template.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class NodeErrorsComponent implements OnChanges {

  @Input() serverValidationError: Array<any>;
  @Input() debugResult: any;
  @Input() openedSchema: 'Input' | 'Output';

  @Input() schemas: any;

  public outputSchemasOpened = false;
  public inputSchemasOpened = false;

  public inputSchemas: NodeSchema[] = [];
  public outputSchema: NodeSchema = null;
  private _schemas: any = {};

  constructor(private _store: Store<fromWizard.State>) { }

  public toggleSchema(schema: any) {
    schema.expanded = !schema.expanded;
  }

  public showConsole(tab: string) {
    this._store.dispatch(new debugActions.ShowDebugConsoleAction(tab));
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.schemas) {
      this._setSchemaValue(changes.schemas.currentValue);
    }
  }

  private _setSchemaValue(value) {
    this._schemas = value;
    if (value && value.inputs && value.inputs.length) {
      this.inputSchemas = value.inputs.filter(input => input.result).map(input => ({
        name: input.result.step,
        tree: this._getTreeSchema(JSON.parse(input.result.schema)),
        expanded: false
      }));
    } else {
      this.inputSchemas = [];
    }
    if (value && value.output && value.output.result) {
      this.outputSchema = {
        name: value.output.result.step,
        tree: this._getTreeSchema(JSON.parse(value.output.result.schema)),
        expanded: false
      };
    } else {
      this.outputSchema = null;
    }
    if (this.openedSchema === 'Output') {
      this.outputSchemasOpened = true;
      this.inputSchemasOpened = false;
      if (this.outputSchema) {
        this.outputSchema.expanded = true;
      }
    } else {
      this.inputSchemasOpened = true;
      this.outputSchemasOpened = false;
      if (this.inputSchemas.length) {
        this.inputSchemas[0].expanded = true;
      }
    }
  }

  private _getTreeSchema(inputSchema: any): StTreeNode[] {
    return inputSchema.fields.map(field => this._getTreeNodeSchema(field));
  }

  private _getTreeNodeSchema(nodeSchema: any): StTreeNode {
    return {
      icon: '',
      name: nodeSchema.name,
      type: nodeSchema.type.type || nodeSchema.type,
      children: nodeSchema.type && nodeSchema.type.elementType && nodeSchema.type.elementType.fields ?
        nodeSchema.type.elementType.fields.map((field) => this._getTreeNodeSchema(field)) : [],
      expanded: false
    };
  }

}

class NodeSchema {
  name: String;
  tree: StTreeNode[];
  expanded: boolean;
}
