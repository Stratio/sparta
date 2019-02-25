/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'sparta-execution-detail-drop-down-title',
  templateUrl: './execution-detail-drop-down-title.component.html',
  styleUrls: ['./execution-detail-drop-down-title.component.scss']
})
export class ExecutionDetailDropDownTitleComponent implements OnInit {

  @Input() title: String = '';
  @Input() IsVisibleContent = false;

  constructor() {}

  ngOnInit(): void {}

  showTable(ev) {
    const element = ev.currentTarget.querySelector('#icon');

    this.IsVisibleContent = !this.IsVisibleContent;
    element.classList.toggle('icon-arrow2_down');
    element.classList.toggle('icon-arrow2_up');
  }

}
