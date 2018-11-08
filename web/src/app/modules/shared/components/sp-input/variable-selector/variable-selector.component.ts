/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  Output,
  OnInit
} from '@angular/core';

@Component({
  selector: 'sp-variable-selector',
  templateUrl: './variable-selector.component.html',
  styleUrls: ['./variable-selector.component.scss']
})
export class VariableSelectorComponent implements OnInit {

    @Input() parameters;
    @Input() currentParameter;
    @Output() onSelectValue = new EventEmitter<string>();

    public sourceOptions: Array<any> = [
        {
            label: 'Global',
            value: 'Global'
        }, {
            label: 'User defined parameter',
            value: 'Undefined'
        }
    ];
    public variableSelector: Array<string> = [];
    public setParamValue = false;

    public paramValue = '';
    public paramDefault = '';
    public paramName: string;

    public sourceValue;

    ngOnInit(): void {
        if (this.parameters.environmentVariables.length) {
            this.sourceOptions = this.sourceOptions.concat({
                label: 'Environment',
                value: 'Environment'
            });
        }
        if (this.currentParameter) {
            if (this.parameters.customGroups) {
                this.sourceOptions = this.sourceOptions.concat(this.parameters.customGroups.filter(g => !g.parent).map(group => ({
                    label: group.name,
                    value: group.name
                })));
            }
            const existList = !!this.sourceOptions.find(option => option.value === this.currentParameter.paramType);
            this.sourceValue = existList ? this.currentParameter.paramType : 'Global';

            this.loadVariables(this.sourceValue);
            if (this.currentParameter.paramType === 'Undefined') {
                this.setParamValue = true;
                this.sourceValue = this.currentParameter.paramType;
                this.paramValue = this.currentParameter.value;
            } else if (this.currentParameter.paramType === 'Custom') {
                const value = this.currentParameter.value;
                this.sourceValue = value.substr(0, value.indexOf('.'));
                this.loadVariables(this.sourceValue);
                this.paramName = value;
            } else {
                this.paramName = existList ? this.currentParameter.value.substr(this.currentParameter.value.indexOf('.') + 1) : undefined;
            }
        }
    }

    loadVariables(groupType) {
        if (!this.currentParameter) {
            this.currentParameter = { value: '' };
        }
        const variableValue = groupType === 'undefined' ?
            this.currentParameter.value : this.currentParameter.value.substr(this.currentParameter.value.indexOf('.') + 1);
        this.setParamValue = false;
        this.paramName = undefined;
        this.paramValue = '';
        switch (groupType) {
            case 'Global': {
                this.variableSelector = this.parameters.globalVariables.map(variable => ({
                    label: variable.name,
                    value: groupType + '.' + variable.name
                }));
                this.paramDefault = this.parameters.globalVariables.find(g => g.name === variableValue) ? this.parameters.globalVariables.find(g => g.name === variableValue).value : '';
                break;
            }
            case 'Environment': {
                this.variableSelector = this.parameters.environmentVariables.map(variable => ({
                    label: variable.name,
                    value: groupType + '.' + variable.name
                }));
                this.paramDefault = this.parameters.environmentVariables.find(g => g.name === variableValue) ? this.parameters.environmentVariables.find(g => g.name === variableValue).value : '';
                break;
            }
            case 'Undefined': {
                this.setParamValue = true;
                this.paramDefault = '';
                break;
            }
            default: {
                const customGroup = this.parameters.customGroups.find(group => group.name === groupType);
                if (customGroup) {
                    this.variableSelector = customGroup.parameters.map(variable => ({
                        label: variable.name,
                        value: this.sourceValue + '.' + variable.name
                    }));
                }
            }
        }
    }

    loadVariablesValue(value) {
        const variableValue = this.sourceValue === 'undefined' ?
            this.currentParameter.value : value.substr(value.indexOf('.') + 1);
        switch (this.sourceValue) {
            case 'Global': {
                this.paramDefault = this.parameters.globalVariables.find(g => g.name === variableValue) ? this.parameters.globalVariables.find(g => g.name === variableValue).value : '';
                break;
            }
            case 'Environment': {
                this.paramDefault = this.parameters.environmentVariables.find(g => g.name === variableValue) ? this.parameters.environmentVariables.find(g => g.name === variableValue).value : '';
                break;
            }
            case 'Undefined': {
                this.setParamValue = true;
                this.paramDefault = '';
                break;
            }
        }
    }

    cancel() {
        this.onSelectValue.emit();
    }

    saveParameterSelection() {
        if (this.setParamValue) {
            this.onSelectValue.emit(this.paramValue);
        } else {
            this.onSelectValue.emit(this.paramName);
        }
    }
}
