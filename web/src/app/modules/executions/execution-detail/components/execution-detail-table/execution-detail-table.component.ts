/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { StTableHeader, StDropDownMenuItem, Order, ORDER_TYPE } from '@stratio/egeo';

@Component({
  selector: 'sparta-execution-detail-table',
  templateUrl: './execution-detail-table.component.html',
  styleUrls: ['./execution-detail-table.component.scss'],
  host: {
    '(document:click)': 'onClick($event)'
  }
})
export class ExecutionDetailTableComponent implements OnInit {

  @Input() set values (values: Array<any>)  {
    this._values = [...values];

    this.filteredParameters = this.filteredParameters
      && this.filteredParameters.length
      && this.filteredParameters.length === values.length
      && this.filteredParameters[0].name ?
      this.filteredParameters :
      [...values];
    const filterLabels = Array.from(new Set(values.map(status => status.type || 'User')))
    .filter(status => status)
    .map(status => {
      return {
        label: status,
        value: status
      };
    });

    this.filterValues = [
      {
        label: 'All parameters',
        value: ''
      },
      ...filterLabels
    ];
    this.selectedFilter = this.selectedFilter || this.filterValues[0];
  }
  @Input() filterQuery: string;
  @Input() isSearchable: Boolean = true;
  @Input() fields: StTableHeader[] = [];

  @Output() onSearchParameters = new EventEmitter<any>();

  public isAllSelected: Boolean = false;
  public searchQuery: String = '';
  private _values = [];
  public filteredParameters;
  public keys = Object.keys;
  public filterValues: StDropDownMenuItem[] = [];
  public selectedFilter: any;
  public showedFilter: Boolean = false;

  constructor() {}

  ngOnInit(): void {}

  filterData(text: string) {
    this.filteredParameters = this._values.filter(textToFilter => textToFilter.name.toLowerCase().includes(text.toLowerCase()));
  }

  selectFilter(event: any, filter: string) {
    event.stopPropagation();
    this.showedFilter = true;
  }

  onChangeTypeFilter(selectedItem: StDropDownMenuItem) {
    this.filteredParameters = this._values
      .filter(
        textToFilter => textToFilter.type ?
        textToFilter.type.toLowerCase().includes(selectedItem.value.toLowerCase()) :
        false
      );
    this.selectedFilter = selectedItem;
  }

  onClick() {
    this.showedFilter = false;
  }

  onSortTable(order: Order) {
    const reverseConst: number = order.type === ORDER_TYPE.ASC ? 1 : -1;
    this.filteredParameters = [...this.filteredParameters].sort((a, b) => {
      return a[order.orderBy].toString().localeCompare(b[order.orderBy].toString()) * reverseConst;
    });
  }

}
