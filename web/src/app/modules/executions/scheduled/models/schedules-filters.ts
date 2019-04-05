/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { StDropDownMenuItem } from "@stratio/egeo";

export const workflowTypesFilterOptions: StDropDownMenuItem[] = [
  {
    label: 'All types',
    value: ''
  },
  {
    label: 'streaming',
    value: 'Streaming'
  },
  {
    label: 'batch',
    value: 'Batch'
  }
];

export const timeIntervalsFilterOptions: StDropDownMenuItem[] = [
  {
    label: 'Launch date',
    value: 0
  },
  {
    label: 'last 60 minutes',
    value: 3600000
  },
  {
    label: 'last 6 hours',
    value: 21600000
  },
  {
    label: 'last 24 hours',
    value: 86400000
  },
  {
    label: 'last 3 days',
    value: 259200000
  },
  {
    label: 'last 7 days',
    value: 604800000
  }
];