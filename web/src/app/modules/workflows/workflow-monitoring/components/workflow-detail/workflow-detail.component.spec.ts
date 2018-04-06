/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { Http } from '@angular/http';
import { RouterTestingModule } from '@angular/router/testing';
import { By } from '@angular/platform-browser';

import { WorkflowDetailComponent } from './workflow-detail.component';
import { MonitoringWorkflow } from './../../models/workflow';

describe('WorkflowDetailComponent', () => {
    let component: WorkflowDetailComponent;
    let fixture: ComponentFixture<WorkflowDetailComponent>;

    const fakeWorkflow: MonitoringWorkflow = {
        id: '0',
        name: 'workflow-name',
        description: 'description',
        nodes: [],
        executionEngine: 'Batch',
        version: 0,
        group: '/home',
    };

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
        component.workflowData = fakeWorkflow;
    });


    xit('Should recalculate initPos and elements to show when changes', () => {
        fixture.detectChanges();
        expect(fixture.debugElement.query(By.css('h3')).nativeElement.textContent).toBe('workflow-name');
    });
});
