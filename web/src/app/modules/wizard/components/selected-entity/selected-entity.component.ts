/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
   ChangeDetectionStrategy,
   ChangeDetectorRef,
   Component,
   ElementRef,
   NgZone,
   OnDestroy,
   OnInit
} from '@angular/core';

@Component({
   selector: 'selected-entity',
   styleUrls: ['selected-entity.styles.scss'],
   templateUrl: 'selected-entity.template.html',
   changeDetection: ChangeDetectionStrategy.OnPush
})

export class SelectedEntityComponent implements OnInit, OnDestroy {


   private _nativeElement: HTMLElement;

   constructor(private zone: NgZone, private _cd: ChangeDetectorRef, private _el: ElementRef) {
      this._nativeElement = _el.nativeElement;
   }

   ngOnInit(): void {
      this._cd.detach();
      this.zone.runOutsideAngular(() => {
         this.mouseMove = this.mouseMove.bind(this);
         window.document.addEventListener('mousemove', this.mouseMove);
      });
   }

   mouseMove(event: any): void {
      this._nativeElement.style.cssText = `transform: translate(${event.clientX}px,${event.clientY}px)`;
   }

   ngOnDestroy(): void {
      window.document.removeEventListener('mousemove', this.mouseMove, false);
   }
}
