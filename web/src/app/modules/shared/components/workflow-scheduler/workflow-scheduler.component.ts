/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { ChangeDetectionStrategy, Component, EventEmitter, OnDestroy, OnInit, Output, ViewChild, ChangeDetectorRef, Input } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, NgForm, Validators } from '@angular/forms';
import { Store, select } from '@ngrx/store';
import { Subscription } from 'rxjs';
import { NgbCalendar, NgbDate, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';

import { ErrorMessagesService } from 'app/services';

@Component({
  selector: 'workflow-scheduler',
  templateUrl: './workflow-scheduler.component.html',
  styleUrls: ['./workflow-scheduler.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WorkflowSchedulerComponent implements OnInit, OnDestroy {

  @Input() scheduledFormControl: FormControl;
  @Input() entityId: string;
  @Output() onCloseScheduleModal = new EventEmitter<any>();

  public groupForm: FormGroup;
  public startDate: { year: number; month: number; day?: number; };

  public forceValidations = false;
  public date: NgbDateStruct;

  private openModal$: Subscription;
  public isDisabled = (date: NgbDate, current: { month: number }) => date.month !== current.month;
  public isWeekend = (date: NgbDate) => this.calendar.getWeekday(date) >= 6;
  public dateFormControl;
  public startTimeFormControl: FormControl;
  public activeFormControl = new FormControl(false);

  public repeatFormControl = new FormControl(false);

  public repeatFormGroup: FormGroup;

  public intervalOptions: Array<any> = [
    {
      label: 'Select a value',
      value: ''
    },
    {
      label: 'milliseconds',
      value: 'ms'
    }, {
      label: 'seconds',
      value: 's'
    }, {
      label: 'minutes',
      value: 'm'
    }, {
      label: 'hours',
      value: 'h'
    },
    {
      label: 'days',
      value: 'd'
    },
    {
      label: 'weeks',
      value: 'w'
    },
    {
      label: 'months',
      value: 'M'
    }, {
      label: 'years',
      value: 'y'
    }];

  constructor(
    public errorsService: ErrorMessagesService,
    private _cd: ChangeDetectorRef,
    private _fb: FormBuilder, private calendar: NgbCalendar) {
    const date = new Date();
    this.startTimeFormControl = new FormControl((date.getHours() + ':' + ('0' + date.getMinutes()).slice(-2)) + ':' + ('0' + date.getSeconds()).slice(-2));
    const currentDate: NgbDateStruct = {
      year: date.getFullYear(),
      month: date.getMonth() + 1,
      day: date.getDay()
    };
    this.dateFormControl = new FormControl(currentDate);

    this.startDate = currentDate;
  }

  createSchedule() {
    if (this.groupForm.valid) {
      this.onCloseScheduleModal.emit(this._getFormModel());
    } else {
      this.forceValidations = true;
      this.dateFormControl.markAsDirty();
    }
  }

  ngOnInit() {
    this.groupForm = this._fb.group({
      date: this.dateFormControl,
      startTime: this.startTimeFormControl,
      activeFormControl: this.activeFormControl
    });
    this.repeatFormControl.valueChanges.subscribe((repeat: boolean) => {
      if (repeat) {
        this._createRepeatForm();
      } else {
        this.groupForm.removeControl('repeat');
        this.repeatFormGroup = null;
      }
    });

    if (this.scheduledFormControl) {
      this.groupForm.valueChanges.subscribe(value => {

        if (this.groupForm.invalid) {
          this.scheduledFormControl.setValue(null);
        } else {
          this.scheduledFormControl.setValue(this._getFormModel());
        }
      });
    }
  }

  private _createRepeatForm() {
    this.repeatFormGroup = this._fb.group({
      value: new FormControl(),
      unit: new FormControl(''),
      unique: new FormControl(false)
    });
    this.groupForm.addControl('repeat', this.repeatFormGroup);
  }

  private _getFormModel() {
    const dateValue = this.dateFormControl.value;
    const date = new Date(`${dateValue.year}-${dateValue.month}-${dateValue.day}`);
    const time = this.startTimeFormControl.value.split(':');
 
    const initDate = new Date(`${dateValue.year}-${dateValue.month}-${dateValue.day}`);
    initDate.setHours(time[0]);
    initDate.setMinutes(time[1]);
    const initDateMillis = initDate.getTime();
    let taskType;
    if (this.repeatFormControl.value) {
      taskType = this.repeatFormGroup.get('unique').value ? 'UNIQUE_PERIODICAL' : 'PERIODICAL';
    } else {
      taskType = 'ONE_TIME';
    }
    let duration;
    if (this.repeatFormControl.value) {
      duration = this.repeatFormGroup.get('value').value + this.repeatFormGroup.get('unit').value
    }
    return {
      taskType,
      actionType: 'RUN',
      active: this.activeFormControl.value,
      initDate: initDateMillis,
      duration
    };
  }

  ngOnDestroy(): void {
    this.openModal$ && this.openModal$.unsubscribe();
  }

}

