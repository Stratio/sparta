/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

const fs = require('fs');
const path = require('path');
const xml2js = require('xml2js');

const infoFileName = 'app-info.json';
const pomPath = path.join(__dirname, '../../', 'pom.xml');
const infoFilePath = path.join(__dirname, '../../src/app/modules/shared/constants/', infoFileName);

const parser = new xml2js.Parser();

fs.readFile(pomPath, { encoding: 'utf-8' }, function (err, data) {
  if (err) {
    throw err;
  }
  parser.parseString(data, function (err, res) {
    if (err) {
      throw err;
    } else {
      writeInfoFile(res.project.parent[0].version[0]);
    }
  });
});

function writeInfoFile(version) {
  const info = {
    version
  }
  fs.writeFile(infoFilePath, JSON.stringify(info, null, 2), function (err) {
    if (err) {
      throw err;
    }
  });
}


