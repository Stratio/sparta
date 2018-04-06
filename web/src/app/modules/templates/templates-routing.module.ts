/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TemplatesComponent } from './templates.component';
import { InputsComponent } from './components/template-list/inputs.component';
import { OutputsComponent } from './components/template-list/outputs.component';
import { TransformationsComponent } from './components/template-list/transformations.component';
import { CreateInputComponent } from './components/template-creation/create-input.component';
import { CreateOutputComponent } from './components/template-creation/create-output.component';
import { CreateTransformationsComponent } from './components/template-creation/create-transformation.component';

const templatesRoutes: Routes = [
   {
      path: '',
      redirectTo: 'inputs',
   },
   {
      path: 'inputs',
      component: InputsComponent,
      children: [
         {
            path: 'create',
            component: CreateInputComponent
        },
        {
            path: 'edit',
            component: CreateInputComponent
        },
        {
            path: 'edit/:id',
            component: CreateInputComponent
        },
      ]
   },
   {
      path: 'outputs',
      component: OutputsComponent,
      children: [
         {
            path: 'create',
            component: CreateOutputComponent
        },
        {
            path: 'edit',
            component: CreateOutputComponent
        },
        {
            path: 'edit/:id',
            component: CreateOutputComponent
        }
      ]
   },
   {
      path: 'transformations',
      component: TransformationsComponent,
      children: [
         {
            path: 'create',
            component: CreateTransformationsComponent
        },
        {
            path: 'edit',
            component: CreateTransformationsComponent
        },
        {
            path: 'edit/:id',
            component: CreateTransformationsComponent
        }
      ]
   },
];

@NgModule({
   exports: [
      RouterModule
   ],
   imports: [
      RouterModule.forChild(templatesRoutes)
   ]
})

export class TemplatesRoutingModule { }
