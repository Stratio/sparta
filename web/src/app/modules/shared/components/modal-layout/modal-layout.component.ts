/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'modal-layout',
  templateUrl: './modal-layout.component.html',
  styleUrls: ['./modal-layout.component.scss']
})

export class ModalLayoutComponent {
    @Input() modalOpened: Boolean = false;
    @Input() title: String;
    @Input() returnTo: String;
    @Output() onCloseModal =  new EventEmitter();

    private closeModal(ev) {
      this.modalOpened = false;
      this.onCloseModal.emit(ev);
    }

}
