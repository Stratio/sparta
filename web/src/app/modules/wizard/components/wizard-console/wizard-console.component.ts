import { errorComparator } from 'rxjs-tslint/node_modules/tslint/lib/verify/lintError';
/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  Inject,
  Input,
  NgZone,
  OnInit
} from '@angular/core';
import { StHorizontalTab } from '@stratio/egeo';
import { DOCUMENT } from '@angular/common';
import { Store } from '@ngrx/store';

import * as debugActions from './../../actions/debug';
import * as fromWizard from './../../reducers';

@Component({
  selector: 'wizard-console',
  styleUrls: ['wizard-console.styles.scss'],
  templateUrl: 'wizard-console.template.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class WizardConsoleComponent implements OnInit, AfterViewInit {

  @Input() get entityData() {
    return this._entityData;
  };
  set entityData(value: any) {
    this.tableFields = [];
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
  private pos1 = 0;
  private pos2 = 0;

  private _entityData: any;
  private _element: any;
  constructor(private _el: ElementRef,
    private _ngZone: NgZone,
    private _store: Store<fromWizard.State>,
    @Inject(DOCUMENT) private _document: Document) {
    this._element = _el.nativeElement;
    this._element.style.transform = 'translateY(100%)';
  }

  ngOnInit(): void {
    this._store.select(fromWizard.getDebugConsoleSelectedTab).subscribe(selectedTab =>
      this.selectedOption = this.options.find(option => option.id === selectedTab));
  }

  ngAfterViewInit(): void {
    this._element.style.top = window.innerHeight - 200 + 'px';
    setTimeout(() => {
      this._element.style.transform = 'translateY(0)';
    });
  }

  changeFormOption(event: any) {
    this._store.dispatch(new debugActions.ChangeSelectedConsoleTab(event.id));
  }

  moveBox(e) {
    this._document.body.classList.add('dragging-console');
    this._ngZone.runOutsideAngular(() => {
      this.pos2 = e.clientY;
      document.onmouseup = this._closeDragElement.bind(this);
      document.onmousemove = this._elementDrag.bind(this);
    });
  }

  closeConsole() {
    this._store.dispatch(new debugActions.HideDebugConsoleAction());
  }

  private _elementDrag(e) {
    // calculate the new cursor position:
    this.pos1 = this.pos2 - e.clientY;
    this.pos2 = e.clientY;
    // set the element's new position:
    const top = this._element.offsetTop - this.pos1;
    this._element.style.top = top < 131 ? 131 : top + 'px';
  }

  private _closeDragElement() {
    this._document.body.classList.remove('dragging-console');
    /* stop moving when mouse button is released:*/
    document.onmouseup = null;
    document.onmousemove = null;
  }
}
