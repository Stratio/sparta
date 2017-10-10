///
/// Copyright (C) 2015 Stratio (http://stratio.com)
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///         http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

import * as castingTemplate from './transformations/casting.json';
import * as checkpointTemplate from './transformations/checkpoint.json';
import * as distinctTemplate from './transformations/distinct.json';
import * as filterTemplate from './transformations/filter.json';
import * as intersectionTemplate from './transformations/intersection.json';
import * as jsonPathTemplate from './transformations/jsonpath.json';
import * as jsonTemplate from './transformations/json.json';
import * as persistTemplate from './transformations/persist.json';
import * as selectTemplate from './transformations/select.json';
import * as triggerTemplate from './transformations/trigger.json';
import * as unionTemplate from './transformations/union.json';
import * as windowTemplate from './transformations/window.json';
import * as csvTemplate from './transformations/csv.json';

export const transformations: any = [
   castingTemplate,
   checkpointTemplate,
   csvTemplate,
   distinctTemplate,
   filterTemplate,
   intersectionTemplate,
   jsonPathTemplate,
   jsonTemplate,
   persistTemplate,
   selectTemplate,
   triggerTemplate,
   unionTemplate,
   windowTemplate
];

export const transformationNames = transformations.map((transformation: any) => {
  return {
        name: transformation.name,
        value: transformation,
        stepType: 'Transformation'
  };
});
