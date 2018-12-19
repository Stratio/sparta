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
import { Store, select } from '@ngrx/store';
import { Subscription } from 'rxjs';

import * as workflowActions from './../../actions/workflow-list';
import * as fromRoot from './../../reducers';
import { ErrorMessagesService } from 'app/services';
import { FOLDER_SEPARATOR } from './../../workflow.constants';

@Component({
  selector: 'workflow-rename-modal',
  templateUrl: './workflow-rename.component.html',
  styleUrls: ['./workflow-rename.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WorkflowRenameModalComponent implements OnInit, OnDestroy {

  @Input() entityType = 'Group';
  @Input() entityName = '';
  @Output() onCloseRenameModal = new EventEmitter<string>();

  @ViewChild('renameForm') public renameForm: NgForm;

  public forceValidations = false;
  public name = '';
  public currentGroup = '';

  private openModal: Subscription;

  constructor(private _store: Store<fromRoot.State>,
    public errorsService: ErrorMessagesService) {
    _store.dispatch(new workflowActions.InitCreateGroupAction());
  }

  updateEntity() {
    if (this.renameForm.valid) {
      if (this.entityType === 'Group') {
        this._store.dispatch(new workflowActions.RenameGroupAction({
          oldName: this.entityName,
          newName: this.currentGroup + FOLDER_SEPARATOR + this.name
        }));
      } else {
        this._store.dispatch(new workflowActions.RenameWorkflowAction({
          oldName: this.entityName,
          newName: this.name
        }));
      }
    } else {
      this.forceValidations = true;
    }
  }

  ngOnInit() {
    if (this.entityType === 'Group') {
      const splittedName = this.entityName.split(FOLDER_SEPARATOR);
      this.currentGroup = splittedName.slice(0, splittedName.length - 1).join(FOLDER_SEPARATOR);
      this.name = splittedName[splittedName.length - 1];
    } else {
      this.name = this.entityName;
    }
    this._store.dispatch(new workflowActions.ResetModalAction());
    this.openModal = this._store.pipe(select(fromRoot.getShowModal)).subscribe((modalOpen) => {
      if (!modalOpen) {
        this.onCloseRenameModal.emit();
      }
    });
  }

  ngOnDestroy(): void {
    this.openModal && this.openModal.unsubscribe();
  }

}

