/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import 'rxjs/add/operator/delay';

import {
   AfterViewInit,
   ChangeDetectionStrategy,
   ChangeDetectorRef,
   Component,
   ElementRef,
   EventEmitter,
   HostListener,
   Input,
   OnInit,
   Output,
} from '@angular/core';
import {
   InputSchema,
   InputSchemaField,
   Join,
   SchemaFieldPosition,
} from '@app/wizard/components/query-builder/models/SchemaFields';
import { INPUT_SCHEMAS_MAX_HEIGHT } from '@app/wizard/components/query-builder/query-builder.constants';
import { Store } from '@ngrx/store';
import { Subject } from 'rxjs/Subject';

import * as queryBuilderActions from './../../actions/queryBuilder';
import * as fromQueryBuilder from './../../reducers';


@Component({
  selector: 'node-schema-input-box',
  styleUrls: ['node-schema-input-box.component.scss'],
  templateUrl: 'node-schema-input-box.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class NodeSchemaInputBoxComponent implements OnInit, AfterViewInit {

  @Input() schema: InputSchema;
  @Input() containerElement: HTMLElement;
  @Input() selectedFieldsNames: Array<string> = [];
  @Input() selectedFields: Array<InputSchemaField> = [];
  @Input() positionSubject: Subject<any>;

  @Input() disableDrag = false;

  @Output() changeFieldsPosition = new EventEmitter<any>();
  @Output() changeContainersPosition = new EventEmitter<any>();
  @Output() addJoin = new EventEmitter<Join>();


  public fieldPositions = {};
  public recalcPositionSubject = new Subject();
  public containerPositionSubject = new Subject();
  public listMaxHeight = INPUT_SCHEMAS_MAX_HEIGHT;

  public img: HTMLImageElement;
  public isDragging = false;
  public headerMenu = false;

  public config = {
    wheelSpeed: 0.4
  };

  public inputOptions = [
    { label: 'Add all fields', value: 'addFields' }
  ];
  private _unselectFnRef: any;

  constructor(private _store: Store<fromQueryBuilder.State>, private _cd: ChangeDetectorRef, private elementRef: ElementRef) { }

  @HostListener('document:click', ['$event'])
   public onDocumentClick(event: MouseEvent): void {
      const targetElement = event.target as HTMLElement;
      if (targetElement && !this.elementRef.nativeElement.contains(targetElement) ) {
         this.headerMenu = false;
      }
   }
  ngOnInit(): void {
    this.img = new Image();
    this.img.src = '/assets/images/move.png';

   }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.recalcPositionSubject.next();
      this.containerPositionSubject.next();
    });
  }

  public selectColumn(field: InputSchemaField) {
    document.removeEventListener('mouseup', this._unselectFnRef);
    if (this.selectedFieldsNames.indexOf(field.column) > -1) {
      this._unselectFnRef = function() {
        this._store.dispatch(new queryBuilderActions.SelectInputSchemaFieldAction(field));
      }.bind(this);
      document.addEventListener('mouseup', this._unselectFnRef);
    } else {
      this._store.dispatch(new queryBuilderActions.SelectInputSchemaFieldAction(field));
    }
  }

  public onChangeElementPosition(column: string, position: SchemaFieldPosition) {
      this.fieldPositions[column] = position;
      if (this.positionSubject) {
         this.positionSubject.next(this.fieldPositions);
      }
  }

  public onDragstart(event: DragEvent) {
    document.removeEventListener('mouseup', this._unselectFnRef);
    if (this.selectedFieldsNames.length < 1)  {
      event.preventDefault();
    }
   this.isDragging = true;
    if (event.dataTransfer['setDragImage'] && this.selectedFieldsNames.length > 1 ) {
      event.dataTransfer['setDragImage'](this.img, -10, 30);
    }
  }

  public onChangeContainerPosition(position: SchemaFieldPosition) {
    this.changeContainersPosition.emit({
      name: this.schema.name,
      position
    });
  }

  public onScroll($event) {
    this.recalcPositionSubject.next();
    this._cd.markForCheck();
  }

  public onMoved() {
    this.selectedFields = [];
    this.selectedFieldsNames = [];
  }
  onHeaderMenu() {
   this.headerMenu = !this.headerMenu;
}
onChangeOption(ev) {
   switch (ev.value) {
      case 'addFields':
         if (this.schema.fields && this.schema.fields.length) {
            const field = {
               table: this.schema.fields[0].table,
               expression: this.schema.fields[0].alias + '.*',
               originFields: [{
                  alias: this.schema.fields[0].alias,
                  name: '*',
                  table: this.schema.fields[0].table
               }]
            };
            this._store.dispatch(new queryBuilderActions.AddOutputSchemaFieldsAction({
               index: 0,
               items: [field]
            }));
         }
         break;

      default:
         break;
   }
   this.headerMenu = false;
}

  onDrop(event) {
      this.isDragging = false;
      if (event.item.length === 1 && event.item[0].fieldType !== '*' && event.item[0].alias !== this.schema.fields[event.index].alias && event.item[0].alias !== 't2') {
         const join: Join = {
            origin: event.item[0],
            destination: this.schema.fields[event.index],
            initData: {
               tableName: event.item[0].table,
               fieldName: event.item[0].column
            },
            fieldPositions: this.fieldPositions
         };
         this._store.dispatch(new queryBuilderActions.AddJoin(join));
      }
   }
}
