/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { ChangeDetectionStrategy, Component, Input, ChangeDetectorRef, Output, EventEmitter, ViewChild, OnInit } from '@angular/core';
import { BaseChartDirective } from 'ng2-charts';

@Component({
  selector: 'executions-chart',
  styleUrls: ['executions-chart.component.scss'],
  templateUrl: 'executions-chart.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class ExecutionsChartComponent implements OnInit {


  @Output() periodChange: EventEmitter<string> = new EventEmitter<string>();

  @Input() get chartData() {
    return this._chartData;
  }
  @ViewChild(BaseChartDirective)  chart: any;

  set chartData(data) {
    this._chartData = data;
    this.labels.length = 0;
    this.labels.push(...data.times);
    this.values = [
      data.streamingTotal,
      data.batchTotal
    ];
    const allValues = [...data.streamingTotal.data, ...data.batchTotal.data];
    const max = allValues.length ? Math.max(...allValues) : 0;
    this.lineChartOptions.scales.yAxes[0].ticks.max = max * 1.05212;
    if (this._initialized) {
      this.chart.refresh();
    }
    this._cd.detectChanges();
  }

  public lineChartOptions: any = {
    responsive: true,
    maintainAspectRatio: false,
    tooltips: {
      displayColors: false
    },
    scales: {
      xAxes: [{
        ticks: {
          autoSkip: false,
          maxRotation: 0,
          minRotation: 0
        },
        gridLines: {
          offsetGridLines: true
        }
      }],
      yAxes: [{
        gridLines: {
          color: 'rgba(0, 0, 0, 0)',
        },
        ticks: {
          suggestedMin: 0,    // minimum will be 0, unless there is a lower value.
          // OR //
          beginAtZero: true,   // minimum value will be 0.
          callback: function (value) { if (value % 1 === 0) { return value; } }, // only show integer numbers
          min: 0,
          max: 0
        },
      }]
    }
  };

  private _chartData: any;
  private _initialized = false;
  public labels = [];
  public values = [[]];
  public lineChartColors: Array<any> = [
    { // blue
      backgroundColor: 'rgba(18,139,222,0.1)',
      borderColor: '#128bde',
      pointBackgroundColor: 'rgba(0, 0, 0, 0)',
      pointBorderColor: 'rgba(0, 0, 0, 0)',
      pointHoverBackgroundColor: '#fff',
      pointHoverBorderColor: 'rgba(148,159,177,0.8)',
      scaleOverride : true,
    },
    { // grey
      backgroundColor: 'rgba(10,10,10, 0.1)',
      borderColor: '#999',
      pointBackgroundColor: 'rgba(0, 0, 0, 0)',
      pointBorderColor: 'rgba(0, 0, 0, 0)',
      pointHoverBackgroundColor: '#fff',
      pointHoverBorderColor: 'rgba(148,159,177,0.8)',
      max: 3
    }
  ];
  public lineChartLegend = true;
  public lineChartType = 'line';
  public periodTitle = 'EXECUTIONS.PERIODS.DAY';

  constructor(private _cd: ChangeDetectorRef) { }

  ngOnInit(): void {
    this._initialized = true;
  }

  public selectPeriod(ev, period) {
    const currentButton: Element = ev.currentTarget;
    const buttons: NodeList = ev.currentTarget.parentElement.parentElement.querySelectorAll('button');
    const periodTitles: Object = {
      'DAY': 'EXECUTIONS.PERIODS.DAY',
      'WEEK': 'EXECUTIONS.PERIODS.WEEK',
      'MONTH': 'EXECUTIONS.PERIODS.MONTH'
    };
    this.periodTitle = periodTitles[period];

    buttons.forEach((button: Element) => button.classList.remove('selected'));
    currentButton.classList.add('selected');
    this.periodChange.emit(period);
  }

}
