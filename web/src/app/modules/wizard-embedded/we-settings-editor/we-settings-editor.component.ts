/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { EditionConfigMode, WizardNode } from '@app/wizard/models/node';
import { cloneDeep as _cloneDeep } from 'lodash';
import { WizardService } from '@app/wizard/services/wizard.service';
import {ErrorMessagesService, InitializeSchemaService} from 'services';
import {Store} from '@ngrx/store';
import * as fromWizard from '@app/wizard/reducers';
import { Subject } from 'rxjs';
import * as wizardActions from '@app/wizard/actions/wizard';
import {HelpOptions} from '@app/shared/components/sp-help/sp-help.component';
import {takeUntil} from 'rxjs/operators';

@Component({
  selector: 'we-settings-editor',
  styleUrls: ['we-settings-editor.styles.scss'],
  templateUrl: 'we-settings-editor.template.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class WeSettingsEditorComponent implements OnInit, OnDestroy {
  @Input() parentNodeData: WizardNode;
  @Input() parentNode: EditionConfigMode;

  @Output() onCloseSettingsEdition = new EventEmitter();
  @Output() onSaveSettingsEdition = new EventEmitter();

  public formSettings: any = {};
  public formTemplateSettings: any;
  public formName: any;
  public formDescription: any;

  public isShowedInfo = true;
  public isShowedHelp = false;
  public validatedName = false;

  public helpOptions: Array<HelpOptions> = [];

  private _componentDestroyed = new Subject();
  private _parentWorkflowNodes: any;
  private _arrayToValidateName: Array<string>;

  constructor(
    private _store: Store<fromWizard.State>,
    private _wizardService: WizardService,
    private _initializeSchemaService: InitializeSchemaService,
    public errorsService: ErrorMessagesService
  ) {}

  ngOnInit(): void {
    this.formTemplateSettings = this._wizardService.getOutputs()[this.parentNodeData.classPrettyName];
    const editedNode = _cloneDeep(this.parentNodeData);
    this.formSettings.configuration = editedNode.configuration;
    this.formSettings.name = editedNode.name;
    this.formSettings.description = editedNode.description;

    this.formName = editedNode.name || '';
    this.formDescription = editedNode.description || '';

    this.helpOptions = this._initializeSchemaService.getHelpOptions(this.formTemplateSettings.properties);

    this._store.select(fromWizard.getWorkflowNodes)
      .pipe(takeUntil(this._componentDestroyed))
      .subscribe(nodes => {
        this._parentWorkflowNodes = nodes;
      });
    this._arrayToValidateName = this._parentWorkflowNodes
      .map(e => e.name)
      .filter(e => e !== this.parentNodeData.name);
  }

  weResetValidation() {
    this.validatedName =  (this._arrayToValidateName.indexOf(this.formName) > -1);
  }

  weSaveSettingsEdition() {
    this._store.dispatch(new wizardActions.SaveEntityErrorAction(false));
    this.onSaveSettingsEdition.emit({
      ...this.formSettings,
      name: this.formName,
      description: this.formDescription
    });
  }

  ngOnDestroy(): void {
    this._componentDestroyed.next();
    this._componentDestroyed.unsubscribe();

    console.info('DESTROY: WeSettingsEditorComponent');
  }
}
