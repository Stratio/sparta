/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  ElementRef,
  Input,
  OnChanges,
  SimpleChanges,
  ViewChild,
  EventEmitter,
  Output,
  AfterViewInit,
} from '@angular/core';
import { WizardAnnotation, annotationColors } from './wizard-annotation.model';
import { SpPopoverComponent } from '@app/shared/components/sp-popover/sp-popover.component';
import { getTimeRemaining } from '@utils';

@Component({
  selector: 'wizard-annotation',
  templateUrl: './wizard-annotation.component.html',
  styleUrls: ['wizard-annotation.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WizardAnnotationComponent implements AfterViewInit, OnInit, OnChanges {

  @Input() annotation: WizardAnnotation;
  @Input() position: { x: number; y: number; };
  @Output() activeChange = new EventEmitter<void>();
  @Output() changeColor = new EventEmitter<string>();

  @Output() postMessage = new EventEmitter<string>();
  @Output() createMessage = new EventEmitter<{ color: string; message: string }>();
  @Output() deleteAnnotation = new EventEmitter<number>();

  @ViewChild(SpPopoverComponent ) popover: SpPopoverComponent;
  @ViewChild('textareaNote') textareaNote: ElementRef;
  @ViewChild('scrollBottom') private _scrollElementRef: ElementRef;


  public palette = annotationColors;
  private _initialized = false;
  public replyActive = false;

  public currentMessage = '';
  public showed = false;
  public time: Array<string>;

  public ngOnInit(): void {
    this._initialized = true;
    setTimeout(() => {
      this._scrollBottom();
      this.showed = true;
    });
  }

  public ngAfterViewInit(): void {
    // if its creating the annotation, focus on textarea
    if (!this.annotation.messages.length) {
      setTimeout(() => this.textareaNote.nativeElement.focus());
    }
  }

  public ngOnChanges(changes: SimpleChanges): void {
    this.time = this.annotation.messages.map(message => getTimeRemaining(message.date));
    if (this._initialized && changes['position']) {
      this.showed = false;
      setTimeout(() => {
        this.showed = true;
        this._scrollBottom();
      });
    }
    this.currentMessage = '';
    this.replyActive = false;

    if (this._initialized && this.popover) {
      this.popover.checkPosition();
    }
  }

  public reply() {
    this.replyActive = true;
    setTimeout(() => this.textareaNote.nativeElement.focus());
  }

  public done() {
    this.postMessage.emit(this.currentMessage);

  }

  public save() {
    this.createMessage.emit({
      color: this.annotation.color,
      message: this.currentMessage
    });
  }

  public onChangeColor(color: string) {
    this.annotation.color = color;
    this.changeColor.emit(color);
  }

  private _scrollBottom() {
    this._scrollElementRef.nativeElement.scrollTop = this._scrollElementRef.nativeElement.scrollHeight;
  }
}
