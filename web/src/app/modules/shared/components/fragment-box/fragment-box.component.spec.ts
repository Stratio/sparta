/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { TestBed, async, ComponentFixture } from '@angular/core/testing';
import { FragmentBoxComponent } from './fragment-box.component';
import { DebugElement, NO_ERRORS_SCHEMA  } from '@angular/core';
import { By } from '@angular/platform-browser';
import { EgeoModule } from '@stratio/egeo';


let component: FragmentBoxComponent;
let fixture: ComponentFixture<FragmentBoxComponent>;
let el: DebugElement;

describe('FragmentBoxComponent', () => {
    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [EgeoModule],
            schemas: [NO_ERRORS_SCHEMA],
            declarations: [FragmentBoxComponent]
        }).compileComponents();  // compile template and css
    }));

    beforeEach(async () => {
        fixture = TestBed.createComponent(FragmentBoxComponent);
        component = fixture.componentInstance;
        component.fragmentData = {
            name: 'Fragment-name',
            description: 'Fragment-description',
            classPrettyName: 'Kafka'
        };
        el = fixture.debugElement;
    });

    it('should show fragment name and description', () => {
        fixture.detectChanges();
        const name: HTMLElement = fixture.debugElement.query(By.css('.name')).nativeElement;
        expect(name.textContent).toContain(component.fragmentData.name);

        const description: HTMLElement = fixture.debugElement.query(By.css('.description')).nativeElement;
        expect(description.textContent).toContain(component.fragmentData.description);
    });

    it('should show fragment icon', () => {
        fixture.detectChanges();
        const icon: HTMLElement = fixture.debugElement.query(By.css('.fragment-icon')).nativeElement;
        expect(icon.textContent.length).toBeGreaterThan(0);
    });
});
