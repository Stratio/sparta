/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import { StHorizontalTab } from '@stratio/egeo';
import { Store } from '@ngrx/store';

import * as viewerState from '@app/executions/workflow-detail-execution/reducers/index';

import { batchInputsObject, streamingInputsObject } from 'data-templates/inputs';
import { batchOutputsObject, streamingOutputsObject } from 'data-templates/outputs';
import { batchTransformationsObject, streamingTransformationsObject } from 'data-templates/transformations';
import { getOutputWriter } from 'data-templates/index';
import { SelectedNodeOutputNames } from '../../models';
import { WizardNode } from '@app/wizard/models/node';

@Component({
  selector: 'workflow-detail-modal',
  templateUrl: './workflow-detail-modal.component.html',
  styleUrls: ['./workflow-detail-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WorkflowDetailModalComponent implements OnInit {

  @Input() selectedNode: WizardNode;
  @Input() executionEngine: string;
  @Input() selectedNodeOutputNames: SelectedNodeOutputNames;
  @Output() closeModal = new EventEmitter();

  public accordionStates = [];
  public tabOptions: StHorizontalTab[] = [
    { id: 'global', text: 'Global' },
    { id: 'writer', text: 'Writers' }
  ];
  public currentStepTemplate: any;
  public tabSelectedOption: StHorizontalTab = this.tabOptions[0];
  public writerTemplatesMap: {[outputName: string]: any};

  constructor(private _store: Store<viewerState.State>) { }

  public ngOnInit(): void {
    if (this.selectedNodeOutputNames) {
      this.writerTemplatesMap = Object.keys(this.selectedNodeOutputNames).reduce((acc, outputName) => {
        this.accordionStates.push(false);
        acc[outputName] = getOutputWriter(this.selectedNodeOutputNames[outputName].classPrettyName, this.executionEngine);
        return acc;
      }, {});
    }
    if (!this.accordionStates.length) {
      this.tabOptions = this.tabOptions.filter(option => option.id !== 'writer');
    }

    if (this.selectedNode && this.executionEngine) {
      this._setSchemaTemplate(this.selectedNode);
    }
  }

  public changedOption(option: StHorizontalTab) {
    this.tabSelectedOption = option;
  }

  private _setSchemaTemplate(step: WizardNode) {
    if (!step) {
      this.currentStepTemplate = null;
      return;
    }

    const isBatch: boolean = this.executionEngine === 'Batch';

    switch (step.stepType) {
      case 'Input':
        this.currentStepTemplate = isBatch ? batchInputsObject[step.classPrettyName] : streamingInputsObject[step.classPrettyName];
        break;
      case 'Output':
        this.currentStepTemplate = isBatch ? batchOutputsObject[step.classPrettyName] : streamingOutputsObject[step.classPrettyName];
        break;
      case 'Transformation':
        this.currentStepTemplate = isBatch ? batchTransformationsObject[step.classPrettyName] : streamingTransformationsObject[step.classPrettyName];
        break;
    }
  }

  public toggleAccordion(index: number) {
    this.accordionStates = {
      ...this.accordionStates,
      [index]: !this.accordionStates[index]
    };
  }

}
