/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { ComponentFixture, async, TestBed, fakeAsync } from '@angular/core/testing';
import { StTableModule, StModalService } from '@stratio/egeo';
import { DebugElement, NO_ERRORS_SCHEMA } from '@angular/core';
import { By } from '@angular/platform-browser';
import { Router } from '@angular/router';

import { RepositoryTableComponent } from './../repository-table.component';
import { TranslateMockModule } from '@test/translate-stub';
import { MenuOptionsListModule } from '@app/shared/components/menu-options-list/menu-options-list.module';
import {WorkflowsManagingService} from '@app/repository/workflows.service';
import {VersionMenuService} from '@app/repository/services/version-menu.service';


let component: RepositoryTableComponent;
let fixture: ComponentFixture<RepositoryTableComponent>;
let tableEl: DebugElement;
let versionEl: DebugElement;

const fakeGroups = [{
  id: 1,
  name: '/home/group'
}];

const fakeWorkflows = [
  {
    name: 'workflow1',
    type: 'Streaming'
  }
];

const fakeVersions = [
  {
    id: '1',
    name: 'version1',
    status: {}
  },
  {
    id: '2',
    name: 'version2',
    status: {}
  }
];

const routerStub = {
  navigate: jasmine.createSpy('navigate')
};

describe('[RepositoryTableComponent]', () => {
  let modalServiceMock: StModalService;
  let versionMenuServiceMock: VersionMenuService;

  beforeEach(async(() => {
    modalServiceMock = jasmine.createSpyObj('StModalService', ['show']);
    versionMenuServiceMock = jasmine.createSpyObj('VersionMenuService', ['getVersionMenu']);

    TestBed.configureTestingModule({
      imports: [
        StTableModule,
        TranslateMockModule,
        MenuOptionsListModule
      ],
      providers: [
        { provide: Router, useValue: routerStub },
        { provide: StModalService, useValue: modalServiceMock },
        { provide: VersionMenuService, useValue: versionMenuServiceMock },
        { provide: WorkflowsManagingService, useValue: WorkflowsManagingService },
      ],
      declarations: [RepositoryTableComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();  // compile template and css
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RepositoryTableComponent);
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

    it('if there are one workflows and a group should show three rows (rows + 1 table header)', () => {
      expect(fixture.nativeElement.querySelectorAll('tr').length).toBe(3);
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

    it('should select a group when the row its clicked', () => {
      let selectedGroup = '';
      component.selectGroup.subscribe((groupName: string) => selectedGroup = groupName);
      const rowSelector = fixture.debugElement.query(By.css('.workflow-table .group-row td:first-child'));
      rowSelector.triggerEventHandler('click', { name: fakeGroups[0].name });
      expect(selectedGroup).toBe(fakeGroups[0].name);
    });

    it('should select a workflow when the row its clicked', () => {
      let selectedWorkflow = '';
      component.selectWorkflow.subscribe((workflowName: string) => selectedWorkflow = workflowName);
      const rowSelector = fixture.debugElement.query(By.css('.workflow-table .workflow-row td:first-child'));
      rowSelector.triggerEventHandler('click', { name: fakeWorkflows[0].name });
      expect(selectedWorkflow).toBe(fakeWorkflows[0].name);
    });

    it('should show workflow versions when click on its name but this should not be selected', () => {
      spyOn(component, 'checkWorkflow');
      let selectedWorkflow: any;
      component.openWorkflow.subscribe((workflow: any) => selectedWorkflow = workflow);
      const nameSelector = fixture.debugElement.query(By.css('.workflow-table .workflow-name'));
      const event = {
        stopPropagation: function () { }
      };
      spyOn(event, 'stopPropagation');
      nameSelector.triggerEventHandler('click', event);
      expect(selectedWorkflow).toBe(fakeWorkflows[0]);
      expect(event.stopPropagation).toHaveBeenCalled();
    });

    it('should show workflow versions when click on its name but this should not be selected', () => {
      spyOn(component, 'checkWorkflow');
      let selectedWorkflow: any;
      component.openWorkflow.subscribe((workflow: any) => {
        selectedWorkflow = workflow;
      });
      const nameSelector = fixture.debugElement.query(By.css('.workflow-table .workflow-name'));
      const event = {
        stopPropagation: function () { }
      };
      spyOn(event, 'stopPropagation');
      nameSelector.triggerEventHandler('click', event);
      expect(selectedWorkflow).toBe(fakeWorkflows[0]);
      expect(event.stopPropagation).toHaveBeenCalled();
    });


    it('the workflow row must show the correct workflow type icon', () => {
      const iconSelector = fixture.debugElement.query(By.css('.streaming-icon'));
      expect(iconSelector).toBeDefined();
    });

    it('should open group when click on its name but this should not be selected', () => {
      spyOn(component, 'checkGroup');
      let selectedGroup: any;
      component.changeFolder.subscribe((group: any) => selectedGroup = group);
      const nameSelector = fixture.debugElement.query(By.css('.workflow-table .group-name'));
      const event = {
        stopPropagation: function () { }
      };
      spyOn(event, 'stopPropagation');
      nameSelector.triggerEventHandler('click', event);
      expect(selectedGroup).toBe(fakeGroups[0]);
      expect(event.stopPropagation).toHaveBeenCalled();
    });


    it('the workflow row must show the correct workflow type icon', () => {
      const iconSelector = fixture.debugElement.query(By.css('.streaming-icon'));
      expect(iconSelector).toBeDefined();
    });
  });

  describe('when a workflow opened should show a version list table ', () => {

    beforeEach(() => {
      component.workflowVersions = fakeVersions;
      fixture.detectChanges();
      versionEl = fixture.debugElement.query(By.css('.version-table'));
    });

    it('should hide workflow and groups table', () => {
      expect(fixture.debugElement.query(By.css('.workflow-table'))).toBeNull();
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

    it('should select a version when the row its clicked', () => {
      let selectedVersion = '';
      component.selectVersion.subscribe((versionId: string) => selectedVersion = versionId);
      const rowSelector = fixture.debugElement.query(By.css('.version-table .workflow-versions td:first-child'));
      rowSelector.triggerEventHandler('click', { id: 'versionId' });
      expect(selectedVersion.length).not.toBe(0);
    });

    it('should redirect to workflow edit when the version name is clicked', fakeAsync(() => {
      const labelSelector = fixture.debugElement.query(By.css('.version-table .workflow-link'));
      const event = {
        stopPropagation: function () { }
      };
      labelSelector.triggerEventHandler('click', event);
      expect(routerStub.navigate).toHaveBeenCalledWith(['wizard/edit', '1']);
    }));

    it('the version title must show the correct workflow type icon', () => {
      const iconSelector = fixture.debugElement.query(By.css('.streaming-icon'));
      expect(iconSelector).toBeDefined();
    });

  });
});
