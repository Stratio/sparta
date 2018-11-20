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

@Component({
  selector: 'we-config-editor',
  styleUrls: ['we-config-editor.styles.scss'],
  templateUrl: 'we-config-editor.template.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class WeConfigEditorComponent implements OnInit, OnDestroy {
  @Input() editedNode: WizardNode;
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
    public errorsService: ErrorMessagesService
  ) {}

  ngOnInit(): void {
    this.formTemplate = this._wizardService.getPipelinesTemplates(this.editedNode.stepType)[this.editedNode.classPrettyName];
    const editedNode = _cloneDeep(this.editedNode);
    this.form = editedNode.configuration;
    this.formName = editedNode.name || '';
    this.formDescription = editedNode.description || '';
    this.helpOptions = this._initializeSchemaService.getHelpOptions(this.formTemplate.properties);

    this.processInputs();
    this.customValidations();
  }

  customValidations() {
    Object.keys(this.editedNode.configuration).map(key => {
      if (this._fieldsToCompare.indexOf(key) > -1 && this.editedNode.configuration[key]) {
        this.customValidators[key] = this.outputValidation.bind(this);
      }
    });
  }

  processInputs() {
    if (this.mlPipelineNode.schemas) {
      const mlPipelineNodeData = _cloneDeep(this.mlPipelineNode.editionType.data);
      const mlPipelineNodeInputs = _cloneDeep(this.mlPipelineNode.schemas.inputs);
      if (mlPipelineNodeInputs && Array.isArray(mlPipelineNodeInputs) && mlPipelineNodeInputs.length) {
        if (mlPipelineNodeInputs.length === 1) {
          const inputFields = mlPipelineNodeInputs.pop().result.schema.fields;
          this.valueDictionary.externalInputs = inputFields.map(field => {
            return {
              label: field.name,
              value: field.name
            };
          });
        } else {
          console.error(new Error('Found more than 1 input from step ' + mlPipelineNodeData.name));
        }
      } else {
        console.error(new Error('No inputs found in step ' + mlPipelineNodeData.name));
      }
    }

    const prevOutputs = this.processPrevOutputs(this.editedNode);
    if (prevOutputs.length) {
      const newExternalInputs = prevOutputs.map(e => {
        return {
          label: e,
          value: e
        };
      });
      if (this.valueDictionary.externalInputs) {
        this.valueDictionary.externalInputs = this.valueDictionary.externalInputs.concat(newExternalInputs).sort(compare);
      } else {
        this.valueDictionary.externalInputs = newExternalInputs.sort(compare);
      }
    }

    if (!this.valueDictionary.externalInputs) {
      this.valueDictionary.externalInputs = [];
    }

    function compare(a, b) {
      if (a.label < b.label) {
        return -1;
      }
      if (a.label > b.label) {
        return 1;
      }
      return 0;
    }
  }

  processPrevOutputs(node: WizardNode, output: string[] = []): string[] {
    const fromEdge = this.edges.find(e => e.destination === node.name);
    if (fromEdge) {
      const prevNode = this.nodes.find(e => e.name === fromEdge.origin);
      output.push(prevNode.configuration.outputCol);
      this.processPrevOutputs(prevNode, output);
    }
    return output;
  }

  saveEdition(configuration: any) {
    if (!this.nameValidation() && !this.outputValidation(configuration)) {
      const editedNode = _cloneDeep({...this.editedNode, configuration});
      editedNode.name = this.formName;
      editedNode.description = this.formDescription;
      editedNode.hasErrors = this.pipelineForm.invalid;
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
