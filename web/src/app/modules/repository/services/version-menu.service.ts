/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {Injectable} from '@angular/core';
import {MenuOptionListGroup} from '@app/shared/components/menu-options-list/menu-options-list.component';
import {versionOptions} from '@app/repository/components/repository-table/repository-table.models';
import { CITags } from '@models/enums';
import { cloneDeep as _cloneDeep } from 'lodash';
import {select, Store} from '@ngrx/store';
import * as fromRoot from 'reducers';
import {take} from 'rxjs/operators';

@Injectable()
export class VersionMenuService {

  private _isCiCdEnabled: boolean;

  constructor(private _store: Store<fromRoot.State>) {
    this._store.pipe(select(fromRoot.getIsCiCdEnabled))
      .pipe(take(1))
      .subscribe(isCiCdEnabled => {
        this._isCiCdEnabled = isCiCdEnabled;
      });
  }

  public getVersionMenu(version: any) {
    const versionMenu: MenuOptionListGroup[] = [];
    if (version.ciCdLabel !== CITags.Released) {
      versionMenu.push(versionOptions.editVersionOption);
      versionMenu.push(versionOptions.runVersionOptions);
    }
    const newVersionsOps = _cloneDeep(versionOptions.newVersionOptions);
    if (version.ciCdLabel === CITags.Released || !this._isCiCdEnabled) {
      newVersionsOps.options = newVersionsOps.options.filter(op => op.id !== 'promote-version');
    }
    versionMenu.push(newVersionsOps);
    if (version.ciCdLabel !== CITags.Released) {
      versionMenu.push(versionOptions.deleteVersionOption);
    }
    return versionMenu;
  }

}
