/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */


import { TestBed, inject } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { WorkflowService } from './../workflow.service';

describe('[WorkflowService]', () => {

    beforeEach(() => TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        providers: [WorkflowService]
    }));

    describe('should be able to retrieve a list of ip pools', () => {

        let service: WorkflowService;
        let http: HttpTestingController;

        beforeEach(inject([WorkflowService], (_workflowService: WorkflowService) => {
            service = TestBed.get(WorkflowService);
            http = TestBed.get(HttpTestingController);

        }));

        afterEach(() => {
            http.verify();
        });

        it('should create group', () => {
            const url = 'groups';
            const groupName = 'group-name';
            service.createGroup(groupName).subscribe(response => {
                expect(response).toEqual('OK');
            });
            const req = http.expectOne(url);
            expect(req.request.method).toBe('POST');
            req.flush('OK');
        });
    });
});
