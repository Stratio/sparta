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
    Component, OnInit, OnDestroy, ElementRef,
    ChangeDetectorRef, HostListener, ViewChild, NgZone
} from '@angular/core';
import { Store } from '@ngrx/store';
import * as fromRoot from 'reducers';
import { Subscription, Observable } from 'rxjs/Rx';
import * as d3 from 'd3';
import * as wizardActions from 'actions/wizard';
import { ZoomBehavior, DragBehavior } from 'd3';
import { WizardEditorService } from './wizard-editor.sevice';
import { InitializeSchemaService } from 'services';
import { ValidateSchemaService } from 'app/services/validate-schema.service';
import { StModalButton, StModalMainTextSize, StModalType, StModalResponse, StModalService, StModalWidth } from '@stratio/egeo';


@Component({
    selector: 'wizard-editor',
    styleUrls: ['wizard-editor.styles.scss'],
    templateUrl: 'wizard-editor.template.html'
})

export class WizardEditorComponent implements OnInit, OnDestroy {

    ESC_KEYCODE = 27;
    SUPR_KEYCODE = 46;

    public entities: any = [];
    public entitiesData: any = [];

    public svgPosition = {
        x: 0,
        y: 0,
        k: 1
    };
    public showConnector = false;

    public SVGParent: any;
    public SVGContainer: any;
    public connectorElement: any;

    public selectedEntity: any = '';
    public drawingConnectionStatus: any = {
        status: false,
        name: ''
    };
    public isShowedEntityDetails$: Observable<boolean>;
    public workflowRelations: Array<any> = [];
    public selectedSegment: any = null;

    /**** Subscriptions ****/
    private creationModeSubscription: Subscription;
    private getSeletedEntitiesSubscription: Subscription;
    private getSeletedEntitiesDataSubscription: Subscription;
    private getWorflowNodesSubscription: Subscription;
    private workflowRelationsSubscription: Subscription;
    private workflowPositionSubscription: Subscription;
    private selectedSegmentSubscription: Subscription;

    private creationMode: any;
    private documentRef: any;
    private newOrigin = '';

    private zoom: ZoomBehavior<any, any>;
    private drag: DragBehavior<any, any, any>;

    @ViewChild('editorArea') editorArea: ElementRef;
    @HostListener('document:keydown', ['$event']) onKeydownHandler(event: KeyboardEvent) {
        if (event.keyCode === this.ESC_KEYCODE) {
            if (this.selectedEntity.length) {
                this.store.dispatch(new wizardActions.UnselectEntityAction());
            }
            this.store.dispatch(new wizardActions.DeselectedCreationEntityAction());

        } else if (event.keyCode === this.SUPR_KEYCODE) {
            this.deleteSelection();
        }
    }


    @HostListener('click', ['$event'])
    clickout(event: any) {
        if (this.editorArea.nativeElement.contains(event.target)) {
            if (this.selectedSegment) {
                this.store.dispatch(new wizardActions.UnselectSegmentAction());
            }
            if (this.selectedEntity) {
                this.store.dispatch(new wizardActions.UnselectEntityAction());
            }
        }
    }

    constructor(private elementRef: ElementRef, private _modalService: StModalService, private editorService: WizardEditorService,
        private validateSchemaService: ValidateSchemaService, private _cd: ChangeDetectorRef, private store: Store<fromRoot.State>,
        private initializeSchemaService: InitializeSchemaService, private _ngZone: NgZone) { }

