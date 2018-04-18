import { getSelectedVersions } from '../../../reducers';
/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Store } from '@ngrx/store';
import { Router } from '@angular/router';

import { ComponentFixture, async, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { WorkflowsManagingHeaderContainer } from './../workflows-header.container';

import { MockStore } from '@test/store-mock';
import * as workflowActions from './../../../actions/workflow-list';

let component: WorkflowsManagingHeaderContainer;
let fixture: ComponentFixture<WorkflowsManagingHeaderContainer>;

const routerStub = {
    navigate: jasmine.createSpy('navigate')
};

describe('[WorkflowsManagingHeaderContainer]', () => {
    const fakeWorkflowList = [
        {
            id: 'id1',
            name: 'workflow1',
            group: '/home',
            version: 1
        },
        {
            id: 'id2',
            name: 'workflow1',
            group: '/home',
            version: 0
        },
        {
            id: 'id3',
            name: 'workflow3',
            group: '/home',
            version: 2
        }
    ];
    const initialStateValue = {
        workflowsManaging: {
            workflowsManaging: {
                workflowList: fakeWorkflowList,
                openedWorkflow: fakeWorkflowList[0],
                selectedVersions: [fakeWorkflowList[0].id],
                selectedVersionsData: [fakeWorkflowList[0]],
                currentLevel: {
                    id: '940800b2-6d81-44a8-84d9-26913a2faea4',
                    name: '/home',
                    label: 'home'
                }
            },
            order: {
                sortOrder: {
                    orderBy: 'name',
                    type: 1
                },
                sortOrderVersions: {
                    orderBy: 'version',
                    type: 1
                }
            }
        }
    };
    const mockStoreInstance: MockStore<any> = new MockStore(initialStateValue);

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [WorkflowsManagingHeaderContainer],
            schemas: [NO_ERRORS_SCHEMA],
            providers: [
                {
                    provide: Store, useValue: mockStoreInstance
                },
                {
                    provide: Router, useValue: routerStub
                },
            ],
        }).compileComponents();  // compile template and css
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(WorkflowsManagingHeaderContainer);
        component = fixture.componentInstance;
        fixture.detectChanges();

    });

    it('should get OnInit selected versions and workflows', () => {
        component.selectedVersionsData$.take(1).subscribe((versions) => {
            expect(versions.length).toBe(1);
            expect(versions[0]).toEqual(fakeWorkflowList[0]);
        });
        component.workflowVersions$.take(1).subscribe((workflows) => {
            expect(workflows.length).toBe(2);
        });
    });

    it('should can edit the selected version', () => {
        component.editVersion(component.selectedVersions[0]);
        expect(routerStub.navigate).toHaveBeenCalledWith(['wizard/edit', component.selectedVersions[0]]);
    });

    describe('should show the correct route in the breadcrumb', () => {
        beforeEach(() => {
            mockStoreInstance.next(initialStateValue);
            fixture.detectChanges();
        });
        it('when a workflow is opened should show its name in the route', () => {
            const value = {
                ...initialStateValue,
                workflowsManaging: {
                    ...initialStateValue.workflowsManaging,
                    workflowsManaging: {
                        ...initialStateValue.workflowsManaging.workflowsManaging,
                        openedWorkflow: fakeWorkflowList[0]
                    }
                }
            };
            mockStoreInstance.next(value);
            fixture.detectChanges();
            expect(component.levelOptions).toEqual(['Home', fakeWorkflowList[0].name]);
        });

        it('when no workflow is opened should show its name in the route', () => {
            const value = {
                ...initialStateValue,
                workflowsManaging: {
                    ...initialStateValue.workflowsManaging,
                    workflowsManaging: {
                        ...initialStateValue.workflowsManaging.workflowsManaging,
                        openedWorkflow: null
                    }
                }
            };
            mockStoreInstance.next(value);
            fixture.detectChanges();
            expect(component.levelOptions).toEqual(['Home']);
        });
    });


    describe('should can display managing header actions', () => {

        beforeEach(() => {
            mockStoreInstance.next(initialStateValue);
            fixture.detectChanges();
            spyOn(mockStoreInstance, 'dispatch');
        });

        it('can dispatch download workflows action', () => {
            const expectedAction = new workflowActions.DownloadWorkflowsAction(component.selectedVersions);
            component.downloadWorkflows();
            expect(mockStoreInstance.dispatch).toHaveBeenCalledWith(expectedAction);
        });

        it('can dispatch select group action', () => {
            const group = {
                name: 'group',
                id: 'id',
                label: 'label'
            };
            const expectedAction = new workflowActions.ChangeGroupLevelAction(group);
            component.selectGroup(group);
            expect(mockStoreInstance.dispatch).toHaveBeenCalledWith(expectedAction);
        });

        it('can dispatch delete workflows action', () => {
            const expectedAction = new workflowActions.DeleteWorkflowAction();
            component.deleteWorkflows();
            expect(mockStoreInstance.dispatch).toHaveBeenCalledWith(expectedAction);
        });

        it('can dispatch delete versions action', () => {
            const expectedAction = new workflowActions.DeleteVersionAction();
            component.deleteVersions();
            expect(mockStoreInstance.dispatch).toHaveBeenCalledWith(expectedAction);
        });

        it('can dispatch changeFolder action to the root group', () => {
            const expectedAction = new workflowActions.ChangeGroupLevelAction('/home');
            component.changeFolder(0); // go to /app
            expect(mockStoreInstance.dispatch).toHaveBeenCalledWith(expectedAction);
        });

        it('can dispatch changeFolder action to the selected group', () => {
            const value = {
                ...initialStateValue,
                workflowsManaging: {
                    ...initialStateValue.workflowsManaging,
                    workflowsManaging: {
                        ...initialStateValue.workflowsManaging.workflowsManaging,
                        currentLevel: {
                            id: '940800b2-6d81-44a8-84d9-26913a2faea4',
                            name: '/home/group/other',
                            label: 'other'
                        }
                    }
                }
            };
            mockStoreInstance.next(value);
            fixture.detectChanges();
            const expectedAction = new workflowActions.ChangeGroupLevelAction('/home/group');
            component.changeFolder(1); // go to /app
            expect(mockStoreInstance.dispatch).toHaveBeenCalledWith(expectedAction);
        });


        it('can dispatch generate version action', () => {
            const expectedAction = new workflowActions.GenerateNewVersionAction();
            component.generateVersion(); // go to /app
            expect(mockStoreInstance.dispatch).toHaveBeenCalledWith(expectedAction);
        });

    });

});

