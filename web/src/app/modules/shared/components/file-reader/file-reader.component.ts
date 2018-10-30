/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, OnInit, Input, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';

@Component({
    selector: 'file-reader',
    templateUrl: './file-reader.template.html',
    styleUrls: ['./file-reader.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FileReaderComponent implements OnInit {

    @Input() text: String;
    @Input() showFileName = false;
    @Output() changedFile: EventEmitter<any> = new EventEmitter<any>();

    public fileName = '';

    public fileChangeEvent(fileInput: any): void {
        if (fileInput.target.files && fileInput.target.files[0]) {
            this.fileName = fileInput.target.files[0].name;
            this._cd.markForCheck();
            const reader = new FileReader();
            reader.readAsText(fileInput.target.files[0], 'UTF-8');
            reader.onload = (loadEvent: any) => {
                this.changedFile.emit(loadEvent.target.result);
            };
        }
    }

    constructor(private _cd: ChangeDetectorRef) { }

    ngOnInit() { }
}