/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
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
