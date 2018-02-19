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

import { ComponentFixture, async, TestBed } from '@angular/core/testing';
import { StTableModule } from '@stratio/egeo';
import { DebugElement, NO_ERRORS_SCHEMA } from '@angular/core';
import { By } from '@angular/platform-browser';

import { WorkflowsManagingTableComponent } from './workflows-table.component';
import { TranslateMockModule } from '@test/translate-stub';
import { RouterTestingModule } from '@angular/router/testing';

let component: WorkflowsManagingTableComponent;
let fixture: ComponentFixture<WorkflowsManagingTableComponent>;
let tableEl: DebugElement;
let versionEl: DebugElement;

const fakeGroups = [{
    id: 1,
    name: '/home'
}];

const fakeWorkflows = [
    {
        name: 'workflow1'
    },
    {
        name: 'workflow2'
    }
];

const fakeVersions = [
    {
        name: 'version1',
        status: {}
    },
    {
        name: 'version2',
        status: {}
    }
]

describe('[WorkflowsManagingTableComponent]', () => {
    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [
                StTableModule,
                TranslateMockModule,
                RouterTestingModule.withRoutes([]),
            ],
            declarations: [WorkflowsManagingTableComponent],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();  // compile template and css
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(WorkflowsManagingTableComponent);
        component = fixture.componentInstance;
        component.groupList = fakeGroups;
        component.workflowList = fakeWorkflows;
        component.selectedWorkflows = [];
        component.selectedVersions = [];
        component.workflowVersions = [];
    });

    describe('should show a workflows and group list table', () => {

        beforeEach(() => {
            fixture.detectChanges();
            tableEl = fixture.debugElement.query(By.css('.workflow-table'));
        });

        it('if there are two workflows and a group should show four rows (rows + 1 table header)', () => {
            expect(fixture.nativeElement.querySelectorAll('tr').length).toBe(4);
        });

        it('should send change order event', () => {
            let currentOrder: any = {
                name: '',
                orderBy: false
            };
            component.onChangeOrder.subscribe((order: any) => currentOrder = order);
            tableEl.triggerEventHandler('changeOrder', {
                orderBy: 'name',
                sortOrder: true
            });
            expect(currentOrder.orderBy).toBe('name');
        });

        xit('should select a workflow when the row its clicked', () => {

        });

        xit('should select a group when the row its clicked', () => {
            
        });

    });

    describe('when a workflow opened should show a version list table ', () => {

        beforeEach(() => {
            component.workflowVersions = fakeVersions;
            fixture.detectChanges();
            versionEl = fixture.debugElement.query(By.css('.version-table'));
        });

        it('should hide workflow and groups table', () => {
            const tableEl = fixture.debugElement.query(By.css('.workflow-table'));
            expect(tableEl).toBeNull();
        });

        it('should show versions table', () => {
            expect(versionEl).not.toBeNull();
        });

        it('if there are two versions show three rows', () => {
            expect(fixture.nativeElement.querySelectorAll('tr').length).toBe(3);
        });

        it('should send change order event when version table column order is changed', () => {
            let currentOrder: any = {
                name: '',
                orderBy: false
            };
            component.onChangeOrderVersions.subscribe((order: any) => currentOrder = order);
            versionEl.triggerEventHandler('changeOrder', {
                orderBy: 'name',
                sortOrder: true
            });
            expect(currentOrder.orderBy).toBe('name');
        });

        xit('should select a versopm when the row its clicked', () => {
            
        });
    });
});
