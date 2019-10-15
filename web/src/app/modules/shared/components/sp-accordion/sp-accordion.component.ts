/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Component, Output, EventEmitter, ChangeDetectionStrategy, Input } from '@angular/core';

@Component({
  selector: 'sp-accordion',
  templateUrl: './sp-accordion.component.html',
  styleUrls: ['./sp-accordion.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SpAccordionComponent {

  @Input() isOpen: boolean;
  @Output() onToggle = new EventEmitter<void>();

}
