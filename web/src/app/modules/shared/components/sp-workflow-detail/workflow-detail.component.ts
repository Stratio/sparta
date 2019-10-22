/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import {
  Component,
  ChangeDetectionStrategy,
  Output,
  EventEmitter, Input,
  OnInit
} from '@angular/core';

import { StHorizontalTab } from '@stratio/egeo';
import { QualityRule } from '@app/executions/models';

import { batchInputsObject, streamingInputsObject } from 'data-templates/inputs';
import { batchOutputsObject, streamingOutputsObject } from 'data-templates/outputs';
import { batchTransformationsObject, streamingTransformationsObject } from 'data-templates/transformations';
import { Engine, CITags } from '@models/enums';
import {WizardNode} from '@app/wizard/models/node';
import {SelectedNodeOutputNames} from '@app/shared/components/sp-workflow-detail/models';
import {Router} from '@angular/router';
@Component({
  selector: 'workflow-detail',
  templateUrl: './workflow-detail.template.html',
  styleUrls: ['./workflow-detail.styles.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WorkflowDetailComponent implements OnInit {

  @Output() onSelectEdge = new EventEmitter<any>();
  @Output() onOpenNode = new EventEmitter<WizardNode>();
  @Output() closeModal = new EventEmitter();

  @Input() id: string;
  @Input() workflowName: string;
  @Input() executionEngine: string;
  @Input() ciCdLabel: string;
  @Input() edges: any = [];
  @Input() nodes: any = [];
  @Input() uiSettings: any;
  @Input() filteredQualityRules: Array<QualityRule> = [];
  @Input() qualityRulesCount: Array<number>;
  @Input() qualityRulesStatus: Array<boolean>;
  @Input() showConfigModal: boolean;
  @Input() selectedNodeOutputNames: Array<SelectedNodeOutputNames>;

  public currentStepTemplate: any;
  public selectedQualityRules = false;
  public selectedStep: any;
  public keys = Object.keys;
  public tabOptions: StHorizontalTab[] = [
    { id: 'global', text: 'Global' },
    { id: 'writer', text: 'Writer' }
  ];
  public tabSelectedOption: StHorizontalTab = this.tabOptions[0];
  public CITags = CITags;
  public detailContext: string;

  constructor(private router: Router) { }

  ngOnInit() {
    const path = this.router.url.split('/');
    this.detailContext = path[2] === 'execution' ? 'execution' : 'repo';
  }

  selectStep(step: any) {
    this.selectedStep = step;
    this.tabSelectedOption = this.tabOptions[0];
    this.selectedQualityRules = false;

    if (step.executionEngine === Engine.Batch) {
      switch (step.stepType) {
        case 'Input':
          this.currentStepTemplate = batchInputsObject[step.classPrettyName];
          break;
        case 'Output':
          this.currentStepTemplate = batchOutputsObject[step.classPrettyName];
          break;
        case 'Transformation':
          this.currentStepTemplate = batchTransformationsObject[step.classPrettyName];
          break;
      }
    } else {
      switch (step.stepType) {
        case 'Input':
          this.currentStepTemplate = streamingInputsObject[step.classPrettyName];
          break;
        case 'Output':
          this.currentStepTemplate = streamingOutputsObject[step.classPrettyName];
          break;
        case 'Transformation':
          this.currentStepTemplate = streamingTransformationsObject[step.classPrettyName];
          break;
      }
    }
  }

  changedOption(option: StHorizontalTab) {
    this.tabSelectedOption = option;
  }

}
