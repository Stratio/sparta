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
    ChangeDetectorRef,
    Component,
    ElementRef,
    HostListener,
    Input,
    NgZone,
    OnDestroy,
    OnInit,
    ViewChild
} from '@angular/core';
import { Store } from '@ngrx/store';
import * as fromRoot from 'reducers';
import { Subscription, Observable } from 'rxjs/Rx';
import * as d3 from 'd3';
import * as wizardActions from 'actions/wizard';
import { ZoomBehavior, DragBehavior } from 'd3';
import { WizardEditorService } from './wizard-editor.sevice';
import { InitializeSchemaService } from 'services';
import { StModalButton, StModalResponse, StModalService } from '@stratio/egeo';
import { isMobile } from 'constants/global';

@Component({
    selector: 'wizard-editor',
    styleUrls: ['wizard-editor.styles.scss'],
    templateUrl: 'wizard-editor.template.html'
})

export class WizardEditorComponent implements OnInit, OnDestroy {

    @Input() workflowType: string;

    ESC_KEYCODE = 27;
    SUPR_KEYCODE = 46;

    public entities: any = [];
    public entitiesData: any = [];

    private svgPosition: any;
    public isMobile = false;
    public showConnector = false;

    // selectors
    public SVGParent: any;
    public SVGContainer: any;
    private documentRef: any;
    public connectorElement: any;

    public selectedEntity: any = '';
    public drawingConnectionStatus: any = {
        status: false,
        name: ''
    };
    public isShowedEntityDetails$: Observable<boolean>;
    public workflowRelations: Array<any> = [];
    public selectedSegment: any = null;
    public isPristine = true;

    /**** Subscriptions ****/
    private creationModeSubscription: Subscription;
    private getSeletedEntitiesSubscription: Subscription;
    private getSeletedEntitiesDataSubscription: Subscription;
    private getWorflowNodesSubscription: Subscription;
    private workflowRelationsSubscription: Subscription;
    private workflowPositionSubscription: Subscription;
    private selectedSegmentSubscription: Subscription;

    private creationMode: any;
    private newOrigin = '';

    public workflowName = '';
    public workflowVersion = '';

