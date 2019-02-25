/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import {Component, Input, OnInit, Output, EventEmitter} from '@angular/core';
import {Parameter} from "@app/executions/execution-detail/types/execution-parameters";
import {ExecutionDetailInfo} from "@app/executions/execution-detail/models/execution-detail-info";
import {Info, ShowedActions} from "@app/executions/execution-detail/types/execution-detail";
import {Router} from "@angular/router";
import {Store} from "@ngrx/store";
import {State} from "@app/executions/executions-managing/reducers";
import * as executionDetailActions from '../../actions/execution-detail';

@Component({
  selector: 'workflow-execution-detail-info',
  templateUrl: './detail-info.component.html',
  styleUrls: ['./detail-info.component.scss']
})

export class DetailInfoComponent implements OnInit {

  @Input() executionDetailInfo: Info;
  @Input() showedActions: ShowedActions;
  @Output() onStopExecution = new EventEmitter<string>();
  @Output() onRerunExecution = new EventEmitter<string>();

  constructor(private route: Router, private _store: Store<State>) { }

  ngOnInit(): void {}

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

  reRunWorkflow(){
    this.onRerunExecution.emit(this.executionDetailInfo.marathonId.toString());
    this.route.navigate(['executions']);
  }

  goToWorkflow(ev, id) {
    ev.preventDefault();
    ev.stopPropagation();
    this.route.navigate(['workflow', id]);
  }

}
