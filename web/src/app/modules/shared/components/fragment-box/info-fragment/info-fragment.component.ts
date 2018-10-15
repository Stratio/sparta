/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, OnDestroy, OnInit, Input, ChangeDetectionStrategy, HostListener } from '@angular/core';
import { streamingInputsObject, batchInputsObject } from 'data-templates/inputs';
import { streamingOutputsObject, batchOutputsObject } from 'data-templates/outputs';
import { streamingTransformationsObject, batchTransformationsObject } from 'data-templates/transformations';
import { Engine } from '@models/enums';

@Component({
    selector: 'info-fragment',
    styleUrls: ['info-fragment.styles.scss'],
    templateUrl: 'info-fragment.template.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class InfoFragmentComponent implements OnInit, OnDestroy {

    @Input() templateType = Engine.Streaming;
    @Input() stepType: string;
    @Input() classPrettyName: string;

    public fragmentInfo = '';
    public showInfo = false;

    @HostListener('document:click') onClick() {
        this.showInfo = false;
    }

    ngOnInit(): void {
        const inputsObject = this.templateType === 'Streaming' ? streamingInputsObject : batchInputsObject;
        const transformationsObject = this.templateType === 'Streaming' ? streamingTransformationsObject : batchTransformationsObject;
        const outputsObject = this.templateType === 'Streaming' ? streamingOutputsObject : batchOutputsObject;
        switch (this.stepType) {
            case 'input':
                this.fragmentInfo = inputsObject[this.classPrettyName] ? inputsObject[this.classPrettyName].description : '';
                break;
            case 'output':
                this.fragmentInfo = outputsObject[this.classPrettyName] ? outputsObject[this.classPrettyName].description : '';
                break;
            case 'transformation':
                this.fragmentInfo = transformationsObject[this.classPrettyName] ? transformationsObject[this.classPrettyName].description : '';
                break;
        }
    }

    constructor() { }

    showFragmentTypeInfo(event: any) {
        event.stopPropagation();
        this.showInfo = true;
    }


    public ngOnDestroy(): void {

    }
}
