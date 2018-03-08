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

@Component({
    selector: 'group-selector',
    templateUrl: './group-selector.template.html',
    styleUrls: ['./group-selector.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class GroupSelectorComponent implements OnInit, OnDestroy {

    @Input() groups: Array<any>;
    @Input() selectedFolder: string;
    @Input() blockChilds = true;
    @Input() currentGroup: string;
    @Input() parentGroup: string;

    @Output() onSelectFolder = new EventEmitter<string>();

    public tree: any = [];


    ngOnInit() {
        this.tree = this.getGroupTree(this.groups, this.parentGroup);
    }

    getGroupTree(groups: Array<any>, openFolder: string, currentFolder = '') {
        const acc: any = [];
        return groups.filter((group: any) => {
            const split = group.name.split(currentFolder + FOLDER_SEPARATOR);
            if (split.length === 2 && split[0] === '' && split[1].indexOf(FOLDER_SEPARATOR) === -1) {
                group.label = split.length > 1 ? split[split.length - 1] : group.name;
                return true;
            } else {
                acc.push(group);
                return false;
            }
        }).map((group: any) => {
            group.open = openFolder.indexOf(group.name) === 0;
            group.subGroups = this.getGroupTree(acc, openFolder, group.name);
            return group;
        });
    }

    ngOnDestroy(): void {

    }

}

