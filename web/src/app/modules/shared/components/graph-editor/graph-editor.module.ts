/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { GraphEditorComponent } from './graph-editor.component';
import { GraphSelectorComponent } from './graph-selector/graph-selector.component';

import { DraggableElementDirective } from './draggable-element.directive';


@NgModule({
  exports: [
    DraggableElementDirective,
    GraphEditorComponent
  ],
  declarations: [
    GraphEditorComponent,
    GraphSelectorComponent,
    DraggableElementDirective
  ],
  imports: [
    CommonModule
  ]
})

export class GraphEditorModule {}
