/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { QUALITY_RULE_ACT_PASS, QUALITY_RULE_ACT_NOT_PASS } from '@app/executions/models';

@Component({
  selector: 'quality-rule-detail',
  templateUrl: './quality-rules.component.html',
  styleUrls: ['./quality-rules.component.scss']
})

export class QualityRulesComponent {
    @Input() qualityRule;
    @Input() compactMode: Boolean = false;
    @Output() closeQualityRulesInfo = new EventEmitter();
    public QUALITY_RULE_ACT_PASS = QUALITY_RULE_ACT_PASS;
    public QUALITY_RULE_ACT_NOT_PASS = QUALITY_RULE_ACT_NOT_PASS;
}
