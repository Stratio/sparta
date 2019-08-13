/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { InnerJoinTypes } from '../query-builder.constants';

export interface InputSchema {
  name: string;
  alias: string;
  fields: Array<InputSchemaField>;
  lostTable?: boolean;
}

export interface InputSchemaField {
  fieldType: string;
  column: string;
  alias: string;
  table: string;
  lostField?: boolean;
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
  lostField?: boolean;
  order: string;
}

export interface OutputSchemaFieldCopleteTable {
  expression: string;
  table: string;
  originFields: Array<OutputSchemaOriginField>;
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
  lostField?: boolean;
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
  type: InnerJoinTypes;
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
