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
import { UtilsService } from './services/utils.service';
import { FormGeneratorComponent } from './components/form-generator/form-generator.component';
import { FloatingMenuComponent } from './components/floating-menu/floating-menu.component';
import { MenuOptionsComponent } from './components/floating-menu/menu-option/menu-options.component';
import { MessageNotificationComponent } from './components/message-notification/message-notification.component';
import { FragmentBoxComponent } from './components/fragment-box/fragment-box.component';
import { FilterPipe } from './pipes/filter.pipe';
import { ToolBarComponent } from './components/tool-bar/tool-bar.component';
import { SpartaSidebarComponent } from './components/sparta-sidebar/sparta-sidebar.component';
import { DragEventsDirective } from './directives/drag-events/drag-events.directive';
import { DragEventsService } from './directives/drag-events/drag-events.service';
import { FormGeneratorGroupComponent } from './components/form-generator/form-generator-group/form-generator-group.component';
import { OrderByPipe } from './pipes/sort.pipe';
import { FormListStringComponent } from './components/form-string-list/form-string-list.component';
import { Keyobject } from './pipes/keyobject';
import { InfoFragmentComponent } from './components/fragment-box/info-fragment/info-fragment.component';
import { NotificationAlertComponent } from './components/notification-alert/notification-alert.component';
import { SpPopoverComponent } from './components/sp-popover/sp-popover.component';
import { LoadingSpinnerComponent } from './components/loading-spinner/loading-spinner.component';

export const sharedProvider = [
    MenuService,
    DragEventsService,
    UtilsService
];

export const shareComponents = [
    SearchFilterComponent,
    FileReaderComponent,
    FormListComponent,
    FormListStringComponent,
    SpPopoverComponent,
    FormFieldComponent,
    FloatingMenuComponent,
    MenuOptionsComponent,
    FormGeneratorComponent,
    NotificationAlertComponent,
    LoadingSpinnerComponent,
    FormGeneratorGroupComponent,
    MessageNotificationComponent,
    FragmentBoxComponent,
    InfoFragmentComponent,
    FormFileComponent,
    ToolBarComponent,
    SpartaSidebarComponent,
    DragEventsDirective,
    FilterPipe,
    Keyobject,
    OrderByPipe
];
