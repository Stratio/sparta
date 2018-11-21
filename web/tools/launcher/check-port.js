/**
 * @name portScan
 * @type {Object}
 *
 * @property {Function} checkPortStatus
 */
import portScan from 'portscanner';
require('dotenv').config();

const env = process.env.NODE_ENV || 'development';
const config = require(`${__dirname}/db-config.json`)[env];
const host = (process.env.CHECK_PORT_DOCKER === 'docker') ? config.host : 'localhost' ;

portScan.checkPortStatus(process.env.CHECK_PORT_PORT, host, (error, status) => {
   if (status === 'open') {
      console.log(host, process.env.CHECK_PORT_PORT, status); /* eslint-disable-line no-console */
   }
});
