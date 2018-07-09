/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
   ChangeDetectionStrategy,
   ChangeDetectorRef,
   Component,
   OnDestroy,
   OnInit,
   ViewChild,
   ViewContainerRef
} from '@angular/core';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import * as fromRoot from './reducers';
import * as pluginsActions from './actions/plugins';

import {
   StTableHeader, StModalButton, StModalResponse, StModalService, StHorizontalTab
} from '@stratio/egeo';
import { TranslateService } from '@ngx-translate/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BreadcrumbMenuService } from 'app/services';
import { FormFileComponent } from '@app/shared/components/form-file/form-file.component';
import { take } from 'rxjs/operator/take';

@Component({
   selector: 'sparta-plugins',
   templateUrl: './plugins.template.html',
   styleUrls: ['./plugins.styles.scss'],
   changeDetection: ChangeDetectionStrategy.OnPush
})
export class PluginsComponent implements OnInit, OnDestroy {

   @ViewChild('pluginModal', { read: ViewContainerRef }) target: any;
   @ViewChild(FormFileComponent) fileInput: FormFileComponent;

   public pluginsList: Array<any> = [];
   public deletePluginModalTitle: string;
   public deletePluginModalMessage: string;
   public deletePluginModalMessageTitle: string;
   public options: Array<StHorizontalTab> = [];
   public selectedPlugins: Array<string> = [];
   public breadcrumbOptions: Array<any>;
   public activeOption = 'PLUGINS';
   public fields: StTableHeader[] = [
      { id: 'fileName', label: 'Name' },
      { id: 'uri', label: 'URI' }
   ];
   public loaded$: Observable<boolean>;

   private selectedPluginsSubscription: Subscription;
   private pluginsListSubscription: Subscription;

   ngOnInit() {
      this._modalService.container = this.target;
      this.store.dispatch(new pluginsActions.ListPluginsAction());
      this.pluginsListSubscription = this.store.select(fromRoot.getPluginsList).subscribe((pluginsList: any) => {
         this.pluginsList = pluginsList;
         this._cd.markForCheck();
      });

      this.loaded$ = this.store.select(fromRoot.isLoaded);

      this.selectedPluginsSubscription = this.store.select(fromRoot.getSelectedPlugins).subscribe((selectedPlugins: Array<string>) => {
         this.selectedPlugins = selectedPlugins;
         this._cd.markForCheck();
      });
   }

   public uploadPlugin(event: any) {
      this.store.dispatch(new pluginsActions.UploadPluginAction(event[0]));
   }

   public selectAll(event: any) {
      this.store.dispatch(new pluginsActions.SelectAllPluginsAction(event));
   }
   public downloadPlugin(uri: string) {
      window.open(uri);
   }


   public deletePluginConfirmModal(): void {
      const buttons: StModalButton[] = [
         { label: 'Cancel', responseValue: StModalResponse.NO, closeOnClick: true, classes: 'button-secondary-gray' },
         { label: 'Delete', responseValue: StModalResponse.YES, classes: 'button-critical', closeOnClick: true }
      ];

      this._modalService.show({
         modalTitle: this.deletePluginModalTitle,
         buttons: buttons,
         maxWidth: 500,
         message: this.deletePluginModalMessage,
         messageTitle: this.deletePluginModalMessageTitle
      }).take(1).subscribe((response: any) => {
         if (response === 1) {
            this._modalService.close();
         } else if (response === 0) {
            this.store.dispatch(new pluginsActions.DeletePluginAction());
         }
      });
   }

   onChangedOption($event: string): void {
      this.route.navigate(['settings/resources/drivers']);
   }

   changeOrder($event: any): void {
      this.store.dispatch(new pluginsActions.ChangeOrderPlugins({
         orderBy: $event.orderBy,
         sortOrder: $event.type
      }));
   }

   checkRow(isChecked: boolean, value: any) {
      this.checkValue({
         checked: isChecked,
         value: value
      });
   }

   checkValue($event: any): void {
      if ($event.checked) {
         this.store.dispatch(new pluginsActions.SelectPluginAction($event.value.fileName));
      } else {
         this.store.dispatch(new pluginsActions.UnselectPluginAction($event.value.fileName));
      }
   }


   constructor(private store: Store<fromRoot.State>, private _modalService: StModalService, private translate: TranslateService,
      private route: Router, private currentActivatedRoute: ActivatedRoute, public breadcrumbMenuService: BreadcrumbMenuService,
      private _cd: ChangeDetectorRef) {
      this.breadcrumbOptions = breadcrumbMenuService.getOptions();
      const deletePluginModalTitle = 'DASHBOARD.DELETE_PLUGIN_TITLE';
      const deletePluginModalMessage = 'DASHBOARD.DELETE_PLUGIN_MESSAGE';
      const deletePluginModalMessageTitle = 'DASHBOARD.DELETE_PLUGIN_MESSAGE_TITLE';
      this.translate.get([deletePluginModalTitle, deletePluginModalMessage, deletePluginModalMessageTitle]).subscribe(
         (value: { [key: string]: string }) => {
            this.deletePluginModalTitle = value[deletePluginModalTitle];
            this.deletePluginModalMessage = value[deletePluginModalMessage];
            this.deletePluginModalMessageTitle = value[deletePluginModalMessageTitle];
         }
      );
   }

   ngOnDestroy(): void {
      this.selectedPluginsSubscription && this.selectedPluginsSubscription.unsubscribe();
      this.pluginsListSubscription && this.pluginsListSubscription.unsubscribe();
   }

}
