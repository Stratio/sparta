/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
   AfterViewInit,
   ChangeDetectionStrategy,
   ChangeDetectorRef,
   Component,
   ElementRef,
   EventEmitter,
   Input,
   OnDestroy,
   Output,
   OnInit
} from '@angular/core';


@Component({
   selector: 'sparta-sidebar',
   templateUrl: './sparta-sidebar.template.html',
   styleUrls: ['./sparta-sidebar.styles.scss'],
   changeDetection: ChangeDetectionStrategy.OnPush
})
export class SpartaSidebarComponent implements OnInit {
   @Input() isVisible = false;
   @Input() showCloseButton = true;
   @Input() showScrollBar = false;
   @Input() fullHeight: boolean;
   @Input() darkBorder = false;
   @Output() onCloseSidebar = new EventEmitter();

   public loaded = false;
   public minHeight: number;

   constructor(private _element: ElementRef, private _cd: ChangeDetectorRef) {
   }

   ngOnInit(): void {
    this.loaded = true;
  }


}
