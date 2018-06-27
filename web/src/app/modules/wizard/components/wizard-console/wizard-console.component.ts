import { errorComparator } from 'rxjs-tslint/node_modules/tslint/lib/verify/lintError';
/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  Input,
  NgZone,
  OnInit
} from '@angular/core';
import { StHorizontalTab } from '@stratio/egeo';
import { Store } from '@ngrx/store';

import * as debugActions from './../../actions/debug';
import * as fromWizard from './../../reducers';

@Component({
  selector: 'wizard-console',
  styleUrls: ['wizard-console.styles.scss'],
  templateUrl: 'wizard-console.template.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class WizardConsoleComponent implements OnInit {

   @Input() get entityData() {
    return this._entityData;
  }

  set entityData(value: any) {
    this.tableFields = [];
    if (value && value.debugResult && value.debugResult.result && value.debugResult.result.data) {
      try {
        const obj = JSON.parse(value.debugResult.result.data);
        for (const key in obj) {
          this.tableFields.push(key);
         }
      } catch (error) { }
    }
    this._entityData = value;
    try {
      const res = value.debugResult.result.data;
      // sometimes its an Object literal, and other times an Array of object literals.
      this.data = {
        data: Array.isArray(res) ? res.map(item => JSON.parse(item)) : JSON.parse(res)
      };
    } catch (error) {
      this.data = null;
    }
  }
  @Input() genericError: any;

  public options: StHorizontalTab[] = [
    {
      id: 'Data',
      text: 'Debug Data'
    }, {
      id: 'Exceptions',
      text: 'Exceptions'
    }];
  public data: any;
  public selectedOption: StHorizontalTab;
  public tableFields: Array<string> = [];

  private _entityData: any;
  constructor(private _el: ElementRef,
    private _ngZone: NgZone,
    private _store: Store<fromWizard.State>) { }

  ngOnInit(): void {
    this._store.select(fromWizard.getDebugConsoleSelectedTab).subscribe(selectedTab =>
      this.selectedOption = this.options.find(option => option.id === selectedTab));
  }

  changeFormOption(event: any) {
    this._store.dispatch(new debugActions.ChangeSelectedConsoleTab(event.id));
  }

  closeConsole() {
    this._store.dispatch(new debugActions.HideDebugConsoleAction());
  }

  getData(result) {
    try {
      return JSON.parse(result.result.data);
    } catch (error) {
      return '';
    }
  }
}
