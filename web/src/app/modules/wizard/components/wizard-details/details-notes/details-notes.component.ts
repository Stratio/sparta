/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { ChangeDetectionStrategy,  Component, Input } from '@angular/core';
import { Observable } from 'rxjs';
import { WizardAnnotation } from '@app/shared/wizard/components/wizard-annotation/wizard-annotation.model';
import { getTimeRemaining } from '@utils';


@Component({
  selector: 'wizard-details-notes',
  templateUrl: './details-notes.component.html',
  styleUrls: ['./details-notes.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WizardDetailsNotesComponent   {
  @Input() get annotations(): WizardAnnotation[] {
    return this._annotations;
  }
  set annotations(value: WizardAnnotation[]) {
    this._annotations = value;
    this._localizeAnnotations(value);
  }

  private _annotations: WizardAnnotation[];

  public localizedAnnotations: WizardAnnotation[] = [];

  private _localizeAnnotations(annotations) {
    this.localizedAnnotations = annotations.map((annotation): WizardAnnotation => ({
      ...annotation,
      messages: annotation.messages.map(message => ({
        ...message,
        localizedDate: getTimeRemaining(message.date)
      }))
    }));
  }
}
