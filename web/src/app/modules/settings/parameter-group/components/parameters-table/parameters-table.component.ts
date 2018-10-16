/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Component, Input, ChangeDetectorRef, OnInit, EventEmitter, Output, OnChanges, OnDestroy } from '@angular/core';
import { GlobalParam } from '@app/settings/parameter-group/models/globalParam';
import { FormGroup, FormBuilder, FormControl, FormArray, Validators } from '@angular/forms';

@Component({
   selector: 'parameters-table',
   styleUrls: ['parameters-table.component.scss'],
   templateUrl: 'parameters-table.component.html'
})

export class ParametersTableComponent implements OnInit, OnChanges, OnDestroy {
   @Input() inputParameters: GlobalParam[];
   @Input() inputList: any[];

   @Input() contextList: any[];
   @Input() withContext: boolean;
   @Input() configContext: boolean;
   @Input() listMode: boolean;
   @Input() creationMode: boolean;

   @Output() onAddNewContext = new EventEmitter<any>();
   @Output() onSaveParamContexts = new EventEmitter<any>();
   @Output() onUpdateElement = new EventEmitter<any>();
   @Output() onNavigate = new EventEmitter<any>();
   @Output() onSaveParamList = new EventEmitter<any>();
   @Output() deleteParam = new EventEmitter<any>();
   @Output() deleteList = new EventEmitter<any>();
   @Output() changeContext = new EventEmitter<any>();


   public myForm: FormGroup;
   public myListForm: FormGroup;
   public contexts: FormArray;
   public header = ['name', 'value'];
   public selectedParameter = null;
   public selectedParameterList = null;
   public showContextMenu = false;
   public checked = false;
   public showOption = false;


   public selectedContextList = [];
   public checkedContextList = [];
   public uncheckedContextList = [];


   public contextOptionActive = false;
   public contextNameList = [];
   public selectedContext = 'default';
   public defaultValue = '';

   constructor(private fb: FormBuilder) {
      this.myForm = fb.group({
         name: new FormControl('', [Validators.required]),
         value: new FormControl('', [Validators.required]),
         contexts: fb.array([])
      });
      this.myListForm = fb.group({
         name: new FormControl('', [Validators.required])
      });
      // this.myForm.valueChanges.subscribe(res => this.onUpdateValue(res));
   }

   ngOnInit() {
      this.selectedContextList = this.withContext && this.contextList ? this.contextList.map(context => ({ name: context.name, checked: false, value: '' })) : [];
      this.uncheckedContextList = this.selectedContextList.filter(e => !e.checked);
      if (this.contextList) {
         this.contextNameList = this.contextList.map(context => context.name);
      }
   }

   ngOnChanges(changes: any) {
      if (changes.creationMode && changes.creationMode.currentValue && changes.creationMode.currentValue !== changes.creationMode.previousValue) {
         this.selectedParameter = null;
         this.selectedParameterList = null;
         changes.inputParameters && changes.inputParameters.currentValue.length ?
         this.selectedRow({ name: '', value: '', contexts: [] }) :
         this.selectedList({ name: '' });
      }
   }

   navigateList(list, ev) {
      if (!this.configContext) {
         ev.preventDefault();
         ev.stopPropagation();
         this.onNavigate.emit(list);
      }

      this.showOption = false;
   }

