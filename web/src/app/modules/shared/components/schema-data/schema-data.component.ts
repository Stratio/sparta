/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Component, ChangeDetectionStrategy, Input, OnInit } from '@angular/core';
import { cloneDeep } from 'lodash';

@Component({
  selector: 'schema-data',
  templateUrl: './schema-data.component.html',
  styleUrls: ['./schema-data.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SchemaDataComponent implements OnInit {

  @Input() data: any;
  @Input() schema: any;
  public schemaParse;

  constructor() { }

  ngOnInit(): void {
    this.schemaParse = [];

    this.schema.forEach( property => {
      if (property.propertyType === 'list') {
        const groupFields = [];
        let group = [];

        property.fields.forEach((field, index) => {
          group.push(field);

          if ( group.length === 3 ) {
            groupFields.push(group);
            group = [];
          }

          if (property.fields.length === (index + 1)) {
            groupFields.push(group);
            group = [];
          }

        });
        this.schemaParse.push({...property, groupFields});
      } else {
        this.schemaParse.push(property);
      }
    });
  }

  getValue(value: any, propertyId?: string): string | Array<string> {
    const _value: string = (typeof value === 'object') ? JSON.stringify(value, null, 2) : value;

    if (propertyId === 'fieldsString') {
      const reExctractStruct = /StructField\(([^)]+)\)/gi;
      const structArray = _value.match(reExctractStruct);
      const finalArray = [];

      structArray.forEach( el => {
        const cleanString = el.replace(/\(|\)|StructField/g, '').split(',');
        finalArray.push(cleanString);
      });

      finalArray.map( el => {
        el[2] = (el[2]) ? 'Nullable' : 'Not Nullable';
        return el;
      });
      return finalArray;
    }

    return _value;
  }

  stringToArray(val): Array<any> {
    return typeof val === 'object' ? val : JSON.parse(val);
  }

}
