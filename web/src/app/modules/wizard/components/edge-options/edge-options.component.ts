/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { ChangeDetectionStrategy, Component, Input, OnChanges, SimpleChanges, ChangeDetectorRef } from '@angular/core';
import { StDropDownMenuItem, StDropDownMenuGroup } from '@stratio/egeo';
import { cloneDeep as _cloneDeep } from 'lodash';
import { TranslateService } from '@ngx-translate/core';
import { Store } from '@ngrx/store';

import * as fromWizard from './../../reducers';
import * as wizardActions from './../../actions/wizard';
import { EdgeOption } from '@app/wizard/models/node';

@Component({
  selector: 'edge-options',
  styleUrls: ['edge-options.styles.scss'],
  templateUrl: 'edge-options.template.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class EdgeOptionsComponent implements OnChanges {

  @Input() edgeOptions: EdgeOption;

  public menu: ((StDropDownMenuItem | StDropDownMenuGroup)[]);
  public selectedItem: StDropDownMenuItem = {
    label: '',
    value: ''
  };
  private _removeEdgeItem: StDropDownMenuItem = {
    label: 'Remove edge',
    value: 'remove'
  };
  private _edgeTypesMenu: ((StDropDownMenuItem | StDropDownMenuGroup)[]) = [
    {
      title: 'Edge type',
      items: [
      ]
    },
    {
      title: '',
      items: [
        this._removeEdgeItem
      ]
    }
  ];

  ngOnChanges(changes: SimpleChanges): void {
    this.selectedItem = {
      label: '',
      value: ''
    };
    const relations = this.edgeOptions.supportedDataRelations;
    const edgeType = this.edgeOptions.edgeType;
    if (relations && relations.length && relations.length > 1) {
      const supportedRelations = relations;
      const menu = _cloneDeep(this._edgeTypesMenu);
      const keys = supportedRelations.map(rel => 'WIZARD.RELATIONS.' + rel.toUpperCase());
      this._translateService.get(keys).subscribe(optionsLabels => {
        const options = keys.map((rel: string, index: number) => {
          const item = {
            label: optionsLabels[rel],
            value: supportedRelations[index]
          };
          if (edgeType === supportedRelations[index]) {
            this.selectedItem = item;
          }
          return item;
        });
        (menu[0] as StDropDownMenuGroup).items = options;
        this.menu = menu;
      });
    } else {
      this.menu = [this._removeEdgeItem];
    }
    this._cd.markForCheck();
  }


  onChange(event: any) {
    if (event.value === 'remove') {
      this._store.dispatch(new wizardActions.DeleteNodeRelationAction({
        origin: this.edgeOptions.relation.initialEntityName,
        destination: this.edgeOptions.relation.finalEntityName
      }));
    } else {
      this._store.dispatch(new wizardActions.SelectEdgeTypeAction({
        value: event.value,
        relation: this.edgeOptions.relation
      }));
      this._store.dispatch(new wizardActions.SetWizardStateDirtyAction());
    }

  }

  constructor(private _translateService: TranslateService,
    private _cd: ChangeDetectorRef,
    private _store: Store<fromWizard.State>) {

  }
}
