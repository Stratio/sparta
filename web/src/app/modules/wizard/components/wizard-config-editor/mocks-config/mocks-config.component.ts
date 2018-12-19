/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import {
   Component, OnInit, OnDestroy, ChangeDetectionStrategy, Input, ChangeDetectorRef
 } from '@angular/core';
import { Store, select } from '@ngrx/store';
 import { Subscription } from 'rxjs';

 import { ErrorMessagesService } from 'services';
 import * as fromWizard from './../../../reducers';
 import * as debugActions from './../../../actions/debug';
 import { getDebugFile } from './../../../reducers';

 @Component({
   selector: 'mocks-config',
   styleUrls: ['mocks-config.styles.scss'],
   templateUrl: 'mocks-config.template.html',
   changeDetection: ChangeDetectionStrategy.OnPush
 })

 export class MocksConfigComponent implements OnInit, OnDestroy {

   @Input() model: any = {};

   public mockOptions = [
     {
       label: 'WIZARD.MOCKS.PASTE',
       icon: 'icon-clipboard',
       value: 'userProvidedExample'
     },
     {
       label: 'WIZARD.MOCKS.UPLOAD',
       icon: 'icon-paper',
       value: 'fileUploaded'
     },
     {
       label: 'WIZARD.MOCKS.URI',
       icon: 'icon-link',
       value: 'path'
     },
     {
       label: 'WIZARD.MOCKS.SQL',
       icon: 'icon-storage',
       value: 'query'
     }
   ];

   private _uploadFileSubscription: Subscription;

   constructor(
     private _cd: ChangeDetectorRef,
     private _store: Store<fromWizard.State>,
     public errorsService: ErrorMessagesService) { }


   ngOnInit(): void {
     if (this.model.path) {
       this.model.selectedMock = 'path';
     } else if (this.model.query) {
       this.model.selectedMock = 'query';
     } else if (this.model.fileUploaded) {
       this.model.selectedMock = 'fileUploaded';
     } else {
       this.model.selectedMock = 'userProvidedExample';
     }
     if (!this.model.path) {
       this.model.path = '';
     }

     this._uploadFileSubscription = this._store.pipe(select(getDebugFile)).subscribe((value) => {
       if (value) {
         this.model.fileUploaded = value;
         this._cd.markForCheck();
       }
     });
   }

   selectMockType(value: string) {
     this.model.selectedMock = value;
   }

   uploadFile(event: any) {
     this.model.userProvidedExample = event;
   }

   removeTextData() {
     this.model.userProvidedExample = '';
   }

   downloadTextData() {
     const element = document.createElement('a');
     element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(this.model.userProvidedExample));
     element.setAttribute('download', 'mock');
     element.style.display = 'none';
     document.body.appendChild(element);
     element.click();
     document.body.removeChild(element);
   }

   uploadMockFile(event) {
     this._store.dispatch(new debugActions.UploadDebugFileAction(event[0]));
   }

   removeMockFile() {
     this._store.dispatch(new debugActions.DeleteDebugFileAction(this.model.fileUploaded));
     this.model.fileUploaded = undefined;
   }

   downloadMockFile() {
     this._store.dispatch(new debugActions.DownloadDebugFileAction(this.model.fileUploaded));
   }

   ngOnDestroy(): void {
     this._uploadFileSubscription && this._uploadFileSubscription.unsubscribe();
   }

 }
