/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
import { InitializeSchemaService } from '../initialize-schema.service';
import { streamingInputsObject } from 'data-templates/inputs';
import { streamingOutputsObject } from 'data-templates/outputs';
import { settingsTemplate } from 'data-templates/index';
import { TestBed, inject } from '@angular/core/testing';
import { TranslateMockModule } from '@test/translate-stub';
import { Engine } from '@models/enums';

const kafkaTemplate = streamingInputsObject['Kafka'];
const printTemplate = streamingOutputsObject['Print'];

const schema = [
   {
      propertyId: 'prop1',
      default: 'val'
   },
   {
      propertyId: 'prop2',
      default: 'val'
   },
   {
      name: 'subcategory',
      properties: [
         {
            propertyId: 'prop3',
            default: 'val'
         }
      ]
   }
];

describe('initialize-schema.service', function () {

   beforeEach(() => TestBed.configureTestingModule({
      imports: [TranslateMockModule],
      providers: [InitializeSchemaService]
   }));

   let initializeSchemaService: InitializeSchemaService;

   beforeEach(inject([InitializeSchemaService], (_initializeSchemaService: InitializeSchemaService) => {
      initializeSchemaService = TestBed.get(InitializeSchemaService);
   }));

   it('should be able to initialize an entity', () => {
      const template: any = <any>kafkaTemplate;
      const model = initializeSchemaService.setDefaultEntityModel(Engine.Streaming, kafkaTemplate, 'Input', true);
      expect(model.classPrettyName).toBe(template.classPrettyName);
   });

   it('should be able to initialize entity writer if it is not an output', () => {
      const model = initializeSchemaService.setDefaultEntityModel(Engine.Streaming, kafkaTemplate, 'Input', true);
      expect(model.writer).toBeDefined();
   });

   xit('should not initialize the writer if it is an output', () => {
      const model = initializeSchemaService.setDefaultEntityModel(Engine.Streaming, printTemplate, 'Output', true);
      expect(model.writer).toBeUndefined();
   });

   it('should be able to initize workflow settings model', () => {
      const template: any = <any>settingsTemplate;
      spyOn(InitializeSchemaService, 'getCategoryModel');
      const model = InitializeSchemaService.setDefaultWorkflowSettings(template);
      expect(Object.keys(model.advancedSettings).length).toBe(template.advancedSettings.length);
      expect(InitializeSchemaService.getCategoryModel).toHaveBeenCalled();
   });

   it('should initialize caterory settings model', () => {
      const model = InitializeSchemaService.getCategoryModel(schema);
      expect(model.prop1).toBeDefined();
      expect(model.prop2).toBeDefined();
      expect(model.subcategory).toBeDefined();
      expect(model.subcategory.prop3).toBe('val');
   });
});
