/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, OnInit, Output, EventEmitter, Input, ViewChildren, ViewContainerRef } from '@angular/core';

@Component({
    selector: 'form-file',
    templateUrl: './form-file.template.html',
    styleUrls: ['./form-file.styles.scss']
})
export class FormFileComponent implements OnInit {

    @Input() text: any;
    @Input() icon = 'icon-upload';
    @Input() type = 'button-primary';
    @Output() onFileUpload = new EventEmitter<string>();

    @ViewChildren('file') vc: any;

    ngOnInit(): void { }

    click() {
        this.vc.first.nativeElement.click();
    }

    onChange(event: any) {
        this.onFileUpload.emit(event.target.files);
        this.vc.first.nativeElement.value = '';
    }

    constructor() { }

}

