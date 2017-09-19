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

import { FormFieldComponent } from './components/form-field/form-field.component';
import { SearchFilterComponent } from './components/search-filter/search-filter.component';
import { FormFileComponent } from './components/form-file/form-file.component';
import { MenuService } from './services/menu.service';
import { FileReaderComponent } from './components/file-reader/file-reader.component';
import { FormListComponent } from './components/form-list/form-list.component';
import { UtilsService } from '@app/shared/services/utils.service';
import { FormGeneratorComponent } from '@app/shared/components/form-generator/form-generator.component';
import { FloatingMenuComponent } from '@app/shared/components/floating-menu/floating-menu.component';
import { MenuOptionsComponent } from '@app/shared/components/floating-menu/menu-option/menu-options.component';
import { MessageNotificationComponent } from '@app/shared/components/message-notification/message-notification.component';
import { FragmentBoxComponent } from '@app/shared/components/fragment-box/fragment-box.component';
import { FilterPipe } from '@app/shared/pipes/filter.pipe';
import { ToolBarComponent } from '@app/shared/components/tool-bar/tool-bar.component';
import { SpartaSidebarComponent } from '@app/shared/components/sparta-sidebar/sparta-sidebar.component';
import { DragEventsDirective } from '@app/shared/directives/drag-events/drag-events.directive';
import { DragEventsService } from '@app/shared/directives/drag-events/drag-events.service';

export const sharedProvider = [
   MenuService,
   DragEventsService,
   UtilsService
];

export const shareComponents = [
    SearchFilterComponent,
    FileReaderComponent,
    FormListComponent,
    FormFieldComponent,
    FloatingMenuComponent,
    MenuOptionsComponent,
    FormGeneratorComponent,
    MessageNotificationComponent,
    FragmentBoxComponent,
    FormFileComponent,
    ToolBarComponent,
    SpartaSidebarComponent,
    DragEventsDirective,
    FilterPipe
];
