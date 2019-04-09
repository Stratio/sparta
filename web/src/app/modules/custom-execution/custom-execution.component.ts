/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  ViewChild,
  AfterViewInit
} from '@angular/core';
import { StHorizontalTab } from '@stratio/egeo';
import { FormGroup, FormControl } from '@angular/forms';

@Component({
  selector: 'custom-execution',
  styleUrls: ['custom-execution.component.scss'],
  templateUrl: 'custom-execution.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class CustomExecutionComponent implements AfterViewInit, OnInit, OnDestroy {

  @Input() executionContexts: any;
  @Input() workflowName: string;
  @Input() blockRunButton: boolean;
  @Input() showSheduler: boolean;
  @Input() workflowId: string;

  @Output() closeCustomExecution = new EventEmitter();
  @Output() executeWorkflow = new EventEmitter<any>();
  @Output() scheduleWorkflow = new EventEmitter<any>();

  @ViewChild('executionForm') executionForm: any;

  public categoriesToggle: any = {};
  public objectKeys = Object.keys;
  public environmentContext;
  public forceValidations = false;
  public customGroups = [];
  public form = {
    environment: undefined,
    customGroups: {},
    extraParams: {}
  };
  public selectedContexts: any = {};
  public scheduledFormControl = new FormControl();

  private groupsAndContextsDefault: any = [];
  private _nodeContainer;
  private _fn: any;


  public tabOptions: StHorizontalTab[] = [{
    id: 'parameters',
    text: 'Parameters'
  }, {
    id: 'schedule',
    text: 'Schedule config'
  }];
  public currentTab = this.tabOptions[0];

  constructor(private _cd: ChangeDetectorRef) {
    this._fn = this._calculatePosition.bind(this);
  }

  public sidebarPosition: number;

  ngOnInit(): void {
    this.groupsAndContextsDefault = this.executionContexts && this.executionContexts.groupsAndContexts.map(group => ({
      ...group, contexts: [{ ...group.parameterList, name: 'Default' }, ...group.contexts]
    }));

    if (this.groupsAndContextsDefault) {

      const envGroup = this.groupsAndContextsDefault.find(group => group.parameterList.name === 'Environment');
      if (envGroup) {
        this.environmentContext = this.getGroupContext(envGroup);
        this.form.environment = 'Default';
        this.changeContext('Default', 'Environment');
      }

      const custom = this.groupsAndContextsDefault.filter(group => group.parameterList.name !== 'Environment');
      this.customGroups = custom.map(group => {
        this.form.customGroups[group.parameterList.name] = 'Default';
        this.changeContext('Default', group.parameterList.name);
        return ({
          name: group.parameterList.name,
          context: this.getGroupContext(group)
        });
      });
    }
  }

  getGroupContext(group) {
    const { parameterList: { name }, contexts } = group;
    return contexts.map((context, i) => ({
      label: context.name,
      value: i === 0 ? name : context.name
    }));
  }

  execute() {
    if (this.executionForm.valid) {

      const paramConfig = {
        extraParams: Object.keys(this.form.extraParams)
          .map(param => ({
            name: param,
            value: this.form.extraParams[param]
          })),
        paramsLists: [
          ...Object.keys(this.form.customGroups).map(param => this.form.customGroups[param]),
          ...(this.form.environment ? [this.form.environment] : [])
        ]
      };

      if (this.showSheduler) {
        const config = {
          ...this.scheduledFormControl.value,
          executionContext: paramConfig,
          entityId: this.workflowId
        };
        this.scheduleWorkflow.emit(config);
      } else {
        this.executeWorkflow.emit(paramConfig);
      }

    } else {
      this.forceValidations = true;
    }
  }

  changeContext(event, groupName) {
    if (event === groupName) {
      event = 'Default';
    }
    const eventGroup = this.groupsAndContextsDefault.find(group => group.parameterList.name === groupName);
    this.selectedContexts[groupName] = eventGroup.contexts.find(context => context.name === event).parameters;
  }

  changedOption(event: StHorizontalTab) {
    this.currentTab = event;
  }

  ngAfterViewInit(): void {
    this._nodeContainer = document.getElementById('run-button');
    this._fn();
    window.addEventListener('resize', this._fn);
    setTimeout(() => {
      Object.keys(this.executionForm.controls).forEach(control => {
        this.executionForm.controls[control].markAsPristine();
      });
      this._cd.detectChanges();
    });

  }

  toggleCategory(category: string) {
    if (this.categoriesToggle[category]) {
      this.categoriesToggle[category] = false;
    } else {
      this.categoriesToggle[category] = true;
    }
  }

  ngOnDestroy(): void {
    window.removeEventListener('resize', this._fn);
    this.closeCustomExecution.emit();
  }

  private _calculatePosition() {
    const rect = this._nodeContainer.getBoundingClientRect();
    this.sidebarPosition = window.innerWidth - rect.left - rect.width;
    this._cd.detectChanges();
  }
}
