/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {MenuOptionListGroup} from "@app/shared/components/menu-options-list/menu-options-list.component";
import { QualityRule } from '@app/executions/models';

export interface Parameter {
  name: String;
  type: String;
  lastModified: String;
  completeName: String;
  selected: Boolean;
}

export interface Status {
  name: String;
  statusInfo: String;
  startTime: String;
}

export interface Info {
  name: String;
  marathonId: String;
  description: String;
  status: String;
  executionEngine: String;
  sparkURI: String;
  historyServerURI: String;
  context: Array<String>;
  launchHour: String;
  launchDate: String;
  startHour: String;
  startDate: String;
  duration: String;
  endHour: String;
  lastError: any;
  endDate: String;
}

export interface ShowedActions {
  showedReRun: boolean;
  showedStop: boolean;
  showedContextMenu: boolean;
  menuOptions: MenuOptionListGroup[];
}

export interface ExecutionDetail {
  info: Info;
  parameters: Array<Parameter>;
  filterParameters: string;
  statuses: Array<Status>;
  showedActions: ShowedActions;
  qualityRules: Array<QualityRule>;
  showConsole: boolean;
}
