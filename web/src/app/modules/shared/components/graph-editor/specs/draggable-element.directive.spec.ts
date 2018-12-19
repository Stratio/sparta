/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { async, TestBed, ComponentFixture } from '@angular/core/testing';
import { Component, DebugElement, OnInit } from '@angular/core';
import { By } from '@angular/platform-browser';

import { DraggableElementPosition } from '../';
import { DraggableElementDirective } from '../';

@Component({
  template: `
  <svg width="800" height="800" style="background-color: red">
    <rect draggable-element
      [(position)]="position"
      width="300"
      height="100"
      style="fill:rgb(0,0,255);stroke-width:3;stroke:rgb(0,0,0)"/>
  </svg>
  `
})
class TestDraggableElementComponent {
  public position: DraggableElementPosition = {
    x: 10,
    y: 10
  };
}


describe('[DraggableElementDirective]', () => {

  let fixture: ComponentFixture<TestDraggableElementComponent>;
  let component: TestDraggableElementComponent;
  let directive: DebugElement;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DraggableElementDirective, TestDraggableElementComponent]
    }).compileComponents();  // compile template and css
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestDraggableElementComponent);
    component = fixture.componentInstance;
    directive = fixture.debugElement.query(By.directive(DraggableElementDirective));
    fixture.detectChanges();
  });

  describe('On init', () => {
    it('sets the draggable element initial position', () => {
      expect(fixture.debugElement.query(By.css('rect')).nativeElement.getAttribute('transform')).toEqual('translate(10,10)');
    });

    describe('listen user interaction events', () => {
      xit('emit click event when the user makes a single click', (done) => {
      });
    });
  });
});
