/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, Input, EventEmitter, Output, ElementRef } from '@angular/core';

@Component({
   selector: 'sp-foreground-notifications',
   templateUrl: 'sp-foreground-notifications.component.html',
   styleUrls: ['sp-foreground-notifications.component.scss'],
   host: {
      '[class.visible]': '_visible'
   }
})
/**
 * @description {Component} [Foreground notifications]
 *
 * Foreground notifications are made to let the user know info about a process she is performing in real time.
 *
 * @example
 *
 * {html}
 *
 * ```
 * <sp-foreground-notifications status="success" text="text" [visible]="isVisible"></sp-foreground-notifications>
 *
 * ```
 */

export class SpForegroundNotificationsComponent {


   /** @Input {bollean} [visible=flase] When true the notification is shown */
   @Input()
   set visible(value: boolean) {
      if (value !== undefined) {
         this._visible = value;
         this.visibleChange.emit(this._visible);
      }
   }
   get visible(): boolean {
      return this._visible;
   }

   /** @Input {string} [text=''] Displayed text */
   @Input() text: string;
   /** @Input {NotificationStatus} [status='NotificationStatus.default'] Defines the criticality level of the notification */
   @Input() status = 'default';

   @Output() visibleChange: EventEmitter<boolean> = new EventEmitter();

   private _visible = false;

   constructor() { }


   onClose(): void {
      this.visible = false;
   }

   getStatus(): string {
      switch (this.status) {
         case 'success':
         case 'warning':
         case 'critical':
            return this.status;
         default:
            return 'default';
      }
   }

}

