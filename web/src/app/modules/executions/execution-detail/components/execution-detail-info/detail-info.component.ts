/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import {Component, Input, Output, OnChanges, EventEmitter, ChangeDetectorRef, ChangeDetectionStrategy} from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import {Router} from '@angular/router';
import {Store} from '@ngrx/store';
import { StModalService, StModalResponse, StModalButton } from '@stratio/egeo';
import { Subscription } from 'rxjs';

import {Info, ShowedActions} from '@app/executions/execution-detail/types/execution-detail';
import {State} from '@app/executions/executions-managing/executions-list/reducers';
import * as executionDetailActions from '../../actions/execution-detail';
import { take } from 'rxjs/operators';
import {ExecutionStatus, Engine, Execution} from '@models/enums';

@Component({
  selector: 'workflow-execution-detail-info',
  templateUrl: './detail-info.component.html',
  styleUrls: ['./detail-info.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class DetailInfoComponent implements OnChanges {

  @Input() executionDetailInfo: Info;
  @Input() showedActions: ShowedActions;
  @Input() lastError: any;
  @Output() onStopExecution = new EventEmitter<string>();
  @Output() onRerunExecution = new EventEmitter<string>();
  @Output() showErrorDetails = new EventEmitter<void>();

  private _modalSubscription: Subscription;
  private runExecutionModalHeader: string;
  private runExecutionModalOkButton: string;
  private runExecutionModalTitle: string;
  private runExecutionModalMessage: string;

  public isVisibleSparkUI = false;
  public isVisibleHistoryServer = false;
  public Engine = Engine;
  public ExecutionType = Execution;

  constructor(
    private route: Router,
    private _store: Store<State>,
    private _translate: TranslateService,
    private _modalService: StModalService,
    private _cd: ChangeDetectorRef
  ) {

    const runExecutionModalHeader = 'EXECUTIONS.RUN_EXECUTION_MODAL_HEADER';
    const runExecutionModalOkButton = 'EXECUTIONS.RUN_EXECUTION_MODAL_OK';
    const runExecutionModalTitle = 'EXECUTIONS.RUN_EXECUTION_MODAL_TITLE';
    const runExecutionModalMessage = 'EXECUTIONS.RUN_EXECUTION_MODAL_MESSAGE';


    this._translate.get([
      runExecutionModalHeader,
      runExecutionModalOkButton,
      runExecutionModalTitle,
      runExecutionModalMessage
    ]).subscribe(
      (value: { [key: string]: string }) => {
        this.runExecutionModalHeader = value[runExecutionModalHeader];
        this.runExecutionModalOkButton = value[runExecutionModalOkButton];
        this.runExecutionModalTitle = value[runExecutionModalTitle];
        this.runExecutionModalMessage = value[runExecutionModalMessage];
      });

  }

  ngOnChanges(): void {
    this.isVisibleSparkUI = this.executionDetailInfo.sparkURI && this.executionDetailInfo.status === ExecutionStatus.RunningStatus;
    this.isVisibleHistoryServer = this.executionDetailInfo.historyServerURI && this.executionDetailInfo.status !== ExecutionStatus.RunningStatus;
    this._cd.detectChanges();
  }

  selectGroupAction(event: string) {
    switch (event) {
      case 'workflow-archive':
        this._store.dispatch(new executionDetailActions.ArchiveExecutionAction(this.executionDetailInfo.marathonId));
        break;
      case 'workflow-unarchive':
        this._store.dispatch(new executionDetailActions.UnArchiveExecutionAction(this.executionDetailInfo.marathonId));
        break;
      case 'workflow-delete':
        this._store.dispatch(new executionDetailActions.DeleteExecutionAction(this.executionDetailInfo.marathonId));
        break;
    }
  }

  showSparkUI(url: string) {
    window.open(url, '_blank');
  }

  reRunWorkflow() {

    this._confirmRunExecution(this.runExecutionModalOkButton, () => {
      this.onRerunExecution.emit(this.executionDetailInfo.marathonId.toString());
      this.route.navigate(['executions']);
    });

  }

  private _confirmRunExecution(textOkButton, callback) {
    const buttons: StModalButton[] = [
      { label: 'Cancel', classes: 'button-secondary-gray', responseValue: StModalResponse.NO },
      { label: textOkButton, classes: 'button-primary', responseValue: StModalResponse.YES, closeOnClick: true }
    ];
    this._modalSubscription = this._modalService.show({
      messageTitle: this.runExecutionModalTitle,
      modalTitle: this.runExecutionModalHeader,
      buttons: buttons,
      maxWidth: 500,
      message: this.runExecutionModalMessage,
    }).pipe(take(1)).subscribe((response: any) => {
      if (response === 1) {
        this._modalService.close();
        this._modalSubscription.unsubscribe();
      } else if (response === 0) {
        this._modalSubscription.unsubscribe();
        callback.call(this);
      }
    });

  }

  goToWorkflow(ev, id) {
    ev.preventDefault();
    ev.stopPropagation();
    this.route.navigate(['workflow', id]);
  }

}
