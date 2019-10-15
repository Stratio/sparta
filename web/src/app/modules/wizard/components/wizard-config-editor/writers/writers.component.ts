/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Component, OnInit, Input, ChangeDetectionStrategy } from '@angular/core';
import { FormGroup, FormControl } from '@angular/forms';
import { getOutputWriter } from 'data-templates/index';
import { WizardService } from '@app/wizard/services/wizard.service';

@Component({
  selector: 'writers-config',
  templateUrl: './writers.component.html',
  styleUrls: ['./writers.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WritersComponent implements OnInit {

  @Input() nodeWriters: any;
  @Input() engine: string;
  @Input() writersGroup: FormGroup;
  @Input() nodeWritersNames: any;
  @Input() forceValidations: boolean;
  public accordionStates = [];
  public getKeys = Object.keys;
  public writerTemplates = [];

  constructor() { }
  public ngOnInit(): void {
    Object.keys(this.nodeWriters).forEach(key => {
      this.writerTemplates.push(getOutputWriter(this.nodeWritersNames[key].classPrettyName, this.engine));
      const control = new FormControl(this.nodeWriters[key]);
      this.writersGroup.addControl(key, control);
      this.accordionStates.push(false);
    });

    setTimeout(() => this.writersGroup.setValue(this.nodeWriters));
  }
}
