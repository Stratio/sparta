/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, OnDestroy, OnInit, Input, Output, EventEmitter, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { icons } from '@app/shared/constants/icons';


@Component({
    selector: 'fragment-box',
    styleUrls: ['fragment-box.styles.scss'],
    templateUrl: 'fragment-box.template.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FragmentBoxComponent implements OnInit, OnDestroy {

    @Input() isChecked: boolean;
    @Input() fragmentData: any;

    @Output() onChecked = new EventEmitter<any>();

    public icons: any = icons;
    ngOnInit(): void {}

    constructor(private _cd: ChangeDetectorRef) { }



    public ngOnDestroy(): void {

    }
}
