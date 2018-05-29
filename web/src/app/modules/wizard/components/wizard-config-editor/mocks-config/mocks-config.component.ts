/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import {
  Component, OnInit, OnDestroy, ChangeDetectionStrategy, Input, ChangeDetectorRef
} from '@angular/core';

import { ErrorMessagesService } from 'services';

@Component({
  selector: 'mocks-config',
  styleUrls: ['mocks-config.styles.scss'],
  templateUrl: 'mocks-config.template.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class MocksConfigComponent implements OnInit {

  @Input() model: any = {};

  public mockOptions = [
    {
      label: 'WIZARD.MOCKS.UPLOAD',
      icon: 'icon-paper',
      value: 'userProvidedExample'
    },
    {
      label: 'WIZARD.MOCKS.URI',
      icon: 'icon-link',
      value: 'path'
    },
    {
      label: 'WIZARD.MOCKS.SQL',
      icon: 'icon-storage',
      value: 'query'
    }
  ];

  constructor(
    private _cd: ChangeDetectorRef,
    public errorsService: ErrorMessagesService) { }


  ngOnInit(): void {
    if (this.model.path) {
      this.model.selectedMock = 'path';
    } else if (this.model.query) {
      this.model.selectedMock = 'query';
    } else {
      this.model.selectedMock = 'userProvidedExample';
    }
    if (!this.model.path) {
      this.model.path = '';
    }
  }

  selectMockType(value: string) {
    this.model.selectedMock = value;
  }

  uploadFile(event: any) {
    this.model.userProvidedExample = event;
  }

  removeTextData() {
    this.model.userProvidedExample = '';
  }

  downloadTextData() {
    const element = document.createElement('a');
    element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(this.model.userProvidedExample));
    element.setAttribute('download', 'mock');
    element.style.display = 'none';
    document.body.appendChild(element);
    element.click();
    document.body.removeChild(element);
  }

}
