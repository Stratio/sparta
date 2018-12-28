/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { ChangeDetectionStrategy, Component, Input, OnInit, ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'executions-chart',
  styleUrls: ['executions-chart.component.scss'],
  templateUrl: 'executions-chart.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class ExecutionsChartComponent implements OnInit {


  @Input() get chartData() {
    return this._chartData;
  }
  set chartData(data) {
    this._chartData = data;
    this.labels.length = 0;
    this.labels.push(...data.times);

    this.values = [
      data.streamingTotal,
      data.batchTotal
    ];
    this._cd.detectChanges();
  }

  public lineChartOptions: any = {
    responsive: true,
    maintainAspectRatio: false,

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
          callback: function (value) { if (value % 1 === 0) { return value; } }
        },
      }]
    }
  };

  private _chartData: any;

  public labels = [];
  public values = [[]];
  public lineChartColors: Array<any> = [
    { // grey
      backgroundColor: 'rgba(18,139,222,0.1)',
      borderColor: '#128bde',
      pointBackgroundColor: 'rgba(0, 0, 0, 0)',
      pointBorderColor: 'rgba(0, 0, 0, 0)',
      pointHoverBackgroundColor: '#fff',
      pointHoverBorderColor: 'rgba(148,159,177,0.8)'
    }
  ];
  public lineChartLegend = true;
  public lineChartType = 'line';

  constructor(private _cd: ChangeDetectorRef) { }

  ngOnInit(): void { }

}