    ngOnInit(): void {
        // ngrx
        this.creationModeSubscription = this.store.select(fromRoot.isCreationMode).subscribe((data) => {
            this.creationMode = data;
        });

        this.getSeletedEntitiesDataSubscription = this.store.select(fromRoot.getSelectedEntityData).subscribe((data) => {
            this.entitiesData = data;
        });
        this.isShowedEntityDetails$ = this.store.select(fromRoot.isShowedEntityDetails);
        this.getSeletedEntitiesSubscription = this.store.select(fromRoot.getSelectedEntities).subscribe((data) => {
            this.selectedEntity = data;
        });
        this.getWorflowNodesSubscription = this.store.select(fromRoot.getWorkflowNodes).subscribe((data: Array<any>) => {
            this.entities = data;
            this.store.dispatch(new wizardActions.ValidateWorkflowAction());
        });
        this.workflowRelationsSubscription = this.store.select(fromRoot.getWorkflowRelations).subscribe((data: Array<any>) => {
            this.workflowRelations = data.map((relation: any) => {
                return {
                    origin: this.entities.find((entity: any) => {
                        return entity.name === relation.origin;
                    }),
                    destination: this.entities.find((entity: any) => {
                        return entity.name === relation.destination;
                    }),
                };
            });
            this.store.dispatch(new wizardActions.ValidateWorkflowAction());
        });

        this.selectedSegmentSubscription = this.store.select(fromRoot.getSelectedRelation).subscribe((relation) => {
            this.selectedSegment = relation;
        });



        // d3
        this.documentRef = d3.select(document);
        this.SVGParent = d3.select(this.elementRef.nativeElement).select('#composition');
        this.SVGContainer = d3.select(this.elementRef.nativeElement).select('#svg-container');
        this.connectorElement = d3.select(this.elementRef.nativeElement).select('.connector-line');
        function deltaFn () {
            return -d3.event.deltaY * (d3.event.deltaMode ? 120 : 7) / 3100;
        }
        this.zoom = d3.zoom()
            .scaleExtent([1 / 8, 3])
            .wheelDelta(deltaFn);

        this.drag = d3.drag();

        this._ngZone.runOutsideAngular(() => {

            this.SVGParent
                .call(this.drag
                    .on('start', () => {
                        const event = d3.event;
                        const position = {
                            offsetX: event.x,
                            offsetY: event.y
                        };
                        this.clickDetected.call(this, position);
                    })/*
                    .on('drag', (e: any, f: any) => {
                        const event = d3.event;
                        this.svgPosition = {
                            x: this.svgPosition.x + event.dx,
                            y: this.svgPosition.y + event.dy,
                            k: this.svgPosition.k
                        };
                        this.setContainerPosition();
                    })*/);

        });
        this.SVGParent.call(this.zoom.on('zoom', (el: SVGSVGElement) => {
            const e: any = d3.event;
            this.svgPosition = {
                x: e.transform.x,
                y: e.transform.y,
                k: e.transform.k
            };
            this.setContainerPosition();
        })).on('dblclick.zoom', null);

        this.workflowPositionSubscription = this.store.select(fromRoot.getWorkflowPosition).subscribe((position: any) => {
            this.svgPosition = position;
            this.SVGParent.call(this.zoom.transform, d3.zoomIdentity.translate(this.svgPosition.x, this.svgPosition.y)
                .scale(this.svgPosition.k === 0 ? 1 : this.svgPosition.k));
        });

    }

    getPosition(entity: any) {
        return 'translate(' + entity.x + ',' + entity.y + ')';
    }

    setContainerPosition(): void {
        const value = 'translate(' + this.svgPosition.x + ',' + this.svgPosition.y + ') scale(' + this.svgPosition.k + ')';
        this.SVGContainer.attr('transform', value);
    }

    closeSideBar() {
        this.store.dispatch(new wizardActions.ToggleDetailSidebarAction());
    }


    clickDetected($event: any) {
        if (this.creationMode.active) {
            const entityData = this.creationMode.data;
            let entity: any = {};
            if (entityData.type === 'template') {
                entity = Object.assign({}, entityData.data);
                if (this.creationMode.data.stepType !== 'Output') {
                    entity.writer = this.initializeSchemaService.getDefaultWriterModel();
                }
                entity.name = this.editorService.getNewEntityName(entityData.data.classPrettyName, this.entities);
            } else {
                entity = this.initializeSchemaService.setDefaultEntityModel(this.creationMode.data.value, this.creationMode.data.stepType, true);
                entity.name = this.editorService.getNewEntityName(entityData.value.classPrettyName, this.entities);
            }
            entity.stepType = this.creationMode.data.stepType;
            entity.uiConfiguration = {
                position: {
                    x: ($event.offsetX - this.svgPosition.x) / this.svgPosition.k,
                    y: ($event.offsetY - this.svgPosition.y) / this.svgPosition.k
                }
            };
            const errors = this.validateSchemaService.validateEntity(entity, this.creationMode.data.stepType, this.creationMode.data.value);

            if (errors && errors.length) {
                entity.hasErrors = true;
                entity.errors = errors;
                entity.createdNew = true;
            }

            entity.created = true;
            this.store.dispatch(new wizardActions.CreateEntityAction(entity));
        }
        this.store.dispatch(new wizardActions.DeselectedCreationEntityAction());
    }

    drawConnector(event: any) {
        const $event = event.event;
        this.newOrigin = event.name;
        const connector: any = {};
        connector.x1 = $event.clientX;
        connector.y1 = $event.clientY - 127;
        connector.x2 = 0;
        connector.y2 = 0;

        this.drawingConnectionStatus = {
            status: true,
            name: event.name
        };


        const w = this.documentRef
            .on('mousemove', drawConnector.bind(this))
            .on('mouseup', mouseup.bind(this));

        function mouseup() {
            event.event.target.classList.remove('over-output2');
            this.showConnector = false;
            this.drawingConnectionStatus = {
                status: false
            };
            w.on('mousemove', null).on('mouseup', null);

        }

        function drawConnector() {
            this.showConnector = true;
            connector.x2 = d3.event.clientX - connector.x1;
            connector.y2 = d3.event.clientY - connector.y1 - 127;
            this.connectorElement.attr('d', 'M ' + connector.x1 + ' ' + connector.y1 + ' l ' + connector.x2 + ' ' + connector.y2);
        }
    }

