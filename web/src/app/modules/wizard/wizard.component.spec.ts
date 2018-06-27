
/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { StoreModule, Store } from '@ngrx/store';
import { ComponentFixture, async, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { WizardService } from './services/wizard.service';
import { ActivatedRoute } from '@angular/router';
import { WizardComponent } from '@app/wizard';
import * as fromRoot from '../../reducers';
import * as fromWizard from './reducers';
import * as externalDataActions from './actions/externalData';
import * as wizardActions from './actions/wizard';
import * as debugActions from './actions/debug';

let component: WizardComponent;
let fixture: ComponentFixture<WizardComponent>;
let store: Store<any>;
const fakeWorkflowId = 'fake-id';
describe('[WizardComponent]', () => {
  const activatedRouteInstance = {
    snapshot: {
      params: {
        id: fakeWorkflowId
      }
    }
  };
  const wizardServiceInstance = {};
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        StoreModule.forRoot(fromRoot.reducers),
        StoreModule.forFeature('wizard', fromWizard.reducers),
      ],
      declarations: [WizardComponent],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        { provide: ActivatedRoute, useValue: activatedRouteInstance },
        { provide: WizardService, useValue: wizardServiceInstance }
      ],
    }).compileComponents();  // compile template and css
  }));

  beforeEach(() => {
    store = TestBed.get(Store);
    spyOn(store, 'dispatch').and.callThrough();
    spyOn(store, 'select').and.callThrough();

    fixture = TestBed.createComponent(WizardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should get the environment list on init', () => {
    expect(store.dispatch).toHaveBeenCalledWith(new externalDataActions.GetEnvironmentListAction());
  });


  it('should initialize the wizard with the workflow data when its in edition mode', () => {
    expect(component.isEdit).toBeTruthy();
    expect(store.dispatch).toHaveBeenCalledWith(new wizardActions.ResetWizardAction(true));
    expect(store.dispatch).toHaveBeenCalledWith(new wizardActions.ModifyWorkflowAction(fakeWorkflowId));
    expect(store.dispatch).toHaveBeenCalledWith(new debugActions.GetDebugResultAction(fakeWorkflowId));
  });

});
