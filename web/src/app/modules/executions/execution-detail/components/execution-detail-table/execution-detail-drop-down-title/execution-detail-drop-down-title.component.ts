/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, OnInit, Input, ViewChild, ElementRef, ViewContainerRef } from '@angular/core';

@Component({
  selector: 'sparta-execution-detail-drop-down-title',
  templateUrl: './execution-detail-drop-down-title.component.html',
  styleUrls: ['./execution-detail-drop-down-title.component.scss']
})
export class ExecutionDetailDropDownTitleComponent implements OnInit {

  @Input() title: String = '';
  @Input() IsVisibleContent = false;
  @ViewChild('icon') iconElement: ElementRef;

  constructor() {}

  ngOnInit(): void {
    if (this.IsVisibleContent) {
      this.iconElement.nativeElement.classList.add('icon-arrow2_up');
    } else {
      this.iconElement.nativeElement.classList.add('icon-arrow2_down');
    }

  }

  showTable() {
    this.IsVisibleContent = !this.IsVisibleContent;
    this.iconElement.nativeElement.classList.toggle('icon-arrow2_down');
    this.iconElement.nativeElement.classList.toggle('icon-arrow2_up');
  }

}
