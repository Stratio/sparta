/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

export interface WizardNode {
  id: string;
  stepType: 'Input' | 'Transformation' | 'Output' | 'Preprocessing' | 'Algorithm';
  name: string;
  created: boolean;
  createdNew?: boolean;
  classPrettyName: string;
  errors?: any;
  hasErrors: boolean;
  nodeTemplate: any;
  configuration: any;
  description?: string;
  debugResult: any;
  supportedDataRelations?: Array<any>;
  uiConfiguration: {
    position: WizardNodePosition
  };
  writers?: Array<any>;
}


export interface WizardNodePosition {
  x: number;
  y: number;
}

export interface WizardEdge {
  origin: string;
  destination: string;
  dataType?: string;
}

export interface WizardEdgeNodes {
  origin: WizardNode;
  destination: WizardNode;
}

interface EdgePosition {
  name: string;
  uiConfiguration: {
    position: WizardNodePosition
  };
}

export interface EdgeOption {
  active: boolean;
  supportedDataRelations?: Array<string>;
  edgeType?: string;
  clientX?: number;
  clientY?: number;
  relation?: {
    initialEntityName: string;
    finalEntityName: string;
  };
}

export interface CreationMode {
  active: boolean;
  data: any;
}

export interface EditionConfigMode {
  editionConfig: boolean;
  isEdition: boolean;
  isPipelinesEdition: boolean;
  editionType: any;
  relationData: {
    inputs: Array<string>;
    output: Array<string>;
  };
  serverValidation?: any;
  serverValidationInternalErrors?: any;
  debugResult: any;
  schemas?: any;
  inputSteps?: any;
}
