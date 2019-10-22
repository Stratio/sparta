/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { MenuOptionListGroup } from '@app/shared/components/menu-options-list/menu-options-list.component';

export const groupOptions: MenuOptionListGroup[] = [
   {
      options: [
         {
            icon: 'icon-edit',
            label: 'Rename',
            id: 'group-name-edition'
         },
         {
            icon: 'icon-move-to-folder',
            label: 'Move',
            id: 'group-move'
         }
      ]
   },
   {
      options: [
         {
            icon: 'icon-trash',
            label: 'Delete',
            id: 'group-delete',
            color: 'critical'
         }
      ]
   }
];

export const workflowOptions: MenuOptionListGroup[] = [
   {
      options: [
         {
            icon: 'icon-edit',
            label: 'Rename',
            id: 'workflow-name-edition'
         },
         {
            icon: 'icon-move-to-folder',
            label: 'Move',
            id: 'workflow-move'
         }
      ]
   },
   {
      options: [
         {
            icon: 'icon-trash',
            label: 'Delete',
            id: 'workflow-delete',
            color: 'critical'
         }
      ]
   }
];

const editVersionOption = {
  options: [{
    icon: 'icon-edit-3',
    label: 'Edit',
    id: 'version-edit'
  }]
};

const runVersionOptions = {
  options: [
    {
      icon: 'icon-play',
      label: 'Run',
      id: 'version-run-workflow'
    },
    {
      icon: 'icon-play',
      label: 'Run with parameters',
      id: 'version-run-params-workflow'
    },
    {
      icon: 'icon-alarm',
      label: 'Schedule',
      id: 'version-schedule-workflow'
    },
    {
      icon: 'icon-alarm',
      label: 'Schedule with parameters',
      id: 'version-schedule-params-workflow'
    }
  ]
};

const newVersionOptions = {
  options: [
    {
      icon: 'icon-square-plus',
      label: 'New workflow from this version',
      id: 'version-new-workflow'
    },
    {
      icon: 'icon-square-plus',
      label: 'New version',
      id: 'version-new-version'
    },
    {
      icon: 'icon-cup',
      label: 'Promote version',
      id: 'promote-version'
    }
  ]
};

const deleteVersionOption = {
  options: [
    {
      icon: 'icon-trash',
      label: 'Delete',
      id: 'version-delete',
      color: 'critical'
    }
  ]
};

export const versionOptions = {
  editVersionOption: editVersionOption,
  runVersionOptions: runVersionOptions,
  newVersionOptions: newVersionOptions,
  deleteVersionOption: deleteVersionOption
};
