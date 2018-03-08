/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, OnInit, Input, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';

@Component({
    selector: 'sparta-sidebar',
    templateUrl: './sparta-sidebar.template.html',
    styleUrls: ['./sparta-sidebar.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SpartaSidebarComponent implements OnInit {

    @Input() isVisible = false;
    @Output() onCloseSidebar = new EventEmitter();

    constructor() { }

    ngOnInit() { }
}