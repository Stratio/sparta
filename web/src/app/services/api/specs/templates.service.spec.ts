/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

import { TestBed, inject } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Store } from '@ngrx/store';
import { cloneDeep as _cloneDeep } from 'lodash';
import { USER_STORE_MOCK } from '@test/mocks/user';

const initialStoreState: any = _cloneDeep(USER_STORE_MOCK);
import { MockStore } from '@test/store-mock';


import { TemplatesService } from './../templates.service';

describe('[TemplatesService]', () => {
   const mockStoreInstance: MockStore<any> = new MockStore(initialStoreState);

   beforeEach(() => {
      spyOn(mockStoreInstance, 'dispatch');
      spyOn(mockStoreInstance, 'select').and.callThrough();
      TestBed.configureTestingModule({
         imports: [HttpClientTestingModule],
         providers: [
            TemplatesService,
            {
               provide: Store, useValue: mockStoreInstance
            }]
      });
   });

  describe('should be able to call templates services', () => {
    let service: TemplatesService;
    let http: HttpTestingController;

    beforeEach(inject([TemplatesService], (_templatesService: TemplatesService) => {
      service = TestBed.get(TemplatesService);
      http = TestBed.get(HttpTestingController);

    }));

    afterEach(() => {
      http.verify();
    });

    it('should can retrieve all templates', () => {
      const url = 'template';
      service.getAllTemplates().subscribe(response => {
        expect(response).toEqual('OK');
      });
      const req = http.expectOne(url);
      expect(req.request.method).toBe('GET');
      req.flush('OK');
    });

    it('should can retrieve a template list', () => {
      const templateType = 'input';
      const url = 'template/';
      service.getTemplateList(templateType).subscribe(response => {
        expect(response).toEqual('OK');
      });
      const req = http.expectOne(url + templateType);
      expect(req.request.method).toBe('GET');
      req.flush('OK');
    });

    it('should can retrieve a template list', () => {
      const templateType = 'input';
      const url = 'template/';
      service.getTemplateList(templateType).subscribe(response => {
        expect(response).toEqual('OK');
      });
      const req = http.expectOne(url + templateType);
      expect(req.request.method).toBe('GET');
      req.flush('OK');
    });


    it('should can retrive a template by its id', () => {
      const templateType = 'input';
      const templateId = '0';
      const url = `template/${templateType}/id/${templateId}`;
      service.getTemplateById(templateType, templateId).subscribe(response => {
        expect(response).toEqual('OK');
      });
      const req = http.expectOne(url);
      expect(req.request.method).toBe('GET');
      req.flush('OK');
    });

    it('should can delete a template', () => {
      const templateType = 'input';
      const templateId = '0';
      const url = `template/${templateType}/id/${templateId}`;
      service.deleteTemplate(templateType, templateId).subscribe(response => {
        expect(response).toEqual('OK');
      });
      const req = http.expectOne(url);
      expect(req.request.method).toBe('DELETE');
      req.flush('OK');
    });


    it('should can validate a template name', () => {
      const templateType = 'input';
      const templateName = 'unput';
      const url = `template/${templateType}/name/${templateName}`;
      service.validateTemplateName(templateType, templateName).subscribe(response => {
        expect(response).toEqual('OK');
      });
      const req = http.expectOne(url);
      expect(req.request.method).toBe('GET');
      req.flush('OK');
    });

    it('should can create a template', () => {
      const templateData = {
        name: 'template'
      };
      const url = 'template';
      service.createTemplate(templateData).subscribe(response => {
        expect(response).toEqual('OK');
      });
      const req = http.expectOne(url);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(templateData);
      req.flush('OK');
    });

    it('should can update a template', () => {
      const templateData = {
        name: 'template'
      };
      const url = 'template';
      service.updateFragment(templateData).subscribe(response => {
        expect(response).toEqual('OK');
      });
      const req = http.expectOne(url);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(templateData);
      req.flush('OK');
    });
  });

});
