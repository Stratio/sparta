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
'use strict';

angular
	.module('StratioUI',[
		'StratioUI.helper.constant.templateUrl',
		'StratioUI.helper.passAllAttributes',
		'StratioUI.layout.dashboard',
		'StratioUI.layout.menutab',
		'StratioUI.components.quickSearch',
		'StratioUI.components.floatingMenu',
		'StratioUI.components.menuElement',
		'StratioUI.components.nodeList',
		'StratioUI.components.primarySidebarTitle',
		'StratioUI.components.primarySidebarSubmenu',
		'StratioUI.components.heading',
		'StratioUI.components.actionButton'
	]);