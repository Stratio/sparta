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
   EventEmitter,
   HostListener,
   Input,
   OnInit,
   Output
} from '@angular/core';
import { OutputSchemaField, OrderBy } from '@app/wizard/components/query-builder/models/SchemaFields';
import { Subject } from 'rxjs/Subject';
import { Store } from '@ngrx/store';

import * as fromQueryBuilder from './../../reducers';
import * as queryBuilderActions from './../../actions/queryBuilder';



@Component({
   selector: 'node-schema-output-box',
   styleUrls: ['node-schema-output-box.component.scss'],
   templateUrl: 'node-schema-output-box.component.html',
   changeDetection: ChangeDetectionStrategy.OnPush
})

export class NodeSchemaOutputBoxComponent implements AfterViewInit {

   @Input() schemaFields: OutputSchemaField[] = [];
   @Input() containerElement: HTMLElement;
   @Input() positionSubject: Subject<any>;
   @Input() filter: string;
   @Input() orderBy: Array<OrderBy> = [];

   @Output() schemaFieldsChange = new EventEmitter<OutputSchemaField[]>();

   public isOrdering = false;
   public orderNumber: number;
   public showFilters = false;
   public editExpression = undefined;
   public editColumn = undefined;
   public showDropDown = undefined;
   public dropDownCoords = { x: 0, y: 0 };
   public editFilter = false;
   public headerMenu = false;

   public items = [
      { label: 'Order by this field ascending', value: 'orderAsc', icon: 'icon-arrow-up' },
      { label: 'Order by this field descending', value: 'orderDesc', icon: 'icon-arrow-down' },
      { label: 'Remove field', value: 'remove', icon: 'icon-trash' }
   ];

   public outputOptions = [
      { label: 'Add field', value: 'addField' },
      { label: 'Remove all fields', value: 'removeFields' }
   ];

   constructor(private _store: Store<fromQueryBuilder.State>, private elementRef: ElementRef) { }

   @HostListener('document:click', ['$event'])
   public onDocumentClick(event: MouseEvent): void {
      const targetElement = event.target as HTMLElement;
      if (targetElement &&
         !this.elementRef.nativeElement.contains(targetElement) &&
         targetElement.className !== 'filter-add') {
         this.showDropDown = undefined;
         this.editColumn = undefined;
         this.editExpression = undefined;
         this.deleteSelection();
         this.editFilter = false;
         this.headerMenu = false;
         this.schemaFields.map(field => {
            if (field.expression === '') {
               field.expression = 'expression';
            }
         });
      }
   }

   ngAfterViewInit(): void {
      setTimeout(() => {
         this.positionSubject.next();
      });
      this.schemaFields.map(field => {
         if (field.expression === '') {
            field.expression = 'expression';
         }
      });
   }
   onEditFilter() {
      this.editFilter = true;
      this.editColumn = undefined;
      this.editExpression = undefined;
   }
   isRepeat(column) {
      return this.schemaFields.filter(field => field.column === column).length > 1;
   }

   onHeaderMenu() {
      this.headerMenu = !this.headerMenu;
   }

   onDrop(event) {
      this.editFilter = false;
      this.showFilters = false;
      if (this.isOrdering) {
         const oldPosition = event.item.index;
         let newPosition = event.index;
         if (newPosition > oldPosition) {
            newPosition = newPosition - 1;
         }
         this._store.dispatch(new queryBuilderActions.ChangeOutputSchemaFieldOrderAction({
            newPosition,
            oldPosition
         }));
         this.isOrdering = false;
         this._store.dispatch(new queryBuilderActions.ChangePathsVisibilityAction(true));
      } else {
         const items = event.item.map(item => ({
            expression: item.alias + '.' + item.column,
            column: item.column,
            order: '',
            originFields: [{
               alias: item.alias,
               name: item.column,
               table: item.table,

            }]
         }));
         this._store.dispatch(new queryBuilderActions.AddOutputSchemaFieldsAction({
            index: event.index,
            items
         }));
      }
      this.editExpression = undefined;
      this._refreshPosition();
   }

   toggleFilters() {
      this.showFilters = !this.showFilters;
      this.editFilter = false;
      this.editColumn = undefined;
      this.editExpression = undefined;
      this._refreshPosition();
   }

   onEndReorder() {
      this.isOrdering = false;
      this._store.dispatch(new queryBuilderActions.ChangePathsVisibilityAction(true));
   }

   onReorder(event, index) {
      this.isOrdering = true;
      this.orderNumber = index;
      this._store.dispatch(new queryBuilderActions.ChangePathsVisibilityAction(false));
   }

   changePosition() {
      setTimeout(() => this.schemaFieldsChange.emit(this.schemaFields));
   }

