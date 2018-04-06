/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { ChangeDetectionStrategy, Component, EventEmitter, Output } from '@angular/core';
import { Store } from '@ngrx/store';

import * as fromRoot from './../../reducers';
import * as environmentActions from './../../actions/environment';

@Component({
    selector: 'import-environment-modal',
    templateUrl: 'import-environment-modal.template.html',
    styleUrls: ['import-environment-modal.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class ImportEnvironmentModalComponent {

    @Output() onCloseImportModal = new EventEmitter();

    public loaded = false;
    public environment: any;
    public fileValidationError = false;

    ngOnInit(): void {}

    changedFile(event: any) {
        this.fileValidationError = false;
        try {
            const content = JSON.parse(event);
            this.loaded = true;
            this.environment = event;
        } catch (e) {
            this.fileValidationError = true;
        }

    }

    importEnvironment() {
        try {
            this.store.dispatch(new environmentActions.ImportEnvironmentAction(JSON.parse(this.environment)));
            this.onCloseImportModal.emit();
        } catch (error) {

        }
    }

    close() {
        this.onCloseImportModal.emit();
    }

    constructor(private store: Store<fromRoot.State>) {}
}
