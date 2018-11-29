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

  @Input() get debugData() {
    return this._debugData;
  };
  set debugData(value: any) {
    this.tableFields = [];
    this._debugData = value;
    if (value && value.result) {
      try {
        const res = value.result.data;
        // sometimes its an Object literal, and other times an Array of object literals.
        this.data = {
          data: Array.isArray(res) ? res.map(item => JSON.parse(item)) : JSON.parse(res)
        };
      } catch (error) {
        this.data = null;
      }
    } else {
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

  private _debugData: any;

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
