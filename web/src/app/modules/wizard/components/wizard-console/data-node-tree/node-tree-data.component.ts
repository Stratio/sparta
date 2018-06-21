/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
  ChangeDetectionStrategy,
  Component,

  Input,
  OnInit
} from '@angular/core';

@Component({
  selector: 'node-tree-data',
  styleUrls: ['node-tree-data.component.scss'],
  templateUrl: 'node-tree-data.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class NodeTreeDataComponent implements OnInit {


  @Input() data: any;

  public dataType: string;
  public objectProperties: Array<any> = [];

  constructor() { }

  ngOnInit(): void {
    try {
      this.data = JSON.parse(this.data);
    } catch (error) {
    }
    if (Array.isArray(this.data)) {
      this.dataType = 'array';
      this.data = this.data.map((item) => ({
        value: item,
        type: typeof item
      }));
    } else if (typeof this.data === 'object') {
      this.dataType = 'object';
      const objectProp = [];
      for (const key in this.data) {
        objectProp.push({
          key: key,
          value: this.data[key],
          type: typeof this.data[key]
        });
      }
      this.objectProperties = objectProp;
    } else {
      this.dataType = 'value';
    }
  }

  isArray(value) {
    return Array.isArray(value);
  }

  toggleProp(prop: any) {
    prop.open = prop.open ? false : true;
  }
}
