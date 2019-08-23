/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { Injectable, OnDestroy } from '@angular/core';
import { annotationColors, WizardAnnotation, WizardAnnotationModel } from './wizard-annotation.model';
import { Store, select } from '@ngrx/store';

import * as fromRoot from 'reducers';
import { Subscription } from 'rxjs';

 @Injectable()
export class WizardAnnotationService implements OnDestroy {

   private _username: string;
  private _usernameSubscription: Subscription;

   constructor(private _store: Store<fromRoot.State>) {
    this._usernameSubscription = this._store.pipe(select(fromRoot.getUsername))
      .subscribe(username => this._username = username);
  }

   ngOnDestroy(): void {
    if (this._usernameSubscription) {
      this._usernameSubscription.unsubscribe();
    }
  }

   public createDraggableNote(event): WizardAnnotation {
    return {
      ...this._createAnnotation(),
      position: event,
      openOnCreate: true
    };
  }

   public createEdgeNote(event): WizardAnnotation {
    return {
      ...this._createAnnotation(),
      edge: event,
      openOnCreate: true
    };
  }

   public createStepNote(name: string): WizardAnnotation {
    return {
      ...this._createAnnotation(),
      stepName: name,
      openOnCreate: true
    };
  }

   private _createAnnotation(): WizardAnnotationModel {
    return {
      date: new Date().getTime(),
      author: this._username || 'Rocket default',
      color: annotationColors[0],
      messages: [],
    };
  }
}
