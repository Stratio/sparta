///
/// Copyright (C) 2015 Stratio (http://stratio.com)
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///         http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

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

