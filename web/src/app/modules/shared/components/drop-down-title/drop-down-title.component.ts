/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, OnInit, Input, ViewChild, ElementRef, ViewContainerRef } from '@angular/core';

@Component({
  selector: 'drop-down-title',
  templateUrl: './drop-down-title.component.html',
  styleUrls: ['./drop-down-title.component.scss']
})
export class DropDownTitleComponent implements OnInit {

  @Input() title: String = '';
  @Input() isVisibleContent = false;
  @Input() labelSize: number;
  @ViewChild('icon') iconElement: ElementRef;

  constructor() {}

  ngOnInit(): void {
    if (this.isVisibleContent) {
      this.iconElement.nativeElement.classList.add('icon-arrow2_down');
    } else {
      this.iconElement.nativeElement.classList.add('icon-arrow2_right');
    }

  }

  showTable() {
    this.isVisibleContent = !this.isVisibleContent;
    this.iconElement.nativeElement.classList.toggle('icon-arrow2_down');
    this.iconElement.nativeElement.classList.toggle('icon-arrow2_right');
  }

}
