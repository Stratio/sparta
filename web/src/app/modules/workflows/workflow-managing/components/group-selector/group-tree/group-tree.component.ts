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

@Component({
    selector: 'group-tree',
    templateUrl: './group-tree.template.html',
    styleUrls: ['./group-tree.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class GroupTreeComponent implements OnInit, OnDestroy {

    @Input() selectedFolder: string;
    @Input() blockChilds = true;
    @Input() currentGroup: string;
    @Input() parentGroup: string;
    @Input() tree: Array<any>;
    @Input() index = 0;

    @Output() onSelectFolder = new EventEmitter<string>();

    ngOnInit() {
        const groups: any = {};
    }

    toggleTree(event: any, subtree: any) {
        event.stopPropagation();
        subtree.open = !subtree.open;
    }

    ngOnDestroy(): void {

    }

    selectFolder(event: any, name: string) {
        event.stopPropagation();
        if (name === this.parentGroup || ((this.blockChilds && name.indexOf(this.currentGroup) === 0 || !this.blockChilds && name === this.currentGroup))) {

        } else {
            this.onSelectFolder.emit(name);
        }
    }

}

