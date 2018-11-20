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
  Output
} from '@angular/core';
import { WorkflowData } from '@app/wizard/wizard.models';
import { FloatingMenuModel } from '@app/shared/components/floating-menu/floating-menu.component';
import { WizardModalComponent } from '@app/wizard/components/wizard-modal/wizard-modal.component';
import { Router } from '@angular/router';
import { StModalService } from '@stratio/egeo';
import * as wizardActions from '@app/wizard/actions/wizard';
import { Store } from '@ngrx/store';
import * as fromWizard from '@app/wizard/reducers';

@Component({
  selector: 'we-header',
  styleUrls: ['we-header.styles.scss'],
  templateUrl: 'we-header.template.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class WeHeaderComponent implements OnInit, OnDestroy {
  @Input() workflowData: WorkflowData;
  @Input() nodeName: string;
  @Input() menuData: Array<FloatingMenuModel>;
  @Input() showDeleteButton: boolean;
  @Input() showEditAndCopyButton: boolean;
  @Input() isDirtyEditor: boolean;

  @Input() dataSetOriginal: any;
  @Input() dataSetInEdition: any;

  @Output() onSavePipelinesWorkflow = new EventEmitter<boolean>();
  @Output() selectedOption = new EventEmitter<any>();
  @Output() onZoomIn = new EventEmitter();
  @Output() onZoomOut = new EventEmitter();
  @Output() onCenter = new EventEmitter();
  @Output() onEditButton = new EventEmitter();
  @Output() onDuplicateNode = new EventEmitter();
  @Output() deleteSelection = new EventEmitter();
  @Output() onEditSettings = new EventEmitter();
  @Output() onShowInfo = new EventEmitter();

  private _modalConfiguration: any;

  constructor(
    private route: Router,
    private _modalService: StModalService,
    private _store: Store<fromWizard.State>) {}

  ngOnInit(): void {
    this._modalConfiguration = {
      modalTitle: 'You will lose your changes',
      outputs: {
        onCloseConfirmModal: this.onCloseConfirmationModal.bind(this)
      },
      maxWidth: 600
    };
  }

  public showConfirmModal(): void {
    if (this.isDirtyEditor) {
      this._modalService.show(this._modalConfiguration, WizardModalComponent);
    } else {
      this._store.dispatch(new wizardActions.HideEditorConfigAction());
    }
  }

  onCloseConfirmationModal(event: any) {
    this._modalService.close();
    if (event === '0') {
      this.onSavePipelinesWorkflow.emit(true);
      this._store.dispatch(new wizardActions.HideEditorConfigAction());
    } else if (event === '1') {
      this._store.dispatch(new wizardActions.HideEditorConfigAction());
    }
  }

  selectedMenuOption($event: any): void {
    this.selectedOption.emit($event);
  }


  ngOnDestroy(): void {
    console.info('DESTROY: WeHeaderComponent');
  }
}
