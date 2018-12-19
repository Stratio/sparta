/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { ChangeDetectionStrategy, Component, Input, EventEmitter, Output } from '@angular/core';

@Component({
  selector: 'wizard-position-tool',
  styleUrls: ['wizard-position-tool.component.scss'],
  templateUrl: 'wizard-position-tool.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WizardPositionToolComponent {
  @Input() currentZoom: number;

  @Output() onZoomIn = new EventEmitter();
  @Output() onZoomOut = new EventEmitter();
  @Output() onCenter = new EventEmitter();
  @Output() setZoom = new EventEmitter<number>();
}
