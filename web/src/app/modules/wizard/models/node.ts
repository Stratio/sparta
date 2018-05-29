/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

export interface WizardNode {
    stepType: 'Input' | 'Transformation' | 'Output';
    name: string;
    created: boolean;
    classPrettyName: string;
    hasErrors: boolean;
    nodeTemplate: any;
    configuration: any;
    debugResult: any;
    uiConfiguration: {
        position: WizardNodePosition
    };
};


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
