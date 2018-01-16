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

import { Component, Input } from '@angular/core';
import { Store } from '@ngrx/store';
import * as fromRoot from 'reducers';

@Component({
    selector: 'user-profile',
    styleUrls: ['user-profile.component.scss'],
    templateUrl: 'user-profile.component.html'
})

export class UserProfileComponent {

    @Input() userName = '';
    public showUserProfileMenu = false;
    public userMenuOffset = { x: 0, y: 7 };

    constructor(private store: Store<fromRoot.State>) { }

    logout() {
        window.location.href = 'logout';
    }

    toggleUserProfile(): void {
      this.showUserProfileMenu = !this.showUserProfileMenu;
   }


}
