/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { ComponentFixture, async, TestBed, fakeAsync } from '@angular/core/testing';
import { StTableModule } from '@stratio/egeo';
import { DebugElement, NO_ERRORS_SCHEMA } from '@angular/core';
import { By } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { Store } from "@ngrx/store";

import { WorkflowsManagingTableContainer } from './../workflows-table.container';

import { MockStore } from '@test/store-mock';


let component: WorkflowsManagingTableContainer;
let fixture: ComponentFixture<WorkflowsManagingTableContainer>;
let tableEl: DebugElement;
let versionEl: DebugElement;

describe('[WorkflowsManagingTableContainer]', () => {

    const mockStoreInstance: MockStore<any> = new MockStore({
        workflowsManaging: {
            workflowsManaging: {
                workflowList: [
                    {
                        name: 'workflow1'
                    },
                    {
                        name: 'workflow2'
                    }
                ]
            }
        }
    });

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [WorkflowsManagingTableContainer],
            schemas: [NO_ERRORS_SCHEMA],
            providers: [
                { provide: Store, useValue: mockStoreInstance },
            ],
        }).compileComponents();  // compile template and css
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(WorkflowsManagingTableContainer);
        component = fixture.componentInstance;
        fixture.detectChanges();

    });

    it('should get OnInit the current versions list', () => {
        component.workflowVersions$.take(1).subscribe((versions) => {

        });
    });
});
