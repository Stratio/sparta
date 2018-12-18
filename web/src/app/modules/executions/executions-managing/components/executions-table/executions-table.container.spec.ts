/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Store } from '@ngrx/store';
import { CommonModule } from '@angular/common';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MockStore } from '@test/store-mock';
import { StModalService } from '@stratio/egeo';

import * as executionActions from './../../actions/executions';
import { ExecutionsTableContainer } from './executions-table.container';

describe('ExecutionTableComponent', () => {

  let component: ExecutionsTableContainer;
  let fixture: ComponentFixture<ExecutionsTableContainer>;
  let compiled: Element;

  const mockStoreInstance: MockStore<any> = new MockStore({
    executions: {
      executions: {
        isArchivedPage: false,
        executionList: [
          {
            id: 'e3d38a8a-c7b5-46b9-8dc9-a60f214f3d35'
          },
          {
            id: '2e364ed5-2802-496b-ad04-f9d38fe803c4'
          },
          {
            id: '470612f9-94bc-47cb-babc-ad1d38e0966b'
          }
        ],
        archivedExecutionList: [],
        executionInfo: null,
        loading: false,
        loadingArchived: true,
        selectedExecutionsIds: [],
        statusFilter: '',
        typeFilter: '',
        timeIntervalFilter: 0,
        searchQuery: '',
        pagination: {
          perPage: 10,
          currentPage: 1,
          total: 3
        },
        order: {
          orderBy: 'startDateWithStatus',
          type: 0
        }
      },
      execution: {
        execution: null,
        loading: false
      }
    }
  });

  beforeEach(async(() => TestBed.configureTestingModule({
    declarations: [ ExecutionsTableContainer ],
    schemas: [NO_ERRORS_SCHEMA],
    providers: [
      { provide: Store, useValue: mockStoreInstance },
      { provide: StModalService, useValue: {} }
    ]
  }).compileComponents()));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExecutionsTableContainer);
    component = fixture.componentInstance;
    fixture.detectChanges();
    compiled = fixture.debugElement.nativeElement;
    spyOn(mockStoreInstance, 'dispatch');
  });

  it('All checkboxes should be checked', () => {
    const expectedAction = new executionActions.ChangeExecutionsSelectAllExecutions();
    component.toggleAllExecutions(true);
    expect(mockStoreInstance.dispatch).toHaveBeenCalledWith(expectedAction);
  });

  it('All checkboxes should be unchecked', () => {
    const expectedAction = new executionActions.ChangeExecutionsDeselectAllExecutions();
    component.toggleAllExecutions(false);
    expect(mockStoreInstance.dispatch).toHaveBeenCalledWith(expectedAction);
  });

});
