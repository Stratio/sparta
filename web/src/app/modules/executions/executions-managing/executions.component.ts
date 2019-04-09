/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
  ChangeDetectorRef,
} from '@angular/core';
import { StHorizontalTab } from '@stratio/egeo';
import { ActivatedRoute } from '@angular/router';
import { take } from 'rxjs/operators';


@Component({
  selector: 'executions',
  styleUrls: ['executions.component.scss'],
  templateUrl: 'executions.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExecutionsComponent implements OnInit, OnDestroy {

  public executionsTabs: StHorizontalTab[] = [
    {
      id: 'executions',
      text: 'Executions'
    },
    {
      id: 'scheduled',
      text: 'Scheduled'
    }
  ];
  public selectedTab = this.executionsTabs[0];
  public showSidebar = false;
  public isArchivedPage: boolean;
  public hiddenToolBar = false;
  private _lastPosition = 0;

  constructor(private _activatedRoute: ActivatedRoute,
      private _cd: ChangeDetectorRef) { 
        this._onScroll = this._onScroll.bind(this);
        document.addEventListener('scroll', this._onScroll);
  }

  ngOnInit(): void {
    this._activatedRoute.pathFromRoot[
      this._activatedRoute.pathFromRoot.length - 1
    ].data
      .pipe(take(1))
      .subscribe(v => {
        this.isArchivedPage = v.archived ? true : false;
      });
  }

  ngOnDestroy(): void {
    document.removeEventListener('scroll', this._onScroll);
  }
  
  changedOption(event: StHorizontalTab) {
    this.selectedTab = event;
  }

  onShowSidebar() {
    this.showSidebar = !this.showSidebar;
  }

  closeSidebar() {
    this.showSidebar = false;
  }

  
  private _onScroll() {
    const top = window.pageYOffset || document.documentElement.scrollTop;
    this.hiddenToolBar = top > 50 && this._lastPosition < top;
    this._lastPosition = top;
    this._cd.markForCheck();
  }
}
