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
  Input,
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

export class SidebarConfigComponent implements OnInit, AfterViewInit, OnDestroy {

  @Input() isVisible = true;
  @Input() nodeData: any;
  @Input() showCrossdataCatalog: boolean;

  @Output() toggleSidebar = new EventEmitter();

  public entityData: any;
  public selectedOption = 'data';

  public sidebarPosition = 0;

  public sideBarOptions: StHorizontalTab[] = [{
    id: 'data',
    text: 'Input/Output'
  }, {
    id: 'crossdata',
    text: 'Crossdata'
  }];

  private _fn: any;
  private _nodeContainer: Element;

  constructor(
    private _cd: ChangeDetectorRef) {
      this._fn = this._calculatePosition.bind(this);
    }

  changeFormOption(event) {
    this.selectedOption = event.id;
  }

  /** lifecyle methods */

  ngOnInit(): void {
    this.entityData = this.nodeData.editionType.data;
  }

  ngAfterViewInit(): void {
    this._nodeContainer = document.getElementById('save-node-button');
    this._fn();
    window.addEventListener('resize', this._fn);
  }

  ngOnDestroy(): void {
    window.removeEventListener('resize', this._fn);
  }

  private _calculatePosition() {
    const rect = this._nodeContainer.getBoundingClientRect();
    this.sidebarPosition = window.innerWidth - rect.left - rect.width - 13;
    this._cd.markForCheck();
  }

}
