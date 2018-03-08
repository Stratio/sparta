/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import * as utils from './';

describe('Util function type', function () {

    const action = 'TEST_ACTION';

    it('should return action name when this is unique', () => {
        expect(utils.type(action)).toBe(action);
    });

    it('should throw an exception when an action its not unique', () => {
        expect(() => utils.type(action)).toThrow();
    });
});