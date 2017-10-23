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

import { TranslateableElement } from '@stratio/egeo';

export interface SpError {
   generic?: string;
   required?: string;
}

export interface SpInputError extends SpError {
   minLength?: string;
   maxLength?: string;
   min?: string;
   max?: string;
   type?: string;
   pattern?: string;
}

export interface SpInputErrorSchema {
   generic?: TranslateableElement;
   required?: TranslateableElement;
   minLength?: TranslateableElement;
   maxLength?: TranslateableElement;
   min?: TranslateableElement;
   max?: TranslateableElement;
   type?: TranslateableElement;
   pattern?: TranslateableElement;
}