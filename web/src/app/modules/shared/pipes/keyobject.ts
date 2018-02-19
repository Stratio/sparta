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

import { Injectable, Pipe } from '@angular/core';

@Pipe({
   name: 'keyobject'
})
@Injectable()
export class Keyobject {

transform(value: any, args:string[]):any {
    const keys = [];
    for (const key in value) {
        keys.push({key: key, value: value[key]});
    }
    return keys;
}}