   selectedList(list: any) {
      this.selectedParameterList = this.selectedParameterList && this.selectedParameterList.name === list.name ? null : list;
      if (this.selectedParameterList) {
         this.myListForm.controls['name'].setValue(list.name);
         this.myListForm.markAsPristine();
         this.showOption = false;
      }

   }
   selectedRow(parameter: any) {
      this.showContextMenu = false;
      this.selectedParameter = this.selectedParameter && this.selectedParameter.name === parameter.name ? null : parameter;
      if (this.selectedParameter) {
         this.showOption = false;

         this.selectedContextList = this.withContext && this.contextList ?
            this.contextList.map(context => ({
               name: context.name,
               checked: parameter.contexts.map(c => c.name).includes(context.name),
               value: this.selectedParameter.defautl || '',
               id: context.id
            })) : [];
         this.contexts = this.myForm.get('contexts') as FormArray;
         if (this.selectedParameter) {
            this.myForm.controls['name'].setValue(parameter.name);
            this.myForm.controls['value'].setValue(parameter.defaultValue || parameter.value);

            this.contexts.controls = [];
            if (parameter.contexts) {
               parameter.contexts.map(context => this.contexts.push(this.fb.group({
                  name: new FormControl(context.name),
                  value: new FormControl(context.value, [Validators.required]),
                  id: new FormControl(context.id)
               })));
            }
            this.myForm.markAsPristine();
         }
         this.uncheckedContextList = this.selectedContextList.filter(e => !e.checked);

      }

   }

   onContextMenu() {
      this.contextNameList = [
         { label: 'default' , value: 'default', test: 'default'},
         ...this.contextList.map(context => ({ label: context.name , value: context.name, test: context.name}))
      ];
      this.contextOptionActive = !this.contextOptionActive;
   }

   onChangeContext(context) {
      this.selectedContext = context.value;
      this.changeContext.emit(this.selectedContext);
   }

   onDeleteList(list) {
      this.deleteList.emit(list);
   }

   onSave() {
      this.onSaveParamContexts.emit({ value: this.myForm.value, name: this.selectedParameter.name });
      this.myForm.markAsPristine();
      this.showOption = false;
   }

   onSaveList() {
      const list = this.selectedParameterList && this.selectedParameterList.id ? this.selectedParameterList : this.inputList[0];
      this.onSaveParamList.emit({ value: this.myListForm.value, list });
      this.myListForm.markAsPristine();
      this.showOption = false;
   }

   onOption() {
      this.showOption = !this.showOption;
   }

   onDeleteParameter(parameter) {
      this.deleteParam.emit(parameter);
   }

   onSelectContext() {
      this.uncheckedContextList = this.selectedContextList.filter(e => !e.checked);
      this.showContextMenu = !this.showContextMenu;
      this.showOption = false;
   }

   checkValue(event) {
      this.selectedContextList[this.selectedContextList.findIndex(context => context.name === event.value)].checked = event.checked;
      this.checked = this.selectedContextList.filter(c => c.checked).length > 0;
   }


   addContext() {
      this.checkedContextList = this.selectedContextList.filter(context => context.checked).map(context => ({ ...context, value: this.selectedParameter.value }));
      this.contexts = this.myForm.get('contexts') as FormArray;
      const actualContexts = this.contexts.value.map(context => context.name);
      const actualChecked = this.checkedContextList.map(context => context.name);
      const added = this.checkedContextList.filter(context => actualContexts.indexOf(context.name) === -1);
      const deleted = this.contexts.value.filter(context => actualChecked.indexOf(context.name) === -1);

      deleted.map(context => {
         const index = this.contexts.value.map(c => c.name).indexOf(context.name);
         this.contexts.removeAt(index);

      });
      added.map(context => this.contexts.push(this.fb.group({
         name: new FormControl(context.name),
         value: new FormControl(context.value, [Validators.required]),
         id: new FormControl(context.id)
      })));

      this.uncheckedContextList = this.selectedContextList.filter(c => !c.checked);

      this.showContextMenu = false;
      this.myForm.markAsDirty();
   }

   ngOnDestroy() {
      this.selectedParameter = null;
      this.contextOptionActive = false;
   }

   onDeleteContext(context) {
      this.selectedContextList = this.selectedContextList.map(c => c.name !== context ? c : { ...c, checked: false });
      this.uncheckedContextList = this.selectedContextList.filter(c => !c.checked);
      const index = this.contexts.value.map(c => c.name).indexOf(context);
      this.contexts.removeAt(index);
      this.myForm.markAsDirty();
   }

}
