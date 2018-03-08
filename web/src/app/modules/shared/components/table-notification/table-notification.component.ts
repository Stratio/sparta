/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
    selector: 'table-notification',
    templateUrl: './table-notification.template.html',
    styleUrls: ['./table-notification.styles.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TableNotificationComponent {
    @Input() text: string;
    @Input() link: string;
    @Output() onClickLink = new EventEmitter();
}
