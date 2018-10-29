/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Component, OnInit, Input, Output, EventEmitter, ChangeDetectorRef } from '@angular/core';
import { Store } from '@ngrx/store';
import * as fromParameters from '../../reducers';
import * as alertParametersActions from '../../actions/alert';
import * as environmentParametersActions from '../../actions/environment';

@Component({
  selector: 'environment-parameters',
  templateUrl: './environment-parameters.component.html',
  styleUrls: ['./environment-parameters.component.scss']
})
export class EnvironmentParametersComponent implements OnInit {

   public showConfigContext = false;
   public alertMessage = '';
   public showAlert: boolean;

   @Input() environmentParams: any;
   @Input() environmentContexts: string[];
   @Input() configContexts: string[];
   @Input() creationMode: boolean;

   @Output() addContext = new EventEmitter<any>();
   @Output() saveParam = new EventEmitter<any>();
   @Output() updateParam = new EventEmitter<any>();
   @Output() addEnvironmentParam =  new EventEmitter<any>();
   @Output() addEnvironmentContext =  new EventEmitter<any>();
   @Output() saveEnvironmentContext =  new EventEmitter<any>();
   @Output() deleteParam = new EventEmitter<any>();
   @Output() changeContext = new EventEmitter<any>();
   @Output() search: EventEmitter<{filter?: string, text: string}> = new EventEmitter<{filter?: string, text: string}>();
   @Output() deleteContext = new EventEmitter<any>();
   @Output() onDownloadParams = new EventEmitter<void>();
   @Output() onUploadParams = new EventEmitter<any>();
   @Output() emitAlert = new EventEmitter<any>();


  constructor(private _store: Store<fromParameters.State>, private _cd: ChangeDetectorRef) { }

   ngOnInit(): void {
      this._store.select(fromParameters.showAlert).subscribe(alert => {
         this.showAlert = !!alert;
         if (alert) {
            this.alertMessage = alert;
            this.closeAlert();
         } else {
            this._cd.markForCheck();
         }
      });
    }

   addNewContext(context) {
      this.addContext.emit(context);
   }

   saveParamContexts(param) {
      this.saveParam.emit(param);
   }

   addParam() {
      this.addEnvironmentParam.emit();
   }

   deleteEnvironmentParam(param) {
      this.deleteParam.emit(param);
   }

   onChangeContext(context) {
      this.changeContext.emit(context);
   }

   toggleConfigContext() {
      this.showConfigContext = !this.showConfigContext;
      this._store.dispatch(new alertParametersActions.HideAlertAction());
      if (!this.showConfigContext) {
         this._store.dispatch(new environmentParametersActions.ListEnvironmentParamsAction());
      }
   }

   saveContextList(context) {
      this.saveEnvironmentContext.emit(context);
   }

   onAddEnvironmentContext() {
      this.addEnvironmentContext.emit();
   }
   onDeleteContext(list) {
      this.deleteContext.emit(list);
   }

   closeAlert() {
      setTimeout(() => this._store.dispatch(new alertParametersActions.HideAlertAction()), 3000);
   }

   downloadParams() {
    this.onDownloadParams.emit();
    }

    uploadParams(params) {
        const reader: FileReader = new FileReader();
        reader.onload = (e) => {
            const loadFile: any = reader.result;
            try {
                const environment = JSON.parse(loadFile);
                if (!environment.parameterList || !environment.contexts) {
                    throw new Error('JSON Environment incorrect schema');
                } else {
                    this.onUploadParams.emit(environment);
                }
            } catch (err) {
                this.emitAlert.emit(err);
            }
        };
        reader.readAsText(params[0]);
    }
}
