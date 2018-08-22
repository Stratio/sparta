/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { ChangeDetectionStrategy, ChangeDetectorRef, Component, ElementRef, OnDestroy, OnInit, Input } from '@angular/core';
import { InputSchema, OutputSchemaField, SelectedInputFieldsNames, SelectedInputFields, Path, ContainerPositions, JoinSchema, OrderBy, Join } from './models/SchemaFields';
import { Subject } from 'rxjs/Subject';
import { Observable } from 'rxjs/Observable';

import { Store } from '@ngrx/store';

import { isEqual as _isEqual } from 'lodash';
import * as fromQueryBuilder from './reducers';
import * as queryBuilderActions from './actions/queryBuilder';
import { SaveJsonWorkflowActionError } from '@app/workflows/workflow-managing/actions/workflow-list';


@Component({
   selector: 'query-builder',
   styleUrls: ['query-builder.component.scss'],
   templateUrl: 'query-builder.component.html',
   changeDetection: ChangeDetectionStrategy.OnPush
})

export class QueryBuilderComponent implements OnInit, OnDestroy {

   @Input() nodeData: any;
   @Input() entityFormModel: any;

   public outputSchemaFields: OutputSchemaField[] = [];
   public containerElement: HTMLElement;
   public paths: Array<Path>;
   public joinPaths: Array<Path>;
   public selectedInputFields: SelectedInputFields;
   public selectedInputFieldsNames: SelectedInputFieldsNames;
   public pathsVisible: boolean;
   public containerPositions: ContainerPositions = {};
   public join: JoinSchema;
   public filter: string;
   public orderBy: Array<OrderBy> = [];
   public showJoinDropDown = false;


   public inputSchemasPositionSubjects: Subject<any>[];
   public outputSchemaPositionSubject: Subject<any>;
   private schemaPositions: any;
   private _subscriptionSubject = new Subject<any>();

   constructor(private _el: ElementRef, private _store: Store<fromQueryBuilder.State>, private _cd: ChangeDetectorRef) {
      this.containerElement = this._el.nativeElement;
   }

   public joinsItems: Array<any> = [
      { label: 'Inner', value: 'INNER' },
      { label: 'Left', value: 'LEFT'},
      { label: 'Left only', value: 'LEFT_ONLY'},
      { label: 'Right', value: 'RIGHT' },
      { label: 'Right only', value: 'RIGHT_ONLY' },
      /* { label: 'Left right only', value: 'LEFT_RIGHT_ONLY' }, */
      { label: 'Full', value: 'FULL'},
      { label: 'Cross', value: 'CROSS' },
      { label: 'Remove Join', value: 'removeJoin', icon: 'icon-trash' }
   ];

   public inputSchemas: Array<InputSchema> = [];


   onChangeOutputFields(event: OutputSchemaField[]) {
      this.outputSchemaFields = event;
      this.getArrowCoordinates();
   }

   initStore() {
      const { configuration } = this.entityFormModel;
      const { schemas, debugResult } = this.nodeData;
      if (schemas) {

         const alias = ['t1', 't2'];
         this.inputSchemas = schemas.inputs
            .filter(input => !(input.error && input.error.message))
            .map((input, index) =>  ({
               name: input.result.step,
               alias: alias[index],
               fields: input.result.schema.fields.map(field => ({
                  column: field.name,
                  fieldType: field.type,
                  alias: alias[index],
                  table: input.result.step
               }))
            })).splice(0, 2);

         if (debugResult && debugResult.error && configuration.backup) {
            if (_isEqual(this.inputSchemas, configuration.backup.inputSchemaFields)) {
               this._store.dispatch(new queryBuilderActions.AddBackup(configuration.backup));
            } else {
               this._store.dispatch(new queryBuilderActions.InitQueryBuilder(this.inputSchemas));
            }
         } else if (configuration.backup  && _isEqual(this.inputSchemas, configuration.backup.inputSchemaFields)) {
            this._store.dispatch(new queryBuilderActions.AddBackup(configuration.backup));
         }  else {
            this._store.dispatch(new queryBuilderActions.InitQueryBuilder(this.inputSchemas));
         }
      } else {
         this._store.dispatch(new queryBuilderActions.InitQueryBuilder([]));
      }

   }

