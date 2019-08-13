/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';
import { isEqual } from 'lodash';
import { InputSchema } from '../models/schema-fields';

@Injectable()
export class QueryBuilderService {

  normalizeBackup(backup: any, inputSchemas: Array<InputSchema>) {
    if (isEqual(backup.inputSchemaFields, inputSchemas)) {
      return backup;
    } else {
      const inputsMap = inputSchemas.reduce((obj, item) => {
        obj[item.name] = item;
        return obj;
      }, {});
      const inputsMockMap = backup.inputSchemaFields.reduce((obj, item) => {
        obj[item.name] = item;
        return obj;
      }, {});

      const fieldsMap: any = {};
      const originTableSchemas: Array<string> = [];
      backup.outputSchemaFields.forEach(field => field.originFields.forEach(originField => {
        if (!fieldsMap[originField.table]) {
          fieldsMap[originField.table] = [];
        }
        if (!originTableSchemas.includes(originField.table)) {
          originTableSchemas.push(originField.table);
        }

        const completedField = inputsMockMap[originField.table].fields.find(f => f.column === originField.name);
        if (completedField) {
          fieldsMap[originField.table].push(completedField);
        }
      }));

      // search and complete undefined tables
      originTableSchemas.forEach(table => {
        if (!inputsMap[table]) {
          this._createMockTable(table, inputSchemas, inputsMap);
        }
      });

      if (backup.join && backup.join.joins && backup.join.joins.length) {
        const joins = backup.join.joins;
        joins.forEach(join => {
          this._completeJoinField(inputsMap, inputSchemas, join.destination);
          this._completeJoinField(inputsMap, inputSchemas, join.origin);
        });
      }

      // mark lost output schema fields
      backup.outputSchemaFields.forEach(field => field.originFields.forEach(originField => {
        if (originField.name === '' || originField.name === '*') {
          return;
        }
        if (!inputsMap[originField.table].fields.find(f => f.column === originField.name)) {
          field.lostField = true;
        }
      }));

      // complete lost input schema fields
      Object.keys(fieldsMap).forEach((table: string) => {
        fieldsMap[table].forEach(field => {
          if (!inputsMap[table].fields.find(inputField => inputField.column === field.column)) {
            inputsMap[table].fields.push({
              ...field,
              alias: inputsMap[table].alias,
              lostField: true
            });
          }
        });
      });
      return {
        ...backup,
        inputSchemaFields: inputSchemas
      };
    }
  }

  private _createMockTable(name, inputSchemas, inputsMap) {
    const table = {
      name,
      alias: `t${inputSchemas.length + 1}`,
      fields: [],
      lostTable: true
    };
    inputsMap[name] = table;
    inputSchemas.push(table);
  }

  private _completeJoinField(inputsMap, inputSchemas, field) {
    let fieldTable = inputsMap[field.table];
    if (!fieldTable) {
      this._createMockTable(field.table, inputSchemas, inputsMap);
    }
    fieldTable = inputsMap[field.table];
    const fieldInput = fieldTable.fields.find(f => f.column === field.column);

    if (!fieldInput) {
      field.lostField = true;
      fieldTable.fields.push({
        ...field
      });
    }
  }

}

