/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import writerContent from './writer.json';
import settings from './settings.json';
import { batchOutputsObject, streamingOutputsObject } from './outputs';


const writerTemplate: Array<any> = <any>writerContent;
export const settingsTemplate = settings;

export const getOutputWriter = function(outputName: string, engine: string) {
  const customWriter = (engine === 'Batch' ? batchOutputsObject : streamingOutputsObject)[outputName].writer || [];
  let saveModeField: any;
  const finalCustomWriter = <Array<any>>customWriter.filter(field => {
    if (field.propertyId === 'saveMode') {
      saveModeField = field;
      return false;
    } else {
      return true;
    }
  });
  return [
    ...(saveModeField ? [...writerTemplate, saveModeField] : writerTemplate),
    ...(finalCustomWriter && finalCustomWriter.length ? [{
      properties: finalCustomWriter,
      name: 'extraOptions',
      noTitle: true,
      propertyId: 'extraOptions'
    }] : [])
  ];
};
