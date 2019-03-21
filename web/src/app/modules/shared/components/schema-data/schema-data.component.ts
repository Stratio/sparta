/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Component, OnInit, Output, EventEmitter, ChangeDetectionStrategy, Input } from '@angular/core';

@Component({
  selector: 'schema-data',
  templateUrl: './schema-data.component.html',
  styleUrls: ['./schema-data.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SchemaDataComponent {

  @Input() data: any;
  @Input() schema: any;
  public getKeys = Object.keys;

  constructor() { }

  getValue(value) {
    return typeof value === 'object' ? JSON.stringify(value, null, 2) : value;
  }

  stringToArray(val) {
    return typeof val === 'object' ? val : JSON.parse(val);
  }

}
