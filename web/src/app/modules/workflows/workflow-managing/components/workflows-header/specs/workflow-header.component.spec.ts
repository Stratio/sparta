/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { ComponentFixture, async, TestBed } from '@angular/core/testing';
import { StBreadcrumbsModule, StModalService, StModalResponse } from '@stratio/egeo';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { By } from '@angular/platform-browser';
import { of } from 'rxjs/observable/of';

import { TranslateMockModule } from '@test/translate-stub';
import { Router } from '@angular/router';
import { WorkflowsManagingHeaderComponent } from './../workflows-header.component';
import { WorkflowsManagingService } from './../../../workflows.service';
import { SharedModule } from '@app/shared';


let component: WorkflowsManagingHeaderComponent;
let fixture: ComponentFixture<WorkflowsManagingHeaderComponent>;

let routeMock: Router;
let modalServiceMock: StModalService;

const workflowsManagingStub = jasmine.createSpyObj('WorkflowsManagingService',
   ['createWorkflowGroup', 'showCreateJsonModal', 'stopWorkflow', 'runWorkflow']);


describe('[WorkflowsManagingHeaderComponent]', () => {
   beforeEach(async(() => {
      routeMock = jasmine.createSpyObj('Route', ['navigate']);
      modalServiceMock = jasmine.createSpyObj('StModalService', ['show']);
      (<jasmine.Spy> modalServiceMock.show).and.returnValue(of(StModalResponse.YES));
      TestBed.configureTestingModule({
         imports: [
            StBreadcrumbsModule,
            TranslateMockModule,
            SharedModule
         ],
         providers: [
            { provide: Router, useValue: routeMock },
            { provide: StModalService, useValue: modalServiceMock },
            { provide: WorkflowsManagingService, useValue: workflowsManagingStub }
         ],
         declarations: [WorkflowsManagingHeaderComponent],
         schemas: [NO_ERRORS_SCHEMA]
      }).compileComponents();  // compile template and css
   }));


   beforeEach(() => {
      fixture = TestBed.createComponent(WorkflowsManagingHeaderComponent);
      component = fixture.componentInstance;
      component.levelOptions = ['home'];
   });

   describe('if no entity has been selected, ', () => {

      beforeEach(() => {
         component.selectedWorkflows = [];
         component.selectedVersions = [];
         component.selectedGroupsList = [];
         component.selectedVersionsData = [];
         component.levelOptions = ['home'];
         fixture.detectChanges();
      });

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
         component.selectedGroupsListInner = ['/home/group'];
      });

      it('should can edit group name', () => {
         fixture.detectChanges();
         fixture.debugElement.query(By.css('#edit-workflow-group-button')).triggerEventHandler('click', {});
         const callParams = (<jasmine.Spy>modalServiceMock.show).calls.mostRecent().args[0];
         expect(callParams.modalTitle).toBe(component.renameFolderTitle);
         expect(callParams.inputs).toEqual({
            entityType: 'Group',
            entityName: component.selectedGroupsListInner[0]
         });
      });

      it('should can move a group', () => {
         fixture.detectChanges();
         fixture.debugElement.query(By.css('#move-group-button')).triggerEventHandler('click', {});
         const callParams = (<jasmine.Spy>modalServiceMock.show).calls.mostRecent().args[0];
         expect(callParams.modalTitle).toBe(component.moveGroupTitle);
         expect(callParams.inputs).toEqual({
            workflow: null,
            currentGroup: component.selectedGroupsListInner[0]
         });
      });


      it('should can remove a group', () => {
         fixture.detectChanges();
         fixture.debugElement.query(By.css('#delete-button')).triggerEventHandler('click', {});
         const callParams = (<jasmine.Spy>modalServiceMock.show).calls.mostRecent().args[0];
         expect(callParams.modalTitle).toBe(component.deleteModalTitle);
         expect(callParams.messageTitle).toBe(component.deleteWorkflowModalMessage);
      });
   });


   describe('if a workflow is selected, ', () => {
      beforeEach(() => {
         component.selectedWorkflowsInner = ['workflow1'];
      });

      it('should can edit workflow name', () => {
         fixture.detectChanges();
         fixture.debugElement.query(By.css('#edit-workflow-group-button')).triggerEventHandler('click', {});
         const callParams = (<jasmine.Spy>modalServiceMock.show).calls.mostRecent().args[0];
         expect(callParams.modalTitle).toBe(component.renameWorkflowTitle);
         expect(callParams.inputs).toEqual({
            entityType: 'Workflow',
            entityName: component.selectedWorkflowsInner[0]
         });
      });

      it('should can move a workflow', () => {
         fixture.detectChanges();
         fixture.debugElement.query(By.css('#move-group-button')).triggerEventHandler('click', {});
         const callParams = (<jasmine.Spy>modalServiceMock.show).calls.mostRecent().args[0];
         expect(callParams.modalTitle).toBe(component.moveGroupTitle);
         expect(callParams.inputs).toEqual({
            workflow: component.selectedWorkflowsInner[0],
            currentGroup: null
         });
      });

      it('should can remove a workflow', () => {
         fixture.detectChanges();
         fixture.debugElement.query(By.css('#delete-button')).triggerEventHandler('click', {});
         const callParams = (<jasmine.Spy>modalServiceMock.show).calls.mostRecent().args[0];
         expect(callParams.modalTitle).toBe(component.deleteModalTitle);
         expect(callParams.messageTitle).toBe(component.deleteWorkflowModalMessage);
      });
   });

   describe('if a version is selected', () => {
      beforeEach(() => {
         const fakeVersion = {
            id: 'id',
            name: 'workflow-version-name',
            version: 1,
            group: '/home',
            status: {
               status: 'Started'
            }
         };
         component.selectedVersionsData = [fakeVersion];
         component.selectedVersionsInner = [fakeVersion.id]; // versions ids
         component.selectedVersions = [fakeVersion.id]; // versions ids
      });

      it('should show the run button with the stop icon if the version status is Running', () => {
         fixture.detectChanges();
         const runDebugElement = fixture.debugElement.query(By.css('#run-workflow-button'));
         expect(runDebugElement).not.toBeNull();
         expect(runDebugElement.children[0].nativeElement.className).toBe('icon-stop');
         runDebugElement.triggerEventHandler('click', {});
         expect(workflowsManagingStub.stopWorkflow).toHaveBeenCalled();
      });

      it('should show the run button with the play icon if the version status is Stopped', () => {
         const fakeVersion = {
            id: 'id',
            name: 'workflow-version-name',
            version: 1,
            group: '/home',
            status: {
               status: 'Stopped'
            }
         };
         component.selectedVersionsData = [fakeVersion];
         fixture.detectChanges();
         const runDebugElement = fixture.debugElement.query(By.css('#run-workflow-button'));
         expect(runDebugElement).not.toBeNull();
         expect(runDebugElement.children[0].nativeElement.className).toBe('icon-play');
         runDebugElement.triggerEventHandler('click', {});
         expect(workflowsManagingStub.runWorkflow).toHaveBeenCalled();
      });

      it('should can download the selected version', () => {
         fixture.detectChanges();
         spyOn(component.downloadWorkflows, 'emit');
         const runDebugElement = fixture.debugElement.query(By.css('#download-button'));
         expect(runDebugElement).not.toBeNull();
         runDebugElement.triggerEventHandler('click', {});
         expect(component.downloadWorkflows.emit).toHaveBeenCalled();
      });

      it('should can edit the selected version', () => {
         fixture.detectChanges();
         spyOn(component.onEditVersion, 'emit');
         const runDebugElement = fixture.debugElement.query(By.css('#edit-version-button'));
         expect(runDebugElement).not.toBeNull();
         runDebugElement.triggerEventHandler('click', {});
         expect(component.onEditVersion.emit).toHaveBeenCalled();
      });


      it('should can remove a version', () => {
         fixture.detectChanges();
         fixture.debugElement.query(By.css('#delete-button')).triggerEventHandler('click', {});
         const callParams = (<jasmine.Spy>modalServiceMock.show).calls.mostRecent().args[0];
         expect(callParams.modalTitle).toBe(component.deleteModalTitle);
         expect(callParams.messageTitle).toBe(component.deleteWorkflowModalMessage);
      });

      it('should can create a new version', () => {
         fixture.detectChanges();
         spyOn(component.generateVersion, 'emit');
         fixture.debugElement.query(By.css('#version-option')).triggerEventHandler('click', {});
         expect(component.generateVersion.emit).toHaveBeenCalled();
      });

      it('should can create a workflow from a version', () => {
         fixture.detectChanges();
         fixture.debugElement.query(By.css('#workflow-option')).triggerEventHandler('click', {});
         const callParams = (<jasmine.Spy>modalServiceMock.show).calls.mostRecent().args[0];
         expect(callParams.modalTitle).toBe(component.duplicateWorkflowTitle);
      });
   });

   describe('other actions can be performed', () => {

      beforeEach(() => fixture.detectChanges());

      it('should can create a folder', () => {
         fixture.debugElement.query(By.css('#group-option')).triggerEventHandler('click', {});
         expect(workflowsManagingStub.createWorkflowGroup).toHaveBeenCalled();
      });

      it('should can create a workflow from json file', () => {
         fixture.debugElement.query(By.css('#file-option')).triggerEventHandler('click', {});
         expect(workflowsManagingStub.showCreateJsonModal).toHaveBeenCalled();
      });

      it('should can create an emty batch workflow', () => {
         fixture.debugElement.query(By.css('#batch-option')).triggerEventHandler('click', {});
         expect(routeMock.navigate).toHaveBeenCalledWith(['wizard/batch']);
      });

      it('should can create an empty streaming workflow', () => {
         fixture.debugElement.query(By.css('#streaming-option')).triggerEventHandler('click', {});
         expect(routeMock.navigate).toHaveBeenCalledWith(['wizard/streaming']);
      });
   });

   describe('an info button is showed', () => {
      let infoButton;

      beforeEach(() => {
         infoButton = fixture.debugElement.query(By.css('#info-button'));
      });

      it('should be active when the info bar is opened', () => {
         component.showDetails = true;
         fixture.detectChanges();
         expect(infoButton.nativeElement.className.indexOf('selected-button') > -1).toBeTruthy();
      });

      it('should be inactive when the info bar is closed', () => {
         component.showDetails = false;
         fixture.detectChanges();
         expect(infoButton.nativeElement.className.indexOf('selected-button') > -1).toBeFalsy();
      });

      it('should change info bar status when is clicked', () => {
         fixture.detectChanges();
         spyOn(component.showWorkflowInfo, 'emit');
         infoButton.triggerEventHandler('click', {});
         expect(component.showWorkflowInfo.emit).toHaveBeenCalled();
      });
   });
});
