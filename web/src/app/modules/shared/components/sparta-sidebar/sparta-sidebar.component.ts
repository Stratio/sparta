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
   Output
} from '@angular/core';

@Component({
   selector: 'sparta-sidebar',
   templateUrl: './sparta-sidebar.template.html',
   styleUrls: ['./sparta-sidebar.styles.scss'],
   changeDetection: ChangeDetectionStrategy.OnPush
})
export class SpartaSidebarComponent implements AfterViewInit, OnDestroy {

   @Input() isVisible = false;
   @Input() showCloseButton = true;
   @Input() showScrollBar = false;
   @Input() fullHeight: boolean;
   @Output() onCloseSidebar = new EventEmitter();

   public loaded = false;
   public minHeight: number;

   constructor(private _element: ElementRef, private _cd: ChangeDetectorRef) {
      this._calculateFullHeight = this._calculateFullHeight.bind(this);
   }

   ngAfterViewInit(): void {
      if (this.fullHeight) {
         this._calculateFullHeight();
         window.addEventListener('resize', this._calculateFullHeight);
      }

   }
   ngOnDestroy(): void {
      window.removeEventListener('resize', this._calculateFullHeight);
   }

   private _calculateFullHeight() {
      const rect = this._element.nativeElement.getBoundingClientRect();
      this.minHeight = window.innerHeight - rect.top - 30;
      this._cd.detectChanges();
   }
}
