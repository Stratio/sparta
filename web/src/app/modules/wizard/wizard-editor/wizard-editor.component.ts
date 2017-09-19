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

import {
    Component, OnInit, OnDestroy, AfterViewChecked, ElementRef, ChangeDetectionStrategy,
    ChangeDetectorRef, HostListener
} from '@angular/core';
import { Store } from '@ngrx/store';
import * as fromRoot from 'reducers';
import { Subscription, Observable } from 'rxjs/Rx';
import { Router, ActivatedRoute } from '@angular/router';
import * as base from 'assets/images/workflow-base.svg';
import * as d3 from 'd3';
import * as wizardActions from 'actions/wizard';
import { D3ZoomEvent } from 'd3';
import { WizardEditorService } from './wizard-editor.sevice';


@Component({
    selector: 'wizard-editor',
    styleUrls: ['wizard-editor.styles.scss'],
    templateUrl: 'wizard-editor.template.html'
})

export class WizardEditorComponent implements OnInit, OnDestroy {

    public entities: any = [];

    public svgPosition = {
        x: 10,
        y: 10,
        z: 1.1
    };

    public showConnector = false;
    public connector = {
        x1: 0,
        y1: 0,
        x2: 30,
        y2: 30
    };

    public SVGParent: any;

    public SVGContainer: any;
    public selectedEntity: any = '';
    public isShowedEntityDetails$: Observable<boolean>;
    public workflowRelations: Array<any> = [];
    private creationModeSubscription: Subscription;
    private creationMode: any;

    private newOrigin: string = '';

    private zoom: any;
    private drag: any;


    constructor(private elementRef: ElementRef, private editorService: WizardEditorService,
        private _cd: ChangeDetectorRef, private store: Store<fromRoot.State>) { }

    ngOnInit(): void {
        // ngrx
        this.creationModeSubscription = this.store.select(fromRoot.isCreationMode).subscribe((data) => {
            this.creationMode = data;
        });
        this.isShowedEntityDetails$ = this.store.select(fromRoot.isShowedEntityDetails);
        this.store.select(fromRoot.getSelectedEntities).subscribe((data) => {
            this.selectedEntity = data;
        });
        this.store.select(fromRoot.getWorkflowNodes).subscribe((data: Array<any>) => {
            this.entities = data;
        });
        this.store.select(fromRoot.getWorkflowRelations).subscribe((data: Array<any>) => {
            this.workflowRelations = data.map((relation: any) => {
                return {
                    origin: this.entities.filter((entity: any) => {
                        return entity.name === relation.origin;
                    })[0],
                    destination: this.entities.filter((entity: any) => {
                        return entity.name === relation.destination;
                    })[0],
                };
            });
        });



        // d3
        this.SVGParent = d3.select(this.elementRef.nativeElement).select('#composition');
        this.SVGContainer = d3.select(this.elementRef.nativeElement).select('#svg-container');

        this.zoom = d3.zoom()
            .scaleExtent([1 / 8, 3])
            .on('zoom', undefined);

        this.drag = d3.drag()
            .on('drag', undefined);


        this.SVGParent.call(this.drag.on('drag', (e: any, f: any) => {
            const event = d3.event;
            this.svgPosition = {
                x: this.svgPosition.x + event.dx,
                y: this.svgPosition.y + event.dy,
                z: this.svgPosition.z
            };
            this.setContainerPosition();
        }));

        this.SVGParent.call(this.zoom.on('zoom', (el: SVGSVGElement) => {
            const e: any = d3.event;
            this.svgPosition = {
                x: e.transform.x,
                y: e.transform.y,
                z: e.transform.k
            };
            this.setContainerPosition();
        })).on('dblclick.zoom', null);

        this.SVGParent.call(this.zoom.transform, d3.zoomIdentity.translate(this.svgPosition.x, this.svgPosition.y)
        .scale(this.svgPosition.z));
        
    }

    getPosition(entity: any) {
        return 'translate(' + entity.x + ',' + entity.y + ')';
    }

    setContainerPosition(): void {
        const value = 'translate(' + this.svgPosition.x + ',' + this.svgPosition.y + ') scale(' + this.svgPosition.z + ')';
        this.SVGContainer.attr('transform', value);
    }

    closeSideBar() {
        this.store.dispatch(new wizardActions.ToggleDetailSidebarAction());
    }


    clickDetected($event: any) {
        if (this.creationMode.active) {
            const entity = this.creationMode.data;
            this.entities.push({
                type: entity.type,
                name: this.editorService.getNewEntityName(entity.type, this.entities),
                position: {
                    x: ($event.offsetX - this.svgPosition.x) / this.svgPosition.z,
                    y: ($event.offsetY - this.svgPosition.y) / this.svgPosition.z
                }
            });
        }
        this.store.dispatch(new wizardActions.DeselectedCreationEntityAction());
    }

    drawConnector(event: any) {
        const $event = event.event;
        this.newOrigin = event.name;

        this.connector.x1 = $event.clientX;
        this.connector.y1 = $event.clientY - 120;
        this.connector.x2 = 0;
        this.connector.y2 = 0;
        this.showConnector = true;

        const w = this.SVGParent
            .on('mousemove', drawConnector.bind(this))
            .on('mouseup', mouseup.bind(this));

        function mouseup() {
            this.showConnector = false;
            w.on('mousemove', null).on('mouseup', null);
        }

        function drawConnector() {
            this.connector.x2 = d3.event.clientX - this.connector.x1;
            this.connector.y2 = d3.event.clientY - this.connector.y1 - 120;
        }
    }

    finishConnector(name: string) {
        this.store.dispatch(new wizardActions.CreateNodeRelation({
            origin: this.newOrigin,
            destination: name
        }));
    }

    changedPosition() {

    }

    changeZoom(zoomIn: boolean) {
        const oldZ = this.svgPosition.z;
        if (zoomIn) {
            this.svgPosition.z += this.svgPosition.z * 0.2;
        } else {
            this.svgPosition.z -= this.svgPosition.z * 0.2;
        }
        this.zoom.scaleTo(this.SVGParent, this.svgPosition.z);
    }

    centerWorkflow(): void {
        const container = this.elementRef.nativeElement.querySelector('#svg-container').getBoundingClientRect();
        const containerWidth = container.width;
        const containerHeight = container.height;
        const svgParent = this.elementRef.nativeElement.querySelector('#composition').getBoundingClientRect();
        const svgWidth = svgParent.width;
        const svgHeight = svgParent.height;
        const translateX = ((svgWidth - containerWidth) / 2 - container.left) / this.svgPosition.z;
        const translateY = ((svgHeight - containerHeight) / 2 - container.top) / this.svgPosition.z;
        this.SVGParent.call(this.zoom.translateBy, translateX, translateY + 120 / this.svgPosition.z);
        //this.SVGParent.call(this.zoom.translateBy, this.svgPosition.x, this.svgPosition.y).call(this.zoom.scaleTo, this.svgPosition.z);
    }

    selectEntity(entity: any) {
        if (this.selectedEntity === entity.name) {
            this.store.dispatch(new wizardActions.UnselectEntityAction());
        } else {
            this.store.dispatch(new wizardActions.SelectEntityAction(entity.name));
        }
    }


    editEntity(entity: any) {
        this.store.dispatch(new wizardActions.SelectEntityAction(entity));
    }

    ngOnDestroy(): void {
        this.creationModeSubscription.unsubscribe();
    }

}
