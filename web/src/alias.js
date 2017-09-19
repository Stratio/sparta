/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// 'app/modules/*' routes are configured to be recognized by tsconfig.json
// for any other route it is needed to be added to tsconfig.json too
const appModules = {
   '@app/core': 'app/modules/core',
   '@app/errors': 'app/modules/errors',
   '@app/workflows': 'app/modules/workflows',
   '@app/wizard': 'app/modules/wizard',
   '@app/inputs': 'app/modules/presets/inputs',
   '@app/outputs': 'app/modules/presets/outputs',
   '@app/presets': 'app/modules/presets',
   '@app/settings': 'app/modules/settings',
   '@app/layout': 'app/modules/layout',
   '@app/resources': 'app/modules/resources',
   '@app/shared': 'app/modules/shared',
   '@app/data-templates': 'app/data-templates',
   '@app/utils': 'app/utils',
   'reducers': 'app/reducers',
   'actions': 'app/actions',
   'services': 'app/services'
};

exports.appModules = function () {
   return appModules;
};
