/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'parameter-group-header',
  templateUrl: './parameter-group-header.component.html',
  styleUrls: ['./parameter-group-header.component.scss']
})
export class ParameterGroupHeaderComponent implements OnInit {

  @Input() searchValue: string;
  @Input() listMode: boolean;
  @Input() withContext: boolean;
  @Input() creationMode: boolean;
  @Input() configContext: boolean;

  @Output() onCreateParam: EventEmitter<void> = new EventEmitter<void>();
  @Output() onUploadParams: EventEmitter<any> = new EventEmitter<any>();
  @Output() onDownloadParams: EventEmitter<void> = new EventEmitter<void>();
  @Output() onSearch: EventEmitter<{filter?: string, text: string}> = new EventEmitter<{filter?: string, text: string}>();
  @Output() onConfigContext: EventEmitter<void> = new EventEmitter<void>();



  constructor() { }

  ngOnInit(): void { }
}
