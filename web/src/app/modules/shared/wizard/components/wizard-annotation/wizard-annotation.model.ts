/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

export interface WizardAnnotationModel {
  date: number;
  author: string;
  messages: Array<WizardAnnotationMessage>;
  color: string;
  stepName?: string;
  edge?: {
    origin: string;
    destination: string;
  };
  position?: {
    x: number,
    y: number
  };
}


export interface WizardAnnotation extends WizardAnnotationModel {
  number?: number;
  openOnCreate?: boolean;
  tipPosition?: {
    x: number,
    y: number
  };
  messages: Array<WizardAnnotationMessageLocalized>;
}

export interface WizardAnnotationMessage {
  text: string;
  author: string;
  date: number;
}

export interface WizardAnnotationMessageLocalized extends WizardAnnotationMessage {
  localizedDate?: string;
}

export const annotationColors = ['#0f1b27', '#128bdd', '#2dcfbe', '#fdbd2b', '#fa9330', '#ec445c'];