   getData(index: number) {
      return {
         i: index
      };
   }

   saveFilter(ev) {
      this._store.dispatch(new queryBuilderActions.SaveFilter(ev.target.value));
      this.editFilter = false;
      ev.preventDefault();
   }

   deleteFilter() {
      this._store.dispatch(new queryBuilderActions.SaveFilter(''));
   }

   onFocusOut(ev) {
      this._store.dispatch(new queryBuilderActions.SaveFilter(this.filter));
   }

   onEditExpression(field, position) {
      this.editExpression = position;
      if (field.originFields.length) {
         this._store.dispatch(new queryBuilderActions.SelectInputSchemaFieldAction({
            ...field.originFields[0],
            column: field.originFields[0].name
         }));
      }
      this.showDropDown = undefined;
      this.editColumn = undefined;
      this._refreshPosition();
   }

   onEditColumn(field, position) {
      this.editColumn = position;
      if (field.originFields.length) {
         this._store.dispatch(new queryBuilderActions.SelectInputSchemaFieldAction({
            ...field.originFields[0],
            column: field.originFields[0].name
         }));
      }
      this.showDropDown = undefined;
      this.editExpression = undefined;
      this._refreshPosition();
   }

   saveExpression(ev, field, position) {
      this.editExpression = undefined;
      this.showDropDown = undefined;
      if (field.expression === '') {
         field.expression = 'expression';
      }
      this._store.dispatch(new queryBuilderActions.UpdateField({ field, position }));
      this._refreshPosition();
      ev.preventDefault();
   }

   saveColumn(ev, field, position) {
      this.editColumn = undefined;
      this.showDropDown = undefined;
      if (field.column === '') {
         field.column = 'columnName';
      }

      this._store.dispatch(new queryBuilderActions.UpdateField({ field, position }));

      this._refreshPosition();
      ev.preventDefault();
   }

   contextMenu(ev, field, position) {
      if (!['input', 'textarea'].includes(ev.path[0].localName)) {
         this.deleteSelection();
         this.dropDownCoords = {
            x: ev.x,
            y: ev.y
         };
         ev.preventDefault();
         this.showDropDown = position;

         const dropDown = this.elementRef.nativeElement.querySelector('#dropdownMenu-' + position);

         const dropsDown = [].slice.call(this.elementRef.nativeElement.querySelectorAll('st-dropdown-menu'));
         dropsDown.map(element => {
            if (element.id.includes('dropdownMenu') && element.querySelector('.icon.icon-check.nuevo')) {
               element.removeChild(element.querySelector('.icon.icon-check.nuevo'));
            }
         });
         const newSpan = document.createElement('span');

         newSpan.style.display = field.order ? 'block' : 'none';
         newSpan.className = 'icon icon-check nuevo';
         newSpan.style.position = 'absolute';
         newSpan.style.right = '8px';
         newSpan.style.top = field.order === 'orderAsc' ? '15px' : '53px';
         newSpan.style.fontSize = '12px';
         dropDown.appendChild(newSpan);


         if (field.originFields.length) {
            this._store.dispatch(new queryBuilderActions.SelectInputSchemaFieldAction({
               ...field.originFields[0],
               column: field.originFields[0].name
            }));
         }

      }

   }

   onChangeOption(ev, field, position) {
      switch (ev.value) {
         case 'removeFields':
            this._store.dispatch(new queryBuilderActions.DeleteAllOutputFields());
            this._refreshPosition();
            break;
         case 'addField':
            const newPosition = this.schemaFields[this.schemaFields.length - 1].position;
            const newField: OutputSchemaField = {
               column: 'newColumn',
               expression: 'newExpression',
               order: '',
               position: { x: newPosition.x, y: newPosition.y + 40, height: newPosition.height },
               originFields: []
            };
            this._store.dispatch(new queryBuilderActions.AddNewField(newField));
            this._refreshPosition();
            break;
         case 'remove':
            this._store.dispatch(new queryBuilderActions.DeleteOutputField({ position }));
            this._refreshPosition();
            break;
         case 'orderAsc':
         case 'orderDesc':
            this._store.dispatch(new queryBuilderActions.ToggleOrder({
               order: field.order === ev.value ? '' : ev.value,
               position
            }));
            break;
         default:
            break;
      }
      this.showDropDown = undefined;
   }

   private _refreshPosition() {
      setTimeout(() => {
         this.positionSubject.next();
         this.schemaFieldsChange.emit(this.schemaFields);
      });
   }

   private deleteSelection() {
      this._store.dispatch(new queryBuilderActions.RemoveSelectedInputSchemas());
      this._refreshPosition();
   }

}
