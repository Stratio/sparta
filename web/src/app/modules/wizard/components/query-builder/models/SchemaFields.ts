/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

export interface InputSchema {
   name: string;
   alias: string;
   fields: Array<InputSchemaField>;
 }

 export interface InputSchemaField {
    fieldType: string;
    column: string;
    alias: string;
    table: string;
 }

 export interface SchemaFieldPosition {
   x: number;
   y: number;
   height?: number;
 }

 export interface SchemaPositions {
  [column: string]: SchemaFieldPosition;
 }

 export interface OutputSchemaField {
    expression: string;
    column: string;
    originFields: Array<OutputSchemaOriginField>;
    position: SchemaFieldPosition;
    order: string;
 }

 export interface OutputSchemaOriginField {
   name: string;
   alias: string;
   table: string;
 }

export interface SelectedInputFields {
  [table: string]: Array<InputSchemaField>;
}

export interface SelectedInputFieldsNames {
  [table: string]: Array<String>;
}

export interface Path {
  initData: {
    tableName: string;
    fieldName: string;
  };
  coordinates: {
    init: {
      x: number;
      y: number;
      height: number;
    },
    end: {
      x: number;
      y: number;
      height: number;
    }
  };
}

export interface ContainerPositions {
  [table: string]: Array<SchemaFieldPosition>;
}

export interface JoinSchema {
   type: string;
   joins: Array<Join>;
}

export interface Join {
   origin: InputSchemaField;
   destination: InputSchemaField;
   initData: {
      tableName: string;
      fieldName: string;
    };
    fieldPositions?: any;
}

enum OrderByOptions {
   ASC = 0,
   DESC = 1
}

export interface OrderBy {
   label: OrderByOptions;
   field: OutputSchemaField;
}

export interface OrderBySchema {
   order: Array<OrderBy>;
}
