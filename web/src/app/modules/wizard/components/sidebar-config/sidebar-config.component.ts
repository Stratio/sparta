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
  EventEmitter,
  Input, OnChanges,
  OnDestroy,
  OnInit,
  Output
} from '@angular/core';
import { StHorizontalTab } from '@stratio/egeo';

@Component({
  selector: 'sidebar-config',
  styleUrls: ['sidebar-config.styles.scss'],
  templateUrl: 'sidebar-config.template.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class SidebarConfigComponent implements OnInit,  OnChanges {
  @Input() isVisible = true;
  @Input() top: number;
  @Input() nodeData: any;
  @Input() showCrossdataCatalog: boolean;

  @Output() toggleSidebar = new EventEmitter();

  public entityData: any;
  public selectedOption = 'data';

  public sidebarPosition = 0;
  public sidebarTopPosition = 0;

  public sideBarOptions: StHorizontalTab[] = [{
    id: 'data',
    text: 'Overview'
  }, {
    id: 'crossdata',
    text: 'Crossdata'
  }];

  readonly _fn: any;
  private _nodeContainer: Element;
  private _nodeContainerTop: Element;

  constructor(private _cd: ChangeDetectorRef) { }

  changeFormOption(event) {
    this.selectedOption = event.id;
  }

  /** lifecyle methods */

  ngOnInit(): void {}

  ngOnChanges(changes): void {
    if (changes.nodeData) {
      this.entityData = (this.nodeData.hasOwnProperty('editionType')) ?
        this.nodeData.editionType.data :
        this.nodeData;
    }
  }
}
