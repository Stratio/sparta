/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import {
   ChangeDetectionStrategy,
   ChangeDetectorRef,
   Component,
   OnInit,
   OnDestroy
} from '@angular/core';
import { Store } from '@ngrx/store';
import { Subscription, Observable } from 'rxjs';

import * as workflowActions from './../../actions/workflow-list';
import { State, getSearchQuery, getCurrentGroupLevel } from './../../reducers';

import { DEFAULT_FOLDER, FOLDER_SEPARATOR } from './../../workflow.constants';
import { WorkflowBreadcrumbItem } from './workflow-breadcrumb/workflow-breadcrumb.model';


@Component({
   selector: 'workflow-table-header-container',
   template: `
        <workflow-table-header [levelOptions]="levelOptions"
            [searchValue]="searchValue$ | async"
            (onSearch)="onSearch($event)"
            (changeFolder)="changeFolder($event)"></workflow-table-header>
    `,
   changeDetection: ChangeDetectionStrategy.OnPush
})

export class WorkflowTableHeaderContainer implements OnInit, OnDestroy {

   public searchValue$: Observable<string>;
   public levelOptions: Array<WorkflowBreadcrumbItem> = [];
   private _currentLevelSubscription: Subscription;

   constructor(private _store: Store<State>, private _cd: ChangeDetectorRef) { }

   ngOnInit(): void {
      this.searchValue$ = this._store.select(getSearchQuery);
      this._currentLevelSubscription = this._store.select(getCurrentGroupLevel).subscribe((levelGroup: any) => {
         const level = levelGroup.group;
         const levelOptions = [{
            icon: 'icon-home',
            label: ''
         }];

         let levels = [];
         if (level.name === DEFAULT_FOLDER) {
            levels = levelOptions; // set default folder as current directory
         } else {
            levels = levelOptions.concat(level.name.split(FOLDER_SEPARATOR).slice(2)
               .map(option => ({
                  icon: '',
                  label: option
               })));
         }
         this.levelOptions = levelGroup.workflow && levelGroup.workflow.length ? [...levels, {
            icon: '',
            label: levelGroup.workflow
         }] : levels;
         this._cd.markForCheck();
      });
   }

   changeFolder(position: number) {
      if (position + 1 === this.levelOptions.length) {
        return;
      }
      const level = position === 0 ? DEFAULT_FOLDER : DEFAULT_FOLDER +
         FOLDER_SEPARATOR + this.levelOptions.slice(1, position + 1).map(option => option.label).join(FOLDER_SEPARATOR);
      this._store.dispatch(new workflowActions.ChangeGroupLevelAction(level));
   }

   onSearch(event: string) {
       this._store.dispatch(new workflowActions.SearchCurrentFolderAction(event));
   }

   ngOnDestroy() {
      this._currentLevelSubscription && this._currentLevelSubscription.unsubscribe();
   }

}
