/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  Input,
  NgZone,
  OnInit,
  Output,
  EventEmitter
} from '@angular/core';
import { StHorizontalTab } from '@stratio/egeo';

@Component({
  selector: 'workflow-console',
  styleUrls: ['workflow-console.styles.scss'],
  templateUrl: 'workflow-console.template.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class WorkflowConsoleComponent implements OnInit {
  @Input() workflow: any;

  @Output() closeConsole = new EventEmitter<boolean>();

  public options: StHorizontalTab[] = [{
    id: 'Exceptions',
    text: 'Exceptions'
  }];

  public selectedOption: StHorizontalTab;

  constructor(private _el: ElementRef, private _ngZone: NgZone) { }

  ngOnInit(): void {
   this.selectedOption = { id: 'Exceptions', text: 'Exceptions' };
  }

  onCloseConsole() {
    this.closeConsole.emit();
  }

}
