/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { StTreeEvent, StTreeNode } from '@stratio/egeo';

@Component({
  selector: 'node-schema',
  styleUrls: ['node-schema.styles.scss'],
  templateUrl: 'node-schema.template.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class NodeSchemaComponent {
  @Input() schema: StTreeNode;

  toggleNode(node: any) {
    node.open = node.open ? false : true;
  }
}
