/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { Store, select } from '@ngrx/store';

import { Subject, Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { WizardService } from '@app/wizard/services/wizard.service';

import * as fromWizard from './../../reducers';
import * as debugActions from './../../actions/debug';

import { StHorizontalTab } from '@stratio/egeo';
import { WizardAnnotation } from '@app/shared/wizard/components/wizard-annotation/wizard-annotation.model';

@Component({
  selector: 'wizard-details',
  templateUrl: './wizard-details.template.html',
  styleUrls: ['./wizard-details.styles.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WizardDetailsComponent implements OnInit, OnDestroy {

  @Input() entityData: any;
  public templates: any = {};
  public config = {};
  public genericError: any;
  public validations: any;
  public tabItems: Array<StHorizontalTab> = [
    {
      text: 'Overview',
      id: 'overview'
    }, {
      text: 'Notes',
      id: 'notes'
    }
  ];
  public activeTabItem = this.tabItems[0];
  public annotations$: Observable<WizardAnnotation[]>;
  public selectedNodeAnnotations$: Observable<WizardAnnotation[]>;

  private _componentDestroyed = new Subject();

  constructor(
    private _cd: ChangeDetectorRef,
    private wizardService: WizardService,
    private _store: Store<fromWizard.State>) { }

  ngOnInit() {
    this.annotations$ = this._store.pipe(select(fromWizard.getAnnotationsWithNumbers));
    this.selectedNodeAnnotations$ = this._store.pipe(select(fromWizard.getSelectedNodeAnnotations));
    this._store.pipe(select(fromWizard.getWorkflowType))
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((workflowType: string) => {
        this.wizardService.workflowType = workflowType;
        this.templates = {
          Input: this.wizardService.getInputs(),
          Output: this.wizardService.getOutputs(),
          Transformation: this.wizardService.getTransformations(),
        };
        this._cd.markForCheck();
      });

    this._store.pipe(select(fromWizard.getDebugResult))
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe((debugResult: any) => {
        this.genericError = debugResult && debugResult.genericError ? debugResult.genericError : null;
        this._cd.markForCheck();
      });

    this._store.pipe(select(fromWizard.getValidationErrors))
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe(validations => {
        this.validations = validations;
        this._cd.markForCheck();
      });
  }

  public changedTabOption(event: StHorizontalTab) {
    this.activeTabItem = event;
  }

  showConsole(tab: string) {
    this._store.dispatch(new debugActions.ShowDebugConsoleAction(tab));
  }

  ngOnDestroy(): void {
    this._componentDestroyed.next();
    this._componentDestroyed.unsubscribe();
  }
}
