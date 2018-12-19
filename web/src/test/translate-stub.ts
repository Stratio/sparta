/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { Pipe, PipeTransform, ModuleWithProviders } from '@angular/core';
import {TranslateService, TranslateLoader,  TranslateModule} from '@ngx-translate/core';
import { of, Observable } from 'rxjs';
import { TestBed } from '@angular/core/testing';

@Pipe({
   name: 'translate'
})
export class TranslateStubPipe implements PipeTransform {
   transform(query: string, ...args: any[]): any {
      return query;
   }
}

class FakeLoader implements TranslateLoader {
    getTranslation(lang: string): Observable<any> {
        return of(require('../assets/i18n/en.json')); // Return translate file
    }
}

export const TranslateMockModule: ModuleWithProviders = TranslateModule.forRoot({
   loader: {provide: TranslateLoader, useClass: FakeLoader}
});

export function initTranslate(): TranslateService {
   const translate: TranslateService = TestBed.get(TranslateService);
   translate.use('en');
   return translate;
}
