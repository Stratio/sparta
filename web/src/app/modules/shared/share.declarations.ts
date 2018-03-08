/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { FormFieldComponent } from './components/form-field/form-field.component';
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
    FileReaderComponent,
    FormListComponent,
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