    private zoom: ZoomBehavior<any, any>;
    private drag: DragBehavior<any, any, any>;
    private _nameSubscription: Subscription;

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
        private _cd: ChangeDetectorRef, private store: Store<fromRoot.State>,
        private initializeSchemaService: InitializeSchemaService, private _ngZone: NgZone) {
        this.isMobile = isMobile;
    }

    ngOnInit(): void {

        this._nameSubscription = this.store.select(fromRoot.getWorkflowHeaderData).subscribe((data: any) => {
            this.workflowName = data.name;
            this.workflowVersion = data.version;
        });
        this.creationModeSubscription = this.store.select(fromRoot.isCreationMode).subscribe((data) => { this.creationMode = data; });
        this.getSeletedEntitiesDataSubscription = this.store.select(fromRoot.getSelectedEntityData)
            .subscribe((data) => { this.entitiesData = data; });
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
            this._cd.markForCheck();
            this.store.dispatch(new wizardActions.ValidateWorkflowAction());
        });

        this.selectedSegmentSubscription = this.store.select(fromRoot.getSelectedRelation).subscribe((relation) => {
            this.selectedSegment = relation;
            this._cd.markForCheck();
        });

        this.documentRef = d3.select(document);
        this.SVGParent = d3.select(this.elementRef.nativeElement).select('#composition');
        this.SVGContainer = d3.select(this.elementRef.nativeElement).select('#svg-container');
        this.connectorElement = d3.select(this.elementRef.nativeElement).select('.connector-line');
        function deltaFn() {
            return -d3.event.deltaY * (d3.event.deltaMode ? 0.0387 : 0.002258);
        }
        this.zoom = d3.zoom()
            .scaleExtent([1 / 8, 3])
            .wheelDelta(deltaFn);

        this.drag = d3.drag();

        this._ngZone.runOutsideAngular(() => {
            let pristine = true;
            let repaints = 0;
            const SVGContainer = this.SVGContainer;
            const repaint = function () {
                SVGContainer.attr('transform', this.toString());
            };
            this.SVGParent.call(this.drag
                .on('start', () => {
                    const event = d3.event;
                    const position = {
                        offsetX: event.x,
                        offsetY: event.y
                    };
                    this.clickDetected.call(this, position);
                }));

            let lastUpdateCall: any;
            this.SVGParent.call(this.zoom.on('zoom', (el: SVGSVGElement) => {
                const e: any = d3.event;
                if (pristine) {
                    if (repaints < 2) {
                        repaints++;
                    } else {
                        if (this.entities.length) {
                            pristine = false;
                            this.store.dispatch(new wizardActions.SetWizardStateDirtyAction());
                        }
                    }
                }
                if (lastUpdateCall) {
                    cancelAnimationFrame(lastUpdateCall);
                }
                this.svgPosition = e.transform;
                lastUpdateCall = requestAnimationFrame(repaint.bind(e.transform));
            })).on('dblclick.zoom', null);
        });

        this.workflowPositionSubscription = this.store.select(fromRoot.getWorkflowPosition).subscribe((position: any) => {
            this.svgPosition = position;
            this.SVGParent.call(this.zoom.transform, d3.zoomIdentity.translate(this.svgPosition.x, this.svgPosition.y)
                .scale(this.svgPosition.k === 0 ? 1 : this.svgPosition.k));
        });
    }

    closeSideBar() {
        this.store.dispatch(new wizardActions.ToggleDetailSidebarAction());
    }

    duplicateNode(): void {
        if (this.selectedEntity) {
            const data = JSON.parse(JSON.stringify((this.entities.find((node: any) => {
                return node.name === this.selectedEntity;
            }))));
            data.name = this.editorService.getNewEntityName(data.name, this.entities);
            const newEntity: any = {
                type: 'copy',
                data: data
            };
            this.store.dispatch(new wizardActions.DuplicateNodeAction(newEntity));
        }
    }

    clickDetected($event: any) {
        if (this.creationMode.active) {
            let entity: any = {};
            const entityData = this.creationMode.data;
            if (entityData.type === 'copy') { // if its a copy, only sets the position
                entity = entityData.data;
            } else {
                entity = this.editorService.initializeEntity(this.workflowType, entityData, this.entities);
            }
            entity.uiConfiguration = {
                position: {
                    x: ($event.offsetX - this.svgPosition.x) / this.svgPosition.k,
                    y: ($event.offsetY - this.svgPosition.y) / this.svgPosition.k
                }
            };
            this.store.dispatch(new wizardActions.CreateEntityAction(entity));
        }
        this.store.dispatch(new wizardActions.DeselectedCreationEntityAction());
    }

    createEdge(event: any) {
        if (isMobile) {
            this.newOrigin = event.name;
            this.drawingConnectionStatus = {
                status: true,
                name: event.name
            };
            const w = this.documentRef
                .on('click', () => {
                    w.on('click', null).on('click', null);
                    this.newOrigin = '';
                    event.event.target.classList.remove('over-output2');
                    this.drawingConnectionStatus = {
                        status: false
                    };
                });
        } else {
            this.drawConnector(event);
        }
    }

    drawConnector(event: any) {
        const $event = event.event;
        this.newOrigin = event.name;
        const connector: any = {};
        connector.x1 = $event.clientX;
        connector.y1 = $event.clientY - 135;
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
            this.newOrigin = '';
            this.drawingConnectionStatus = {
                status: false
            };
            w.on('mousemove', null).on('mouseup', null);
        }

        function drawConnector() {
            this.showConnector = true;
            connector.x2 = d3.event.clientX - connector.x1;
            connector.y2 = d3.event.clientY - connector.y1 - 135;
            this.connectorElement.attr('d', 'M ' + connector.x1 + ' ' + connector.y1 + ' l ' + connector.x2 + ' ' + connector.y2);
        }
    }

    finishConnector(destinationEntity: any) {
        if (this.newOrigin && this.newOrigin.length) {
            this.store.dispatch(new wizardActions.CreateNodeRelationAction({
                origin: this.newOrigin,
                destination: destinationEntity.name,
                destinationData: destinationEntity
            }));
        }
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
        this.SVGParent.call(this.zoom.translateBy, translateX, translateY + 135 / this.svgPosition.k);
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
        }
    }

    public deleteConfirmModal(modalTitle: string, modalMessage: string, handler: any): void {
        const buttons: StModalButton[] = [
            { label: 'Cancel', responseValue: StModalResponse.NO, closeOnClick: true, classes: 'button-secondary-gray' },
            { label: 'Delete', responseValue: StModalResponse.YES, classes: 'button-critical', closeOnClick: true }
        ];

        this._modalService.show({
            modalTitle: modalTitle,
            buttons: buttons,
            maxWidth: 500,
            messageTitle: 'Are you sure?',
            message: modalMessage,
        }).subscribe((response: any) => {
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

    ngOnDestroy(): void {
        this.creationModeSubscription && this.creationModeSubscription.unsubscribe();
        this.getSeletedEntitiesSubscription && this.getSeletedEntitiesSubscription.unsubscribe();
        this.getWorflowNodesSubscription && this.getWorflowNodesSubscription.unsubscribe();
        this.workflowRelationsSubscription && this.workflowRelationsSubscription.unsubscribe();
        this.workflowPositionSubscription && this.workflowPositionSubscription.unsubscribe();
        this.getSeletedEntitiesDataSubscription && this.getSeletedEntitiesDataSubscription.unsubscribe();
        this.selectedSegmentSubscription && this.selectedSegmentSubscription.unsubscribe();
        this._nameSubscription && this._nameSubscription.unsubscribe();

    }
}