   ngOnInit(): void {


      this.initStore();

      this._store.select(fromQueryBuilder.getSelectedFields)
         .takeUntil(this._subscriptionSubject)
         .subscribe(selectedFields => {
            this.selectedInputFields = selectedFields;
            const selectedInputFieldsNames = {};
            for (const propertyName in selectedFields) {
               if (selectedInputFieldsNames) {
                  selectedInputFieldsNames[propertyName] = selectedFields[propertyName].map(field => field.column);
               }
            }
            this.selectedInputFieldsNames = selectedInputFieldsNames;
         });


      this._store.select(fromQueryBuilder.getRelationPathVisibility)
         .takeUntil(this._subscriptionSubject)
         .subscribe((visibility: boolean) => {
            setTimeout(() => {
               this.pathsVisible = visibility;
               this._cd.markForCheck();
            });
         });

      this._store.select(fromQueryBuilder.getOutputSchemaFields)
         .takeUntil(this._subscriptionSubject)
         .subscribe((outputSchemaFields) => {
            this.outputSchemaFields = outputSchemaFields;
            this.getArrowCoordinates();
            this._cd.markForCheck();
         });
      this.inputSchemasPositionSubjects = this.inputSchemas.map(() => new Subject<any>());
      this.outputSchemaPositionSubject = new Subject<any>();

      const subjects = [];
      this.inputSchemasPositionSubjects.forEach(subject => subjects.push(subject.debounceTime(0)));
      Observable.combineLatest(subjects)
         .takeUntil(this._subscriptionSubject)
         .subscribe(value => {
            this.schemaPositions = value;
            this.getArrowCoordinates();
            this.getJoinArrowCoordinates();
            this._cd.markForCheck();
         });

      this._store.select(fromQueryBuilder.getJoin)
         .subscribe((join: JoinSchema) => {
            this.join = join;
            this.getJoinArrowCoordinates();
         });

      this._store.select(fromQueryBuilder.getFilter)
         .subscribe((filter: string) => {
            this.filter = filter;
            this.getArrowCoordinates();
         });

      this._store.select(fromQueryBuilder.getInputSchemaFields)
         .subscribe((inputSchemas:  Array<InputSchema>) => {
            this.inputSchemas = inputSchemas;
         });

   }

   isDisableDrag(input) {
      return (this.join.type === 'RIGHT_ONLY' && input === 0) || (this.join.type === 'LEFT_ONLY' && input === 1);
   }


   onChangeJoin(join) {
      this.showJoinDropDown = false;
      const dropDown = this.containerElement.querySelector('#joinDropdown');
      if (join.value === 'removeJoin') {
         this._store.dispatch(new queryBuilderActions.DeleteJoin());
      } else {
         this._store.dispatch(new queryBuilderActions.ChangeJoinType(join.value));
      }
      if (dropDown.querySelector('.icon.icon-check') ) {
         dropDown.removeChild(dropDown.querySelector('.icon.icon-check'));
      }
   }

   getJoinIcon() {
      return `assets/images/JOIN_${this.join.type}White.png`;
   }

   onToggleJoinsItems() {
      const dropDown = this.containerElement.querySelector('#joinDropdown');
      const newSpan = document.createElement('span');
      const position = this.joinsItems.map(join => join.value).indexOf(this.join.type);

      this.showJoinDropDown = !this.showJoinDropDown;
      if (this.showJoinDropDown) {
         newSpan.className = 'icon icon-check';
         newSpan.style.position = 'absolute';
         newSpan.style.right = '30px';
         newSpan.style.top = 15 + position * 38 + 'px';
         newSpan.style.fontSize = '12px';
         dropDown.appendChild(newSpan);
      } else if (dropDown.querySelector('.icon.icon-check')) {
         dropDown.removeChild(dropDown.querySelector('.icon.icon-check'));
      }
   }

   ngOnDestroy(): void {
      this._subscriptionSubject.next();
      this._subscriptionSubject.unsubscribe();
   }

   setContainerPosition(event) {
      this.containerPositions[event.name] = event.position;
   }

   getArrowCoordinates() {
      const paths = [];
      const inputSchemasOrder = {};
      this.inputSchemas.forEach((schema: InputSchema, index: number) => inputSchemasOrder[schema.name] = index);
      if (this.schemaPositions) {
         this.outputSchemaFields.forEach((field: OutputSchemaField) => {
            field.originFields.forEach((originField) => {
               if (field.position && !field.expression.includes('.*')) {
                  const initCoors = this.schemaPositions[inputSchemasOrder[originField.table]][originField.name];
                  if (initCoors) {
                     paths.push({
                        initData: {
                           tableName: originField.table,
                           fieldName: originField.name
                        },
                        coordinates: {
                           init: {
                              x: 440,
                              y: initCoors.y,
                              height: initCoors.height
                           },
                           end: field.position
                        }
                     });
                  }
               }
            });
         });
      }
      this.paths = paths;
   }

   getJoinArrowCoordinates() {
      if (!this.join || !this.join.joins) {
        return;
      }
      const joinPaths = [];
      const inputSchemasOrder = {};
      this.inputSchemas.forEach((schema: InputSchema, index: number) => inputSchemasOrder[schema.name] = index);

      if (this.schemaPositions && this.schemaPositions.length > 1) {
         this.join.joins.map((jo: Join) => {
            if (inputSchemasOrder[jo.origin.table] !== undefined) {
               if (!this.schemaPositions[inputSchemasOrder[jo.origin.table]][jo.origin.column] || !this.schemaPositions[inputSchemasOrder[jo.destination.table]][jo.destination.column]) {
                  this.schemaPositions = [this.schemaPositions[1], this.schemaPositions[0]];
               }
               const initCoors = this.schemaPositions[inputSchemasOrder[jo.origin.table]][jo.origin.column];
               const endCoors = this.schemaPositions[inputSchemasOrder[jo.destination.table]][jo.destination.column];
               joinPaths.push({
                  initData: {
                     tableName: jo.origin.table,
                     fieldName: jo.origin.column
                  },
                  coordinates: {
                     init: {
                        x: 60,
                        y: initCoors.y,
                        height: initCoors.height
                     },
                     end: {
                        x: 60,
                        y: endCoors.y,
                        height: endCoors.height
                     }
                  }
               });
            }
         });
      }
      this.joinPaths = joinPaths;
   }
}
