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
  OnInit,
  Renderer2,
  Input,
  AfterViewInit
} from '@angular/core';

@Component({
  selector: 'selected-entity',
  styleUrls: ['selected-entity.component.scss'],
  template: `<span [className]="icon && icon.length ? ('icon ' + icon) : 'square'"></span>`,
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class WizardSelectedEntityComponent implements OnInit, AfterViewInit, OnDestroy {

  @Input() icon: string;

  private _nativeElement: HTMLElement;

  constructor(private zone: NgZone,
    private _cd: ChangeDetectorRef,
    private _renderer: Renderer2,
    _el: ElementRef) {
    this._nativeElement = _el.nativeElement;
    this._mouseMove = this._mouseMove.bind(this);
    this._mouseEnter = this._mouseEnter.bind(this);
  }

  ngOnInit(): void {
    this.zone.runOutsideAngular(() => {
      document.addEventListener('mousemove', this._mouseMove);
      document.addEventListener('mouseenter', this._mouseEnter, false);
    });
  }

  ngAfterViewInit(): void {
    this._cd.detach();
  }

  private _mouseMove(event: MouseEvent): void {
    this._renderer.setStyle(this._nativeElement, 'transform', `translate(${event.clientX}px,${event.clientY}px`);
  }

  private _mouseEnter(event: MouseEvent): void {
    document.removeEventListener('mouseenter', this._mouseEnter, false);
    this._mouseMove(event);
  }

  ngOnDestroy(): void {
    document.removeEventListener('mousemove', this._mouseMove, false);
  }
}
