/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable } from '@angular/core';

@Injectable()
export class WizardConfigEditorService {

  public normalizeQueryConfiguration(queryBuilder: any) {
   const usedInputs = [].concat.apply([], queryBuilder.outputSchemaFields
     .map(output => output.originFields.map(field => `${field.alias}.${field.table}`)))
     .filter((elem, index, self) => index === self.indexOf(elem));
   let joinClause = null;
   let fromClause = null;
   if (usedInputs.length > 1 || queryBuilder.join.type.includes('RIGHT_ONLY') || queryBuilder.join.type.includes('LEFT_ONLY')) {
     // JOIN
     if (queryBuilder.join && queryBuilder.join.joins && queryBuilder.join.joins.length) {
       const origin = queryBuilder.join.joins[0].origin;
       const destination = queryBuilder.join.joins[0].destination;
       const leftTable = { tableName: origin.table, alias: origin.alias };
       const rightTable = { tableName: destination.table, alias: destination.alias };
       const joinTypes = queryBuilder.join.type;
       const joinConditions = queryBuilder.join.joins.map(join => ({ leftField: join.origin.column, rightField: join.destination.column }));
       joinClause = { leftTable, rightTable, joinTypes, joinConditions };
     } else {
       joinClause = {
         leftTable: { tableName: queryBuilder.inputSchemaFields[0].name, alias: queryBuilder.inputSchemaFields[0].alias },
         rightTable: { tableName: queryBuilder.inputSchemaFields[1].name, alias: queryBuilder.inputSchemaFields[1].alias },
         joinTypes: 'CROSS'
       };
     }
   } else {
     // FROM
     if (queryBuilder.inputSchemaFields.length || queryBuilder.outputSchemaFields.length) {
       const table = queryBuilder.inputSchemaFields[0];
       const ouputTable = queryBuilder.outputSchemaFields[0] || [];
       fromClause = {
         tableName: ouputTable.originFields && ouputTable.originFields.length ? ouputTable.originFields[0].table : table.name,
         alias: ouputTable.originFields && ouputTable.originFields.length ? ouputTable.originFields[0].alias : table.alias
       };
     }
   }

    // SELECT
   const selectClauses = queryBuilder.outputSchemaFields
     .map(output => {
       if (output.column) {
         return {
           expression: output.expression,
           alias: output.column
         };
       } else {
         return {
           expression: output.expression
         };
       }
     });

    // WHERE
   const whereClause = queryBuilder.filter;
   // ORDERBY
   const orderByClauses = queryBuilder.outputSchemaFields
     .map((output, position) => {
       const order = !output.order ? output.order : output.order === 'orderAsc' ? 'ASC' : 'DESC';
       return { field: output.expression, order, position };
     })
     .filter(output => !!output.order);
   const result = { selectClauses, whereClause, joinClause, orderByClauses, fromClause };
   // Delete null references
   Object.keys(result).forEach((key) => (result[key] === null) && delete result[key]);

    return result;
 }
}
