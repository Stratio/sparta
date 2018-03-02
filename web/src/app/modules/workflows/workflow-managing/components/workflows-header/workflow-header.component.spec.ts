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
import { StTableModule, StBreadcrumbsModule, StModalService } from '@stratio/egeo';
import { DebugElement, NO_ERRORS_SCHEMA } from '@angular/core';
import { By } from '@angular/platform-browser';

import { TranslateMockModule } from '@test/translate-stub';
import { Router } from '@angular/router';
import { WorkflowsManagingHeaderComponent } from './workflows-header.component';
import { WorkflowsManagingService } from './../../workflows.service';


let component: WorkflowsManagingHeaderComponent;
let fixture: ComponentFixture<WorkflowsManagingHeaderComponent>;

const routerStub = {
    navigate: jasmine.createSpy('navigate')
};

const stModalServiceStub = {
    show: jasmine.createSpy('show')
};

const workflowsManagingStub = {

}

describe('[WorkflowsManagingHeaderComponent]', () => {
    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [
                StBreadcrumbsModule,
                TranslateMockModule,
            ],
            providers: [
                { provide: Router, useValue: routerStub },
                { provide: StModalService, useValue: stModalServiceStub },
                { provide: WorkflowsManagingService, useValue: workflowsManagingStub }
            ],
            declarations: [WorkflowsManagingHeaderComponent],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();  // compile template and css
    }));


    beforeEach(() => {
        fixture = TestBed.createComponent(WorkflowsManagingHeaderComponent);
        component = fixture.componentInstance;
        component.selectedWorkflows = [];
        component.selectedVersions = [];
        component.selectedGroupsList = [];
        component.selectedVersionsData = [];
        component.levelOptions = ['home'];
        fixture.detectChanges();
    });

    describe('if no entity has been selected, ', () => {

        it('the edit button should not be displayed', () => {
            expect(fixture.debugElement.query(By.css('#edit-workflow-group-button'))).toBeNull();
        });

        it('the run workflow button should not be displayed', () => {
            expect(fixture.debugElement.query(By.css('#run-workflow-button'))).toBeNull();
        });

        it('the download button should not be displayed', () => {
            expect(fixture.debugElement.query(By.css('#download-button'))).toBeNull();
        });

        it('the edit version button should not be displayed', () => {
            expect(fixture.debugElement.query(By.css('#edit-version-button'))).toBeNull();
        });

        it('the move group button should not be displayed', () => {
            expect(fixture.debugElement.query(By.css('#move-group-button'))).toBeNull();
        });

        it('the delete button should not be displayed', () => {
            expect(fixture.debugElement.query(By.css('#delete-button'))).toBeNull();
        });
    });

    describe('if a group is selected, ', () => {
        beforeEach(() => {
            component.selectedGroupsList = ['/home/group'];
            fixture.detectChanges();
        });

        xit('should can edit group name', () => {
            fixture.debugElement.query(By.css('#edit-workflow-group-button')).triggerEventHandler('click', {});
        });

    });
});
