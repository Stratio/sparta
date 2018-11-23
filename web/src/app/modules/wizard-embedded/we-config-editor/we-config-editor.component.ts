/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
  ChangeDetectionStrategy,
  Input,
  Component,
  OnDestroy,
  OnInit,
  Output,
  EventEmitter,
  ViewChild
} from '@angular/core';
import { EditionConfigMode, WizardEdge, WizardNode } from '@app/wizard/models/node';
import { WizardService } from '@app/wizard/services/wizard.service';
import { cloneDeep as _cloneDeep } from 'lodash';
import { NgForm } from '@angular/forms';
import { ErrorMessagesService, InitializeSchemaService } from 'services';
import {HelpOptions} from '@app/shared/components/sp-help/sp-help.component';
import {NodeHelpersService} from '@app/wizard-embedded/_services/node-helpers.service';
import {ValidateSchemaService} from '@app/wizard/services/validate-schema.service';

@Component({
  selector: 'we-config-editor',
  styleUrls: ['we-config-editor.styles.scss'],
  templateUrl: 'we-config-editor.template.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class WeConfigEditorComponent implements OnInit, OnDestroy {
  @Input() editedNode: WizardNode;
  @Input() editedNodeEditionMode: any;
  @Input() nodes: Array<WizardNode>;
  @Input() edges: Array<WizardEdge>;
  @Input() mlPipelineNode: EditionConfigMode;

  @Output() onCloseEdition = new EventEmitter();
  @Output() onSaveEdition = new EventEmitter();

  @ViewChild('pipelineForm') public pipelineForm: NgForm;

  public form: any = {};
  public formTemplate: any;
  public formName: any;
  public formDescription: any;
  public errorRepeatedNameValidation: boolean;
  public errorPatternNameValidation: boolean;
  public errorOutputColumn: boolean;

  public valueDictionary: any = {};
  public customValidators = {};

  public isShowedInfo = true;
  public isShowedHelp = false;

  public helpOptions: Array<HelpOptions> = [];

  private _fieldsToCompare: string[] = ['outputCol', 'predictionCol', 'probabilityCol', 'rawPredictionCol'];

  constructor(
    private _wizardService: WizardService,
    private _initializeSchemaService: InitializeSchemaService,
    private _nodeHelpers: NodeHelpersService,
    public errorsService: ErrorMessagesService,
    public _validateSchema: ValidateSchemaService
  ) {}

  ngOnInit(): void {
    this.formTemplate = this._wizardService.getPipelinesTemplates(this.editedNode.stepType)[this.editedNode.classPrettyName];
    this.formTemplate.properties = this.formTemplate.properties.map(e => {
      return (!e.propertyName.includes('(' + e.propertyId + ')')) ?
        {
          ...e,
          propertyName: e.propertyName + ' (' + e.propertyId + ')',
          tooltip: e.tooltip + ' Variable name: ' + e.propertyId + '.'
        } : e;
    });
    const editedNode = _cloneDeep(this.editedNode);
    this.form = editedNode.configuration;
    this.formName = editedNode.name || '';
    this.formDescription = editedNode.description || '';
    this.helpOptions = this._initializeSchemaService.getHelpOptions(this.formTemplate.properties);

    this.valueDictionary.externalInputs = this._nodeHelpers.getInputFields(this.editedNode);
    this.customValidations();
  }

  customValidations() {
    Object.keys(this.editedNode.configuration).map(key => {
      if (this._fieldsToCompare.indexOf(key) > -1 && this.editedNode.configuration[key]) {
        this.customValidators[key] = this.outputValidation.bind(this);
      }
    });
  }

  saveEdition(configuration: any) {
    if (!this.nameValidation() && !this.outputValidation(configuration)) {
      const editedNode = _cloneDeep({...this.editedNode, configuration});
      editedNode.name = this.formName;
      editedNode.description = this.formDescription;
      editedNode.hasErrors = this.pipelineForm.invalid;
      editedNode.errors = this._validateSchema.validateEntity(editedNode);
      this.onSaveEdition.emit(editedNode);
    }
  }

  nameValidation() {
    const repeatedNameValidation = this.repeatedNameValidation();
    this.errorRepeatedNameValidation = repeatedNameValidation;
    const patternNameValidation = this.patternNameValidation();
    this.errorPatternNameValidation = !patternNameValidation;
    return (repeatedNameValidation || !patternNameValidation);
  }

  repeatedNameValidation() {
    return !!this.nodes
      .map(e => {
        return (e.name !== this.editedNode.name && e.name === this.formName);
      })
      .filter(value => value)
      .length;
  }

  patternNameValidation() {
    return (/^[\w]+$/.test(this.formName));
  }

  outputValidation(value) {
    let output = '';
    const nodesToCheck = _cloneDeep(this.nodes);
    nodesToCheck.splice(this.nodes.indexOf(this.editedNode), 1);
    const namesToCompare = [];
    nodesToCheck.map(e => {
      Object.keys(e.configuration).map(key => {
        if ((this._fieldsToCompare.indexOf(key) > -1) && e.configuration[key]) {
          namesToCompare.push(e.configuration[key]);
        }
      });
    });
    this.valueDictionary.externalInputs.map(e => {
      return namesToCompare.push(e.label);
    });

    if (namesToCompare.indexOf(value) > -1) {
      output =  'Invalid field value. Exists inputs or outputs with the same name.';
    }

    this.errorOutputColumn = !!output;
    return output;
  }

  ngOnDestroy(): void {
    console.info('DESTROY: WeConfigEditorComponent');
  }
}
