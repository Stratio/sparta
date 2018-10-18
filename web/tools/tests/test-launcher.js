/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

var spawn = require('cross-spawn');
function parseTestPattern(argv) {
   var found = false;
   var pattern = argv.map(function(v) {
      if (found) {
         return v;
      }
      if (v === '--') {
         found = true;
      }
   }).filter(function(a) {
      return a
   }).join(' ');
   return pattern ? ['--grep', pattern] : [];
}

process.env.SINGLE_TEST_RUN = parseTestPattern(process.argv).join(',');
spawn('npm', ['run', 'ng-test'], {stdio: ['ignore', process.stdout, process.stderr]});

