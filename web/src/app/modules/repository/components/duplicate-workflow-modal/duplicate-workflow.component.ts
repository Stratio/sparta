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
import { take } from 'rxjs/internal/operators/take';

@Component({
   selector: 'workflow-duplicate-modal',
   templateUrl: './duplicate-workflow.component.html',
   styleUrls: ['./duplicate-workflow.component.scss'],
   changeDetection: ChangeDetectionStrategy.OnPush
})
export class DuplicateWorkflowComponent implements OnInit, OnDestroy {

   @Input() version: any;
   @Output() onCloseDuplicateModal = new EventEmitter<string>();

   @ViewChild('renameForm') public renameForm: NgForm;

   public forceValidations = false;
   public name = '';
   public currentGroup = '';
   public parentGroup: any;
   public groups: any = [];
   public selectedFolder = '';

   private openModal: Subscription;

   constructor(private _store: Store<fromRoot.State>, public errorsService: ErrorMessagesService) {
      _store.dispatch(new workflowActions.InitCreateGroupAction());
   }


   ngOnInit() {
      this._store.dispatch(new workflowActions.ResetModalAction());
      this.openModal = this._store.pipe(select(fromRoot.getShowModal))
        .subscribe((modalOpen) => {
         if (!modalOpen) {
            this.onCloseDuplicateModal.emit();
         }
      });
      this.currentGroup = this.version.group;
      this.name = this.version.name;
      this._store.pipe(select(fromRoot.getAllGroups))
        .pipe(take(1))
        .subscribe((groups: any) => {
         this.parentGroup = this.currentGroup;
         this.groups = groups;
      });
   }

   selectFolder(folder: string) {
      this.selectedFolder = folder;
   }

   duplicate() {
      this._store.dispatch(new workflowActions.DuplicateWorkflowAction({
         id: this.version.id,
         group: this.selectedFolder,
         tag: this.version.tag
      }));
   }

   ngOnDestroy(): void {
      this.openModal && this.openModal.unsubscribe();
   }

}

