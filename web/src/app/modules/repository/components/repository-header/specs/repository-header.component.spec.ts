import { ToolBarModule } from '../../../../shared/components/tool-bar/tool-bar.module';
/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { ComponentFixture, async, TestBed } from '@angular/core/testing';
import {  StModalService, StModalResponse } from '@stratio/egeo';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { of } from 'rxjs';

import { TranslateMockModule, initTranslate } from '@test/translate-stub';
import { Router } from '@angular/router';
import { RepositoryHeaderComponent } from './../repository-header.component';
import { WorkflowsManagingService } from './../../../workflows.service';
import { MenuOptionsListModule } from '@app/shared/components/menu-options-list/menu-options-list.module';

import { BreadcrumbMenuService } from 'app/services';


let component: RepositoryHeaderComponent;
let fixture: ComponentFixture<RepositoryHeaderComponent>;

let routeMock: Router;
let modalServiceMock: StModalService;

const workflowsManagingStub = jasmine.createSpyObj('WorkflowsManagingService',
   ['createWorkflowGroup', 'showCreateJsonModal', 'stopWorkflow', 'runWorkflow']);

const breadcrumbMenuService = jasmine.createSpyObj('BreadcrumbMenuService', ['getOptions']);
/*
describe('[RepositoryHeaderComponent]', () => {
   beforeEach(async(() => {
      routeMock = jasmine.createSpyObj('Route', ['navigate']);
      modalServiceMock = jasmine.createSpyObj('StModalService', ['show']);
      (<jasmine.Spy> modalServiceMock.show).and.returnValue(of(StModalResponse.YES));
      TestBed.configureTestingModule({
         imports: [
            TranslateMockModule,
            MenuOptionsListModule,
            ToolBarModule
         ],
         providers: [
            { provide: Router, useValue: routeMock },
            { provide: StModalService, useValue: modalServiceMock },
            { provide: WorkflowsManagingService, useValue: workflowsManagingStub },
            { provide: BreadcrumbMenuService, useValue: breadcrumbMenuService }
         ],
         declarations: [RepositoryHeaderComponent],
         schemas: [NO_ERRORS_SCHEMA]
      }).compileComponents();  // compile template and css
   }));


   beforeEach(() => {
      initTranslate();
      fixture = TestBed.createComponent(RepositoryHeaderComponent);
      component = fixture.componentInstance;
      component.levelOptions = ['home'];
       fixture.detectChanges();
   });

   afterEach(() => {
      fixture.destroy();
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
         expect(fixture.nativeElement.querySelector('#edit-workflow-group-button')).toBeNull();
      });

      it('the run workflow button should not be displayed', () => {
         expect(fixture.nativeElement.querySelector('#run-workflow-button')).toBeNull();
      });

      it('the download button should not be displayed', () => {
         expect(fixture.nativeElement.querySelector('#download-button')).toBeNull();
      });

      it('the move group button should not be displayed', () => {
         expect(fixture.nativeElement.querySelector('#move-group-button')).toBeNull();
      });

      it('the delete button should not be displayed', () => {
         expect(fixture.nativeElement.querySelector('#delete-button')).toBeNull();
      });
   });

   describe('if a group is selected, ', () => {
      beforeEach(() => {
         component.selectedGroupsList = ['/home/group'];
         component.selectedWorkflows = [];
         fixture.detectChanges();
      });

      xit('should can remove a group', () => {
         fixture.nativeElement.querySelector('#delete-button').click();
         const callParams = (<jasmine.Spy>modalServiceMock.show).calls.mostRecent().args[0];
         expect(callParams.modalTitle).toBe(component.deleteModalTitle);
         expect(callParams.messageTitle).toBe(component.deleteWorkflowModalMessage);
      });
   });

   describe('if a workflow is selected, ', () => {
      beforeEach(() => {
         component.selectedWorkflowsInner = ['workflow1'];
      });

      xit('should can remove a workflow', () => {
         fixture.nativeElement.querySelector('#delete-button').click();
         const callParams = (<jasmine.Spy>modalServiceMock.show).calls.mostRecent().args[0];
         expect(callParams.modalTitle).toBe(component.deleteModalTitle);
         expect(callParams.messageTitle).toBe(component.deleteWorkflowModalMessage);
      });
   });

   describe('if a version is selected', () => {
      const fakeVersion = {
         id: 'id',
         name: 'workflow-version-name',
         version: 1,
         group: '/home',
         status: {
            status: 'Started'
         }
      };
      beforeEach(() => {
         component.selectedVersionsData = [fakeVersion];
         component.selectedVersionsInner = [fakeVersion.id]; // versions ids
         component.selectedVersions = [fakeVersion.id]; // versions ids
         fixture.detectChanges();
      });

      xit('should can download the selected version', () => {
         fixture.detectChanges();
         spyOn(component.downloadWorkflows, 'emit');
         const runDebugElement: HTMLButtonElement = fixture.nativeElement.querySelector('#download-button');
         expect(runDebugElement).not.toBeNull();
         runDebugElement.click();
         expect(component.downloadWorkflows.emit).toHaveBeenCalled();
      });

      xit('should can remove a version', () => {
         fixture.detectChanges();
         fixture.nativeElement.querySelector('#delete-button').click();
         const callParams = (<jasmine.Spy>modalServiceMock.show).calls.mostRecent().args[0];
         expect(callParams.modalTitle).toBe(component.deleteModalTitle);
         expect(callParams.messageTitle).toBe(component.deleteWorkflowModalMessage);
      });

   });

   describe('other actions can be performed', () => {
      beforeEach(() =>  {
         fixture.nativeElement.querySelector('#create-entity-button').click();
         fixture.detectChanges();
      });
      it('should can create a folder', () => {
         fixture.nativeElement.querySelector('#group-option').click();
         expect(workflowsManagingStub.createWorkflowGroup).toHaveBeenCalled();
      });

      it('should can create a workflow from json file', () => {
         fixture.nativeElement.querySelector('#file-option').click();
         expect(workflowsManagingStub.showCreateJsonModal).toHaveBeenCalled();
      });

      it('should can create an emty batch workflow', () => {
         fixture.nativeElement.querySelector('#batch-option').click();
         expect(routeMock.navigate).toHaveBeenCalledWith(['wizard/batch']);
      });

      it('should can create an empty streaming workflow', () => {
         fixture.nativeElement.querySelector('#streaming-option').click();
         expect(routeMock.navigate).toHaveBeenCalledWith(['wizard/streaming']);
      });
   });

   describe('an info button is showed', () => {
      let infoButton;

      beforeEach(() => {
         infoButton = fixture.nativeElement.querySelector('#info-button');
      });

      xit('should be active when the info bar is opened', () => {
         component.showDetails = true;
         fixture.detectChanges();

         expect(infoButton.classList).toContain('selected-button');
      });

      it('should be inactive when the info bar is closed', () => {
         component.showDetails = false;
         fixture.detectChanges();

         expect(infoButton.classList).not.toContain('selected-button');
      });

      it('should change info bar status when is clicked', () => {
         spyOn(component.showWorkflowInfo, 'emit');
         infoButton.click();
         expect(component.showWorkflowInfo.emit).toHaveBeenCalled();
      });
   });
});

*/
