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

import { ComponentFixture, async, TestBed, fakeAsync } from '@angular/core/testing';
import { StTableModule, StBreadcrumbsModule } from '@stratio/egeo';
import { DebugElement, NO_ERRORS_SCHEMA } from '@angular/core';
import { By } from '@angular/platform-browser';

import { TranslateMockModule } from '@test/translate-stub';
import { Router } from '@angular/router';
import { WorkflowsManagingHeaderComponent } from './workflows-header.component';


let component: WorkflowsManagingHeaderComponent;
let fixture: ComponentFixture<WorkflowsManagingHeaderComponent>;
let tableEl: DebugElement;
let versionEl: DebugElement;



const routerStub = {
  navigate: jasmine.createSpy('navigate')
};

describe('[WorkflowsManagingHeaderComponent]', () => {
    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [
                StBreadcrumbsModule,
                TranslateMockModule,
            ],
            providers: [
                { provide: Router, useValue: routerStub },
            ],
            declarations: [WorkflowsManagingHeaderComponent],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();  // compile template and css
    }));


    beforeEach(() => {
        fixture = TestBed.createComponent(WorkflowsManagingHeaderComponent);
        component = fixture.componentInstance;
    });

});
