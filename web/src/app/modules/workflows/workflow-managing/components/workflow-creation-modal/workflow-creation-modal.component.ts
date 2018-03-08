/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, OnInit, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';

@Component({
    selector: 'workflow-creation-modal',
    templateUrl: './workflow-creation-modal.template.html',
    styleUrls: ['./workflow-creation-modal.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class WorkflowCreationModal implements OnInit {

    @Output() onSelectedMethod = new EventEmitter<string>();

    constructor() { }

    ngOnInit() { }
}
