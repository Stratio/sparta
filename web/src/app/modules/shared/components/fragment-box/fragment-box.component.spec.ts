///
/// Copyright (C) 2015 Stratio (http://stratio.com)
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///         http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

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
