/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import {
  AfterContentInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  EventEmitter,
  Inject,
  Input,
  NgZone,
  OnInit,
  Output
} from '@angular/core';
import { DOCUMENT } from '@angular/common';

@Component({
   selector: 'sp-help',
   templateUrl: './sp-help.component.html',
   styleUrls: ['./sp-help.component.scss'],
   changeDetection: ChangeDetectionStrategy.OnPush
})
export class SpHelpComponent implements OnInit, AfterContentInit {

   @Input() get helpOptions(): Array<HelpOptions> {
      return this._helpOptions;
   }
   set helpOptions(value) {
      this._helpOptions = value;
      if (this.searchValue && this.searchValue.length) {
         this.search({
            text: this.searchValue
         });
      } else {
         this.options = value;
      }

   }
   @Input() xPos = 0;
   @Input() yPos = 0;
   @Output() onCloseHelpMenu =  new EventEmitter();

   public definitionShowed: HelpOptions = null;
   public options: Array<HelpOptions> = [];
   public searchValue = '';
   public config = {};

   private pos1 = 0;
   private pos2 = 0;
   private pos3 = 0;
   private pos4 = 0;

   private _element: any;
   private _helpOptions: Array<HelpOptions> = [];
   constructor(private _el: ElementRef,
      private _ngZone: NgZone,
      @Inject(DOCUMENT) private _document: Document,
   ) {
      this._element = _el.nativeElement;
   }

   ngOnInit() {
      this.options = this.helpOptions;
   }

   ngAfterContentInit(): void {
      this._element.style.top = this._element.offsetTop + this.yPos + 'px';
      this._element.style.left = this._element.offsetLeft + this.xPos + 'px';
   }

   showDefinition(event: string): void {
      this.definitionShowed = this.helpOptions.find(option => option.label === event);
   }

   returnList() {
      this.definitionShowed = null;
   }

   search(event) {
      this.definitionShowed = null;
      this.searchValue = event.text;
      this.options = this.helpOptions.filter(option =>
         option.label.toLocaleUpperCase().indexOf(event.text.toLocaleUpperCase()) > -1);
   }

   showSpartaDoc() {
      window.open('https://stratio.atlassian.net/wiki/spaces/SPARTA2x0x/overview');
   }

   moveBox(e) {
      this._document.body.classList.add('dragging');
      this._ngZone.runOutsideAngular(() => {
         this.pos3 = e.clientX;
         this.pos4 = e.clientY;
         document.onmouseup = this._closeDragElement.bind(this);
         document.onmousemove = this._elementDrag.bind(this);
      });
   }

   private _elementDrag(e) {
      // calculate the new cursor position:
      this.pos1 = this.pos3 - e.clientX;
      this.pos2 = this.pos4 - e.clientY;
      this.pos3 = e.clientX;
      this.pos4 = e.clientY;
      // set the element's new position:
      let top = this._element.offsetTop - this.pos2;
      let left = this._element.offsetLeft - this.pos1;

      const height = window.innerHeight;
      const width = window.innerWidth;

      const rect = this._element.getBoundingClientRect();
      if (top > height - rect.height) {
         top = height - rect.height;
      }
      if (left > width - rect.width) {
         left = width - rect.width;
      }
      this._element.style.top = top < 0 ? 0 : top + 'px';
      this._element.style.left = left < 0 ? 0 : left + 'px';
   }

   private _closeDragElement() {
      this._document.body.classList.remove('dragging');
      /* stop moving when mouse button is released:*/
      document.onmouseup = null;
      document.onmousemove = null;
   }
}

export interface HelpOptions {
   label: String;
   text: String;
   sections?: Array<HelpOptions>;
}
