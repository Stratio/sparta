/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { TestBed, async, ComponentFixture } from '@angular/core/testing';
import { ConsoleBoxComponent } from './console-box.component';
import { DebugElement, NO_ERRORS_SCHEMA  } from '@angular/core';
import { By } from '@angular/platform-browser';
import { EgeoModule } from '@stratio/egeo';


let component: ConsoleBoxComponent;
let fixture: ComponentFixture<ConsoleBoxComponent>;
let el: DebugElement;

describe('ConsoleBoxComponent', () => {
    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [EgeoModule],
            schemas: [NO_ERRORS_SCHEMA],
            declarations: [ConsoleBoxComponent]
        }).compileComponents();  // compile template and css
    }));

    beforeEach(async () => {
        fixture = TestBed.createComponent(ConsoleBoxComponent);
        component = fixture.componentInstance;
        el = fixture.debugElement;
    });
});
