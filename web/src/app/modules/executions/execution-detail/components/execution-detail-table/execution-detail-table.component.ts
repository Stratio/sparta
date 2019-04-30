/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, OnInit, Input, Output, EventEmitter, ViewEncapsulation } from '@angular/core';
import { StTableHeader, StDropDownMenuItem, Order, ORDER_TYPE } from '@stratio/egeo';
import { NONE_TYPE } from '@angular/compiler/src/output/output_ast';

@Component({
  selector: 'sparta-execution-detail-table',
  templateUrl: './execution-detail-table.component.html',
  styleUrls: ['./execution-detail-table.component.scss'],
  host: {
    '(document:click)': 'onClick($event)'
  }
})
export class ExecutionDetailTableComponent implements OnInit {

  @Input() defaultFilterLabel = 'All parameters';
  @Input() valueToFilter = 'type';
  @Input() set values (values: Array<any>)  {
    this._values = [...values];

    this.filteredParameters = this.filteredParameters
      && this.filteredParameters.length
      && this.filteredParameters.length === values.length
      && this.filteredParameters[0].name ?
      this.filteredParameters :
      [...values];
    this.filterQuery = this._filterQuery;
    const filterLabels = Array.from(new Set(values.map(status => status[this.valueToFilter])))
    .filter(status => status)
    .map(status => {
      return {
        label: status,
        value: status
      };
    });

    this.filterValues = [
      {
        label: this.defaultFilterLabel,
        value: ''
      },
      ...filterLabels
    ];
    this.selectedFilter = this.selectedFilter || this.filterValues[0];
  }
  @Input() isSearchable: Boolean = true;
  @Input() fields: StTableHeader[] = [];
  @Input() fieldId = 'id';
  @Input() set filterQuery(filter) {
    this._filterQuery = filter;
    if (filter) {
      this.filteredParameters = this._values.filter(textToFilter => textToFilter.name.toLowerCase().includes(filter.toLowerCase()));
    }
  }

  @Output() onSearchParameters = new EventEmitter<any>();
  @Output() onSelectItem = new EventEmitter<any>();
  @Output() onFilter = new EventEmitter<string>();

  public isAllSelected = false;
  private _values = [];
  public filteredParameters;
  public keys = Object.keys;
  public filterValues: StDropDownMenuItem[] = [];
  public selectedFilter: any;
  public showedFilter = false;
  public _filterQuery: string;

  constructor() {}

  ngOnInit(): void {}

  filterData(text: string) {
    this.onFilter.emit(text);
  }

  selectFilter(event: any, filter: string) {
    event.stopPropagation();
    this.showedFilter = true;
  }

  onChangeTypeFilter(selectedItem: StDropDownMenuItem) {
    this.filteredParameters = this._values
      .filter(
        textToFilter => textToFilter[this.valueToFilter] ?
        textToFilter[this.valueToFilter].toLowerCase().includes(selectedItem.value.toLowerCase()) :
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

  selectItem(parameterData) {
    if (parameterData.hasOwnProperty(this.fieldId)) {
      this.onSelectItem.emit(parameterData[this.fieldId]);
    } else {
      this.onSelectItem.emit();
    }
  }

}
