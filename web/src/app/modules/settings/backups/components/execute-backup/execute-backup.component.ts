/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, OnInit, Output, EventEmitter, ViewChild } from '@angular/core';

@Component({
    selector: 'execute-backup',
    templateUrl: './execute-backup.template.html',
    styleUrls: ['./execute-backup.styles.scss']
})
export class ExecuteBackup implements OnInit {

    @Output() onCloseExecuteModal = new EventEmitter<any>();

    public removeData = false;

    ngOnInit(): void {}

    onChangeValue(value: any) {
        this.removeData = value;
    }

    submitExecute() {
        this.onCloseExecuteModal.emit({
            execute: true,
            removeData: this.removeData
        });
    }

    constructor() {

    }
}
