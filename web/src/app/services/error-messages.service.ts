/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Injectable()
export class ErrorMessagesService {
   public errors: any;

   constructor(private translate: TranslateService) {
      const errorMessages = ['ERRORS.INPUTS.GENERIC', 'ERRORS.INPUTS.REQUIRED', 'ERRORS.INPUTS.MINLENGTH', 'ERRORS.INPUTS.MAXLENGTH',
         'ERRORS.INPUTS.MIN', 'ERRORS.INPUTS.MAX', 'ERRORS.INPUTS.PATTERN'];

      this.translate.get(errorMessages).subscribe(
         (value: { [key: string]: string }) => {
            this.errors = {
               inputErrors: {
                  generic: value['ERRORS.INPUTS.GENERIC'],
                  required: value['ERRORS.INPUTS.REQUIRED'],
                  minLength: value['ERRORS.INPUTS.MINLENGTH'],
                  maxLength: value['ERRORS.INPUTS.MAXLENGTH'],
                  min: value['ERRORS.INPUTS.MIN'],
                  max: value['ERRORS.INPUTS.MAX'],
                  pattern: value['ERRORS.INPUTS.PATTERN']
               },
               selectRequiredError: value['ERRORS.INPUTS.REQUIRED']
            };
         }
      );
   }
}
