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
