/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { MenuOptionListGroup } from '@app/shared/components/menu-options-list/menu-options-list.component';

export const RUN_OPTIONS: MenuOptionListGroup[] = [{
  options: [
    {
      label: 'Run',
      id: 'simple'
    },
    {
      label: 'Run with parameters',
      id: 'advanced'
    }
  ]
}];

export const MENU_OPTIONS: MenuOptionListGroup[] = [
  {
    options: [
      {
        icon: 'icon-folder',
        label: 'New folder',
        id: 'group'
      }
    ]
  },
  {
    options: [
      {
        icon: 'icon-streaming-workflow',
        label: 'Streaming workflow',
        id: 'streaming'
      },
      {
        icon: 'icon-batch-workflow',
        label: 'Batch workflow',
        id: 'batch'
      }
    ]
  },
  {
    options: [
      {
        icon: 'icon-json',
        label: 'Import from JSON',
        id: 'file'
      }
    ]
  },
];

export const MENU_OPTIONS_DUPLICATE = [
  {
    name: 'Generate new version',
    value: 'version'
  }, {
    name: 'New workflow from this version',
    value: 'workflow'
  }
];
