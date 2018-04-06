/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

const path = require('path');
const fs = require('fs');

const folders = ['inputs', 'transformations', 'outputs'];

const templatesPath = __dirname + '/../src/app/data-templates/';

const safeVariableName = (fileName) => {
  const indexOfDot = fileName.indexOf('.');
  if (indexOfDot === -1) {
    return fileName;
  } else {
    return fileName.slice(0, indexOfDot);
  }
};

const buildExportBlock = (files) => {
  let importBlock;
  importBlock = files.filter((file) => file.indexOf('.json') > -1)
    .map((fileName) => {
    return 'export * from \'./' + fileName + '\';';
  });
  importBlock = importBlock.join('\n');
  return importBlock;
};

const generateIndex = (folder) => {
    let code = '';
    const files = fs.readdirSync(templatesPath + folder)
    code += buildExportBlock(files);
    console.log(code)
}

folders.map((folder) => generateIndex(folder));
// fs.writeFileSync('index.ts', content, 'utf8');