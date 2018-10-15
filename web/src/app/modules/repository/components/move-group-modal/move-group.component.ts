/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import {
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    Input,
    OnDestroy,
    OnInit,
    Output,
    ViewChild
} from '@angular/core';
import { NgForm } from '@angular/forms';
import { Store } from '@ngrx/store';
import { Subscription } from 'rxjs';

import * as workflowActions from './../../actions/workflow-list';
import * as fromRoot from './../../reducers';
import { FOLDER_SEPARATOR } from './../../workflow.constants';
import { Group } from './../../models/workflows';
import { take } from 'rxjs/operators';

@Component({
    selector: 'move-group-modal',
    templateUrl: './move-group.template.html',
    styleUrls: ['./move-group.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class MoveGroupModal implements OnInit, OnDestroy {

    @Input() workflow: any;
    @Input() currentGroup: string;
    @Output() onCloseMoveGroup = new EventEmitter<string>();

    @ViewChild('renameForm') public renameForm: NgForm;

    public forceValidations = false;
    public groups: any = [];

    public parentGroup = '';
    public workflowGroup = '';
    public selectedFolder = '';
    public disableButton = false;

    private group: Group;
    private openModal: Subscription;


    constructor(private _store: Store<fromRoot.State>) { }

    ngOnInit() {
        this._store.dispatch(new workflowActions.ResetModalAction());
        this._store.select(fromRoot.getAllGroups).pipe(take(1)).subscribe((groups: any) => {
            this.groups = groups;
        });

        if (this.workflow) {
            this._store.select(fromRoot.getCurrentGroupLevel).pipe(take(1)).subscribe((current: any) => {
                this.parentGroup = current.group.name;
                this.group = current.group;
            });
        } else {
            const split = this.currentGroup.split(FOLDER_SEPARATOR);
            this.parentGroup = split.slice(0, split.length - 1).join(FOLDER_SEPARATOR);
        }
        this.openModal = this._store.select(fromRoot.getShowModal).subscribe((modalOpen) => {
            if (!modalOpen) {
                this.onCloseMoveGroup.emit();
            }
        });
    }

    selectFolder(folder: string) {
        this.selectedFolder = folder;
    }

    updateFolder() {
        this.disableButton = true;
        if (this.workflow) {
            this._store.dispatch(new workflowActions.MoveWorkflowAction({
                groupTarget: this.selectedFolder, // get target id
                groupSourceId: this.group.id,
                workflowName: this.workflow
            }));
        } else {
            const split = this.currentGroup.split(FOLDER_SEPARATOR);
            this._store.dispatch(new workflowActions.RenameGroupAction({
                oldName: this.currentGroup,
                newName: this.selectedFolder + FOLDER_SEPARATOR + split[split.length - 1]
            }));
        }
    }

    ngOnDestroy(): void {
        this.openModal && this.openModal.unsubscribe();
    }

}

