/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { ChangeDetectionStrategy, Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';

import { cloneDeep as _cloneDeep } from 'lodash';

@Component({
  selector: 'node-tree-data',
  styleUrls: ['node-tree-data.component.scss'],
  templateUrl: 'node-tree-data.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class NodeTreeDataComponent implements OnChanges {
  @Input() data: any;

  public dataType: string;
  public objectProperties: Array<any> = [];
  constructor() { }

  public nodeData: any;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.data) {


      this.nodeData = _cloneDeep(this.data);
      if (typeof this.data === 'string') {
        try {
          this.nodeData = JSON.parse(this.data);
        } catch (error) { }
      }

      if (Array.isArray(this.nodeData)) {
        this.dataType = 'array';
        this.nodeData = this.data.map((item) => ({
          value: item,
          type: typeof item,
          open: true
        }));
      } else if (typeof this.data === 'object') {
        this.dataType = 'object';
        const objectProp = [];
        for (const key in this.nodeData) {
          objectProp.push({
            key: key,
            value: this.nodeData[key],
            type: typeof this.nodeData[key],
            open: true
          });
        }
        this.objectProperties = objectProp;
      } else {
        this.dataType = 'value';
      }
    }
  }

  isArray(value) {
    return Array.isArray(value);
  }

  toggleProp(prop: any) {
    prop.open = prop.open ? false : true;
  }
}
