/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {DetailInfoComponent} from "@app/executions/execution-detail/components/execution-detail-info/detail-info.component";
import {DetailInfoContainer} from "@app/executions/execution-detail/components/execution-detail-info/detail-info.container";
import { MenuOptionsListModule } from '@app/shared/components/menu-options-list/menu-options-list.module';
import {ExecutionHelperService} from "../../../../../services/helpers/execution.service";

@NgModule({
  imports: [ CommonModule, MenuOptionsListModule ],
  declarations: [ DetailInfoComponent, DetailInfoContainer ],
  exports: [ DetailInfoContainer ],
  providers: [ExecutionHelperService],
})
export class DetailInfoModule {}
