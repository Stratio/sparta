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

import { SimpleChange } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { Http } from '@angular/http';
import { RouterTestingModule } from '@angular/router/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { By } from '@angular/platform-browser';

import { WorkflowDetailComponent } from "./workflow-detail.component";

describe('WorkflowDetailComponent', () => {
    let component: WorkflowDetailComponent;
    let fixture: ComponentFixture<WorkflowDetailComponent>;

    beforeEach(
        async(() => {
            TestBed.configureTestingModule({
                declarations: [WorkflowDetailComponent],
                schemas: [NO_ERRORS_SCHEMA]
            }).compileComponents(); // compile template and css
        })
    );

    beforeEach(() => {
        fixture = TestBed.createComponent(WorkflowDetailComponent);
        component = fixture.componentInstance;
        component.workflowData = {
            name: 'workflow-name'
        };
    });


    xit('Should recalculate initPos and elements to show when changes', () => {
        fixture.detectChanges();
        expect(fixture.debugElement.query(By.css('h3')).nativeElement.textContent).toBe('workflow-name');
    });
});