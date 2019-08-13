/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  Input
} from '@angular/core';
import {
  InputSchema,
  OutputSchemaField,
  SelectedInputFieldsNames,
  SelectedInputFields,
  Path,
  ContainerPositions,
  JoinSchema,
  Join,
  OutputSchemaFieldCopleteTable
} from './models/schema-fields';
import { Subject, combineLatest, Observable } from 'rxjs';
import { takeUntil, debounceTime } from 'rxjs/operators';

import { Store } from '@ngrx/store';
import { cloneDeep } from 'lodash';

import * as fromQueryBuilder from './reducers';
import * as queryBuilderActions from './actions/query-builder';
import { StDropDownMenuItem } from '@stratio/egeo';
import { QueryBuilderService } from './services/query-builder.service';
import { InnerJoinTypes } from './query-builder.constants';


@Component({
  selector: 'query-builder',
  styleUrls: ['query-builder.component.scss'],
  templateUrl: 'query-builder.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class QueryBuilderComponent implements OnInit, OnDestroy {

  @Input() nodeData: any;
  @Input() entityFormModel: any;

  public outputSchemaFields: Array<OutputSchemaField | OutputSchemaFieldCopleteTable> = [];
  public inputSchemas: Array<InputSchema> = [];

  public containerElement: HTMLElement;
  public paths: Array<Path>;
  public joinPaths: Array<Path>;
  public containerPositions: ContainerPositions = {};
  public join: JoinSchema;
  public showJoinDropDown = false;
  public iconJoin = 'icon-inner-join';
  public inputSchemasPositionSubjects: Subject<any>[];
  public outputSchemaPositionSubject: Subject<any>;

  public selectedInputFields$: Observable<SelectedInputFields>;
  public selectedInputFieldsNames$: Observable<SelectedInputFieldsNames>;
  public filter$: Observable<string>;

  private _schemaPositions: any;
  private _subscriptionSubject = new Subject<any>();

  private _inputsSchemaIndexMap: { [s: string]: number };

  constructor(
    private _el: ElementRef,
    private _store: Store<fromQueryBuilder.State>,
    private _queryBuilderService: QueryBuilderService,
    private _cd: ChangeDetectorRef) {
    this.containerElement = this._el.nativeElement;
  }

  public joinsItems: Array<StDropDownMenuItem> = [
    { label: 'Inner', icon: 'icon-inner-join', value: InnerJoinTypes.Inner },
    { label: 'Left', icon: 'icon-left-join', value: InnerJoinTypes.Left },
    { label: 'Left only', icon: 'icon-left-only-join', value: InnerJoinTypes.LeftOnly },
    { label: 'Right', icon: 'icon-right-join', value: InnerJoinTypes.Right },
    { label: 'Right only', icon: 'icon-right-only-join', value: InnerJoinTypes.RightOnly },
    { label: 'Full', icon: 'icon-full-join', value: InnerJoinTypes.Full },
    { label: 'Cross', icon: 'icon-circle-cross', value: InnerJoinTypes.Cross },
    { label: 'Remove Join', icon: 'icon-trash', value: InnerJoinTypes.RemoveJoin }
  ];

  onChangeOutputFields(event: OutputSchemaField[]) {
    this.outputSchemaFields = event;
    this._getArrowCoordinates();
  }

  initStore() {
    const configuration = cloneDeep(this.entityFormModel.configuration);
    const schemas = this.nodeData.schemas;
    this.inputSchemas = schemas && schemas.inputs
      .filter(input => !(input.error && input.error.message))
      .map((input, index) => ({
        name: input.result.step,
        alias: `t${index + 1}`,
        fields: input.result.schema.fields.map(field => ({
          column: field.name,
          fieldType: field.type,
          alias: `t${index + 1}`,
          table: input.result.step
        }))
      })).splice(0, 2) || [];

    configuration.backup ?
      this._store.dispatch(new queryBuilderActions.AddBackup(this._queryBuilderService.normalizeBackup(configuration.backup, this.inputSchemas))) :
      this._store.dispatch(new queryBuilderActions.InitQueryBuilder(this.inputSchemas));

  }

  ngOnInit(): void {
    this.initStore();
    this.selectedInputFields$ = this._store.select(fromQueryBuilder.getSelectedFields);
    this.selectedInputFieldsNames$ = this._store.select(fromQueryBuilder.getSelectedInputFieldsNames);
    this.filter$ = this._store.select(fromQueryBuilder.getFilter);

    this.inputSchemasPositionSubjects = this.inputSchemas.map(() => new Subject<any>());
    this.outputSchemaPositionSubject = new Subject<any>();

    this._store.select(fromQueryBuilder.getOutputSchemaFields)
      .pipe(takeUntil(this._subscriptionSubject))
      .subscribe((outputSchemaFields) => {
        this.outputSchemaFields = outputSchemaFields;
        this._getArrowCoordinates();
        this._cd.markForCheck();
      });
    const subjects = [];
    this.inputSchemasPositionSubjects.forEach(subject => subjects.push(subject.pipe(debounceTime(0))));
    combineLatest(subjects)
      .pipe(takeUntil(this._subscriptionSubject))
      .subscribe(value => {
        this._schemaPositions = value;
        this._getArrowCoordinates();
        this._getJoinArrowCoordinates();
        this._cd.markForCheck();
      });

    this._store.select(fromQueryBuilder.getJoin)
      .pipe(takeUntil(this._subscriptionSubject))
      .subscribe((join: JoinSchema) => {
        this.join = { ...join, joins: join.joins.filter((element: any, index, self) => index === self.findIndex((t: any) => t.origin.column === element.origin.column && t.origin.alias === element.origin.alias && t.destination.column === element.destination.column && t.destination.alias === element.destination.alias)) };
        this.iconJoin = this.joinsItems.find(j => j.value === join.type).icon;
        this._getJoinArrowCoordinates();
        this._cd.markForCheck();
      });

    this._store.select(fromQueryBuilder.getInputSchemaFields)
      .pipe(takeUntil(this._subscriptionSubject))
      .subscribe((inputSchemas: Array<InputSchema>) => {
        this._inputsSchemaIndexMap = {};
        inputSchemas.forEach((input, index) => {
          this._inputsSchemaIndexMap[input.name] = index;
        });
        this.inputSchemas = inputSchemas;
      });
  }

  ngOnDestroy(): void {
    this._subscriptionSubject.next();
    this._subscriptionSubject.unsubscribe();
  }

  isDisableDrag(input) {
    return (this.join.type === InnerJoinTypes.RightOnly && input === 0) || (this.join.type === InnerJoinTypes.LeftOnly && input === 1);
  }


  onChangeJoin(join) {
    this.showJoinDropDown = false;
    const dropDown = this.containerElement.querySelector('#joinDropdown');
    if (join.value === InnerJoinTypes.RemoveJoin) {
      this._store.dispatch(new queryBuilderActions.DeleteJoin());
    } else {
      this._store.dispatch(new queryBuilderActions.ChangeJoinType(join.value));
    }
    if (dropDown.querySelector('.icon.icon-check')) {
      dropDown.removeChild(dropDown.querySelector('.icon.icon-check'));
    }
  }

  getJoinIcon() {
    return this.join.type === InnerJoinTypes.Inner ? 'icon-inner-join' : this.join.type;
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

  setContainerPosition(event) {
    this.containerPositions[event.name] = event.position;
  }

  private _getArrowCoordinates() {
    const paths: Array<Path> = [];
    this.outputSchemaFields.forEach((field: OutputSchemaField) => {
      field.originFields.forEach((originField) => {
        if (field.position && !field.expression.includes('.*') && originField && originField.name && this._inputsSchemaIndexMap && this._inputsSchemaIndexMap[originField.table] >= 0 && this._schemaPositions) {
          const initCoors = this._schemaPositions[this._inputsSchemaIndexMap[originField.table]][originField.name];
          if (initCoors) {
            paths.push({
              initData: {
                tableName: originField.table,
                fieldName: originField.name
              },
              lostField: field.lostField,
              coordinates: {
                init: {
                  x: 440,
                  y: initCoors.y,
                  height: initCoors.height
                },
                end: field.position as any
              }
            });
          }
        }
      });
    });
    this.paths = paths;
  }

  private _getJoinArrowCoordinates() {
    if (!this.join || !this.join.joins || (this.inputSchemas && this.inputSchemas.length < 2)) {
      return;
    }
    const joinPaths = [];
    const inputSchemasOrder = {};
    this.inputSchemas.forEach((schema: InputSchema, index: number) => inputSchemasOrder[schema.name] = index);
    this.join.joins.forEach((jo: Join) => {
      if (this._schemaPositions && this._schemaPositions.length > 1 && inputSchemasOrder[jo.origin.table] >= 0 && inputSchemasOrder[jo.destination.table] >= 0) {
        if (inputSchemasOrder[jo.origin.table] && inputSchemasOrder[jo.destination.table] && (!this._schemaPositions[inputSchemasOrder[jo.origin.table]][jo.origin.column] || !this._schemaPositions[inputSchemasOrder[jo.destination.table]][jo.destination.column])) {
          this._schemaPositions = [this._schemaPositions[1], this._schemaPositions[0]];
        }
        const initCoors = this._schemaPositions[inputSchemasOrder[jo.origin.table]][jo.origin.column];
        const endCoors = this._schemaPositions[inputSchemasOrder[jo.destination.table]][jo.destination.column];
        joinPaths.push({
          initData: {
            tableName: jo.origin.table,
            fieldName: jo.origin.column
          },
          endData: {
            tableName: jo.destination.table
          },
          lostField: jo.origin.lostField || jo.destination.lostField,
          coordinates: {
            init: {
              x: 300 - 3,
              y: initCoors.y,
              height: initCoors.height
            }, end: {
              x: 300 - 3,
              y: endCoors.y,
              height: endCoors.height
            }
          }
        });
      }
    });
    this.joinPaths = joinPaths;
  }
}