    finishConnector(destinationEntity: any) {
        this.store.dispatch(new wizardActions.CreateNodeRelationAction({
            origin: this.newOrigin,
            destination: destinationEntity.name,
            destinationData: destinationEntity
        }));
    }

    changeZoom(zoomIn: boolean) {
        if (zoomIn) {
            this.svgPosition.k += this.svgPosition.k * 0.2;
        } else {
            this.svgPosition.k -= this.svgPosition.k * 0.2;
        }
        this.zoom.scaleTo(this.SVGParent, this.svgPosition.k);
    }

    centerWorkflow(): void {
        const container = this.elementRef.nativeElement.querySelector('#svg-container').getBoundingClientRect();
        const svgParent = this.elementRef.nativeElement.querySelector('#composition').getBoundingClientRect();

        const containerWidth = container.width;
        const containerHeight = container.height;

        const svgWidth = svgParent.width;
        const svgHeight = svgParent.height;
        const translateX = ((svgWidth - containerWidth) / 2 - container.left) / this.svgPosition.k;
        const translateY = ((svgHeight - containerHeight) / 2 - container.top) / this.svgPosition.k;
        this.SVGParent.call(this.zoom.translateBy, translateX, translateY + 127 / this.svgPosition.k);
    }

    selectEntity(entity: any) {
        if (this.selectedSegment) {
            this.store.dispatch(new wizardActions.UnselectSegmentAction());
        }
        if (this.selectedEntity !== entity.name) {
            this.store.dispatch(new wizardActions.SelectEntityAction(entity.name));
        }
    }

    editButtonEntity() {
        this.store.dispatch(new wizardActions.SaveWorkflowPositionsAction(this.entities));
        this.store.dispatch(new wizardActions.EditEntityAction());
    }

    editEntity(entity: any) {
        this.store.dispatch(new wizardActions.SaveWorkflowPositionsAction(this.entities));
        this.store.dispatch(new wizardActions.ShowEditorConfigAction({
            stepType: entity.stepType,
            data: entity
        }));
    }

    showSettings(): void {
        this.store.dispatch(new wizardActions.ShowEditorConfigAction({
            stepType: 'settings'
        }));
    }

    saveWorkflow(): void {
        // save entities position
        this.store.dispatch(new wizardActions.SaveWorkflowPositionsAction(this.entities));
        this.store.dispatch(new wizardActions.SaveEditorPosition(this.svgPosition));
        this.store.dispatch(new wizardActions.SaveWorkflowAction());
    }


    deleteSelection() {
        if (this.selectedEntity && this.selectedEntity.length) {
            this.deleteConfirmModal('Delete node', 'This node and its relations will be deleted.', () => {
                this.store.dispatch(new wizardActions.DeleteEntityAction());
            });
        }
        if (this.selectedSegment) {
            this.store.dispatch(new wizardActions.DeleteNodeRelationAction(this.selectedSegment));
            /* this.deleteConfirmModal('Delete relation', 'This relation will be deleted from workflow.', () => {
                 this.store.dispatch(new wizardActions.DeleteNodeRelationAction(this.selectedSegment));
             });*/
        }
    }

    public deleteConfirmModal(modalTitle: string, modalMessage: string, handler: any): void {
        const buttons: StModalButton[] = [
            { icon: 'icon-trash', iconLeft: true, label: 'Delete', primary: true, response: StModalResponse.YES },
            { icon: 'icon-circle-cross', iconLeft: true, label: 'Cancel', response: StModalResponse.NO }
        ];

        this._modalService.show({
            qaTag: 'delete-template',
            modalTitle: modalTitle,
            buttons: buttons,
            message: modalMessage,
            mainText: StModalMainTextSize.BIG,
            modalType: StModalType.NEUTRAL,
            modalWidth: StModalWidth.COMPACT
        }).subscribe((response) => {
            if (response === 1) {
                this._modalService.close();
            } else if (response === 0) {
                handler();
            }
        });
    }

    trackBySegmentFn(index: number, item: any) {
        return index; // or item.id
    }

    trackByBoxFn(index: number, item: any) {
        return item.name;
    }

    ngOnDestroy(): void {
        this.creationModeSubscription && this.creationModeSubscription.unsubscribe();
        this.getSeletedEntitiesSubscription && this.getSeletedEntitiesSubscription.unsubscribe();
        this.getWorflowNodesSubscription && this.getWorflowNodesSubscription.unsubscribe();
        this.workflowRelationsSubscription && this.workflowRelationsSubscription.unsubscribe();
        this.workflowPositionSubscription && this.workflowPositionSubscription.unsubscribe();
        this.getSeletedEntitiesDataSubscription && this.getSeletedEntitiesDataSubscription.unsubscribe();
        this.selectedSegmentSubscription && this.selectedSegmentSubscription.unsubscribe();
    }
}
