/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    Input,
    OnDestroy,
    OnInit,
    Output,
} from '@angular/core';

import { FOLDER_SEPARATOR } from './../../workflow.constants';
import { GroupTree } from './group.tree.model';
import { Group } from './../../models/workflows';

@Component({
    selector: 'group-selector',
    templateUrl: './group-selector.template.html',
    styleUrls: ['./group-selector.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class GroupSelectorComponent implements OnInit {

    @Input() groups: Array<Group>;
    @Input() selectedFolder: string;
    @Input() blockChilds = true;
    @Input() currentGroup: string;
    @Input() parentGroup: string;

    @Output() onSelectFolder = new EventEmitter<string>();

    public tree: GroupTree[] = [];


    ngOnInit() {
        this.tree = this.getGroupTree(this.groups, this.parentGroup);
    }

    getGroupTree(groups: Array<Group>, openFolder: string, currentFolder = ''): GroupTree[] {
       const groupTree = {
           subGroups: {}
       };
       groups.forEach((group: Group) => {
          const folders = group.name.split(FOLDER_SEPARATOR);
          const groupTreeAux = groupTree;
          let level = groupTreeAux;
          folders.forEach((folderName: string, index: number) => {
            if (!folderName.length) {
                return;
            }
            let currentLevel = level.subGroups[folderName];
            if (!currentLevel) {
                level.subGroups[folderName] = {
                    name: FOLDER_SEPARATOR + folders.slice(1, index + 1 ).join(FOLDER_SEPARATOR),
                    label: folderName,
                    open: false,
                    selectable: false,
                    subGroups: {}
                };
                currentLevel = level.subGroups[folderName];
            }
            if (folders.length  === index + 1) {
               currentLevel.selectable = true;
            }
            currentLevel.open = openFolder.indexOf(currentLevel.name) === 0;
            level = currentLevel;
          });
       });
       return this._covertHashToArray(groupTree).subGroups;
    }

    private _covertHashToArray(data: any) {
        data.subGroups = Object.keys(data.subGroups).map(key => data.subGroups[key]);
        data.subGroups.map(group => this._covertHashToArray(group));
        return data;
    }

}

