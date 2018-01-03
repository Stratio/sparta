///
/// Copyright (C) 2015 Stratio (http://stratio.com)
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///         http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

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
            this._cd.detectChanges();
            const reader = new FileReader();
            reader.readAsText(fileInput.target.files[0]);
            reader.onload = (loadEvent: any) => {
                this.changedFile.emit(loadEvent.target.result);
            };
        }
    }

    constructor(private _cd: ChangeDetectorRef) { }

    ngOnInit() { }
}