/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  OnInit,
  Output
} from '@angular/core';
import { Store, select } from '@ngrx/store';
import { Subscription } from 'rxjs';

import * as fromWizard from './../../../reducers';

@Component({
  selector: 'parameters-group-selector',
  styleUrls: ['parameters-group-selector.component.scss'],
  templateUrl: 'parameters-group-selector.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class ParametersGroupSelectorComponent implements OnInit {

  @Input() selectedGroups: Array<string> = [];
  @Output() selectedGroupsChange = new EventEmitter<Array<string>>();

  public customGroups: Array<any> = [];
  public filteredCustomGroups: Array<any> = [];

  public selectedEnvironments = false;
  public config = {
    wheelSpeed: 0.4
  };
  public searchText = '';
    public isOpened = false;

  private _groupsSubscription: Subscription;

  ngOnInit(): void { }

  constructor(private _eref: ElementRef, private _store: Store<fromWizard.State>) {
    this._groupsSubscription = this._store.pipe(select(fromWizard.getCustomGroups))
      .subscribe(customGroups => {
        this.customGroups = customGroups.filter(g => !g.parent);
        this.filteredCustomGroups = customGroups;
      });
  }

  @HostListener('document:click', ['$event'])
  onClick(event: any): void {
    if (!this._eref.nativeElement.contains(event.target)) {
      this.isOpened = false;
    }
  }

  toggleSelector() {
    this.isOpened = !this.isOpened;
    this.onSearchResult({
      text: ''
    });
  }

  onSelectAllCustom(event) {
    if (event.checked) {
      this.selectedGroups = [...this.customGroups.map(group => group.name),
        ...this.selectedGroups.indexOf('Environment') > -1 ? ['Environment'] : []];
    } else {
      this.selectedGroups = this.selectedGroups.filter(group => group === 'Environment');
    }
    this.selectedGroupsChange.emit(this.selectedGroups);
  }

  selectCustom(event, groupName: string) {
    if (event.checked) {
      this.selectedGroups = [...this.selectedGroups, groupName];
    } else {
      this.selectedGroups = this.selectedGroups.filter(group => group !== groupName);
    }
    this.selectedGroupsChange.emit(this.selectedGroups);
  }
  selectEnvironmentGroup(event) {
    if (event.checked) {
      if (this.selectedGroups.indexOf('Environment') === -1) {
        this.selectedGroups =  [...this.selectedGroups, 'Environment'];
      }
    } else {
      this.selectedGroups = this.selectedGroups.filter(env => env !== 'Environment');
    }
    this.selectedGroupsChange.emit(this.selectedGroups);
  }

  onSearchResult(event) {
    this.searchText = event.text.toUpperCase();
    if (event.text.length) {
      this.filteredCustomGroups = this.customGroups.filter(group => {
        return group.name.toUpperCase().indexOf(this.searchText) > -1;
      });
    } else {
      this.filteredCustomGroups = this.customGroups;
    }
  }
}
