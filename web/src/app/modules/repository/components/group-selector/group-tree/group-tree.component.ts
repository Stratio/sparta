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
    Output,
} from '@angular/core';
import { GroupTree } from '../group.tree.model';

@Component({
    selector: 'group-tree',
    templateUrl: './group-tree.template.html',
    styleUrls: ['./group-tree.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class GroupTreeComponent {

    @Input() selectedFolder: string;
    @Input() blockChilds = true;
    @Input() currentGroup: string;
    @Input() parentGroup: string;
    @Input() tree: Array<GroupTree>;
    @Input() index = 0;

    @Output() onSelectFolder = new EventEmitter<string>();

    toggleTree(event: any, subtree: GroupTree) {
        event.stopPropagation();
        subtree.open = !subtree.open;
    }

    selectFolder(event: any, name: string) {
        event.stopPropagation();
        if (name === this.parentGroup || ((this.blockChilds && name.indexOf(this.currentGroup) === 0
            || !this.blockChilds && name === this.currentGroup))) {

        } else {
            this.onSelectFolder.emit(name);
        }
    }

    trackByFn(index, item) {
        return item.id;
    }

}

