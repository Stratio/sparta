/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import {
   ChangeDetectionStrategy,
   Component,
   OnDestroy,
   OnInit,
   ViewChild,
   ViewContainerRef
} from '@angular/core';
import { Store } from '@ngrx/store';
import { ChangeDetectorRef } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { NgForm, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { StModalService } from '@stratio/egeo';
import { TranslateService } from '@ngx-translate/core';

import * as fromRoot from './reducers';
import * as environmentActions from './actions/environment';
import { generateJsonFile } from '@utils';
import { ImportEnvironmentModalComponent } from './components/import-environment-modal/import-environment-modal.component';
import { BreadcrumbMenuService } from 'services';
import { ErrorMessagesService } from 'app/services/error-messages.service';
import { EnvironmentListResponse, Environment } from 'app/models/environment';

@Component({
   selector: 'environment',
   templateUrl: './environment.template.html',
   styleUrls: ['./environment.styles.scss'],
   changeDetection: ChangeDetectionStrategy.OnPush
})
export class EnvironmentComponent implements OnInit, OnDestroy {

   @ViewChild('environmentModal', { read: ViewContainerRef }) target: any;
   @ViewChild('environmentsForm') public environmentsForm: NgForm;

   public breadcrumbOptions: any = [];

   public item: any = {};
   public internalControl: FormGroup;
   public items: FormArray;
   public filter = '';
   public model: Array<Environment> = [];
   public forceValidations = false;
   public environmentList: Subscription;
   public filteredValue = '';
   public menuOptions = [{
      name: 'Export environment data',
      value: 'export'
   },
   {
      name: 'Import environment data',
      value: 'import'
   }];
   public fields = [
      {
         'propertyId': 'name',
         'propertyName': '_KEY_',
         'propertyType': 'text',
         'width': 3
      },
      {
         'propertyId': 'value',
         'propertyName': '_VALUE_',
         'propertyType': 'text',
         'width': 6
      }];

   public duplicated: Array<string> = [];

   ngOnInit() {

      this.items = this.formBuilder.array([]);
      this.internalControl = new FormGroup({
         variables: this.items
      });

      for (const field of this.fields) {
         this.item[field.propertyId] = ['', Validators.required];
      }

      this._modalService.container = this.target;
      this._store.dispatch(new environmentActions.ListEnvironmentAction());
      this.environmentList = this._store.select(fromRoot.getEnvironmentList).subscribe((envList: EnvironmentListResponse) => {
         this.model = envList.variables;
         this.initForm(this.model);
         this._cd.detectChanges();

         setTimeout(() => {
            if (this.internalControl.invalid) {
               this.forceValidations = true;
               this.internalControl.markAsPristine();
               this._cd.detectChanges();
            }
         });
      });
   }

   initForm(variables: Array<Environment>) {
      if (variables && Array.isArray(variables) && variables.length) {
         this.items.controls = [];
         for (const value of variables) {
            const item: any = {};
            for (const field of this.fields) {
               item[field.propertyId] = [value[field.propertyId], Validators.required];
            }
            const form: FormGroup = this.formBuilder.group(item);
            this.items.push(form);
         }
      } else {
         this.items.controls = [];
      }
      this.internalControl.markAsPristine();
      this.getDuplicated();
      this._cd.detectChanges();
   }

   isHidden(value: Environment) {
      return !(value.name.toLowerCase().indexOf(this.filter) > -1);
   }

   downloadVariables() {
      generateJsonFile(new Date().getTime().toString(), this.internalControl.value.variables);
   }

   uploadVariables(event: any) {
      const reader = new FileReader();
      reader.readAsText(event[0]);
      reader.onload = (loadEvent: any) => {
         try {
            this.initForm([...this.internalControl.value.variables].concat(JSON.parse(loadEvent.target.result)));
         } catch (error) {
            console.log('Parse error');
         }
      };
   }

   getDuplicated() {
      this.duplicated = this.internalControl.value.variables.map((variable: any) => variable.name)
         .filter((variable: string, index: number, variables: Array<string>) => variable.length && variables.indexOf(variable) !== index);
   }

   addItem(): void {
      this.internalControl.markAsDirty();
      this.items.push(this.formBuilder.group(this.item));
      this.filter = '';
   }

   deleteItem(i: number) {
      this.internalControl.markAsDirty();
      this.items.removeAt(i);
   }

   updateFilter() {
      this.onSearchResult(this.filteredValue);
   }

   saveEnvironment() {
      this.getDuplicated();
      if (!this.duplicated.length && this.internalControl.valid) {
         this.forceValidations = false;
         this._store.dispatch(new environmentActions.SaveEnvironmentAction(this.internalControl.value));
         this.internalControl.markAsPristine();
      } else {
         this.forceValidations = true;
      }
   }

   selectedMenuOption(event: any) {
      if (event.value === 'import') {
         this.importEnvironmentData();
      } else {
         this._store.dispatch(new environmentActions.ExportEnvironmentAction());
      }
   }

   public importEnvironmentData(): void {
      this._modalService.show({
         modalTitle: 'Import environment data',
         outputs: {
            onCloseImportModal: this.onCloseImportModal.bind(this)
         },
      }, ImportEnvironmentModalComponent);
   }

   onSearchResult($event: any) {
      this.filter = $event.text.toLowerCase();
   }

   public onCloseImportModal() {
      this.forceValidations = true;
      this._modalService.close();
   }

   constructor(private _store: Store<fromRoot.State>, private _cd: ChangeDetectorRef,
      private _modalService: StModalService, private translate: TranslateService, private formBuilder: FormBuilder,
      public breadcrumbMenuService: BreadcrumbMenuService, public errorsService: ErrorMessagesService) {
      this.breadcrumbOptions = breadcrumbMenuService.getOptions();
   }

   ngOnDestroy(): void {
      this.environmentList && this.environmentList.unsubscribe();
   }

}
