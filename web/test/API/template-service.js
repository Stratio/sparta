describe('API.template-service', function() {
  beforeEach(module('webApp'));
  beforeEach(module('template/input.json'));
  beforeEach(module('template/output.json'));
  beforeEach(module('template/policy.json'));
  beforeEach(module('template/operator/default.json'));
  beforeEach(module('template/operator/count.json'));
  beforeEach(module('template/dimension/default.json'));
  beforeEach(module('template/dimension/datetime.json'));
  beforeEach(module('template/dimension/geohash.json'));
  beforeEach(module('template/trigger/transformation.json'));
  beforeEach(module('template/trigger/cube.json'));

  var srv, rootScope, httpBackend, fakeFragmentTemplateByTypeInput, fakeFragmentTemplateByTypeOutput,
      fakePolicyTemplate, fakeDefaultOperatorTemplate, fakeCountOperatorTemplate,
      fakeDefaultDimensionTemplate, fakeDateTimeTemplate, fakeGeoHashTemplate,
      fakeTransformationTriggerTemplate, fakeCubeTriggerTemplate = null;

  beforeEach(
      inject(function($httpBackend, $resource, $rootScope, _templateInput_, _templateOutput_, _templatePolicy_,
                      _templateOperatorCount_, _templateOperatorDefault_, _templateDimensionDefault_,
                      _templateDimensionDatetime_, _templateDimensionGeohash_, _ApiTemplateService_,
                      _templateTriggerTransformation_, _templateTriggerCube_) {
        fakeFragmentTemplateByTypeInput = _templateInput_;
        fakeFragmentTemplateByTypeOutput = _templateOutput_;
        fakePolicyTemplate = _templatePolicy_;
        fakeDefaultDimensionTemplate = _templateDimensionDefault_;
        fakeDateTimeTemplate = _templateDimensionDatetime_;
        fakeGeoHashTemplate = _templateDimensionGeohash_;
        fakeCountOperatorTemplate = _templateOperatorCount_;
        fakeDefaultOperatorTemplate = _templateOperatorDefault_;
        fakeTransformationTriggerTemplate = _templateTriggerTransformation_;
        fakeCubeTriggerTemplate = _templateTriggerCube_;

        rootScope = $rootScope;
        srv = _ApiTemplateService_;
        httpBackend = $httpBackend;

        httpBackend.when('GET', 'languages/en-US.json').respond({});
      })
  );

  it("Should return input templates", function() {
    var fragmentTypeJSON = {"type": "input.json"};
    httpBackend.when('GET', '/data-templates/input.json').respond(fakeFragmentTemplateByTypeInput);

    srv.getFragmentTemplateByType().get(fragmentTypeJSON).$promise.then(function(result) {
      expect(JSON.stringify(result)).toEqual(JSON.stringify(fakeFragmentTemplateByTypeInput));
    });

    rootScope.$digest();
    httpBackend.flush();
  });

  it("Should return output templates", function() {
    var fragmentTypeJSON = {"type": "output.json"};
    httpBackend.when('GET', '/data-templates/output.json').respond(fakeFragmentTemplateByTypeOutput);

    srv.getFragmentTemplateByType().get(fragmentTypeJSON).$promise.then(function(result) {
      expect(JSON.stringify(result)).toEqual(JSON.stringify(fakeFragmentTemplateByTypeOutput));
    });

    rootScope.$digest();
    httpBackend.flush();
  });

  it("Should return policy templates", function() {
    httpBackend.when('GET', '/data-templates/policy.json').respond(fakePolicyTemplate);

    srv.getPolicyTemplate().get().$promise.then(function(result) {
      expect(JSON.stringify(result)).toEqual(JSON.stringify(fakePolicyTemplate));
    });

    rootScope.$digest();
    httpBackend.flush();
  });

  describe("Should return dimension template by type", function() {
    beforeEach(function() {
      httpBackend.when('GET', '/data-templates/dimension/default.json').respond(fakeDefaultDimensionTemplate);
      httpBackend.when('GET', '/data-templates/dimension/date-time.json').respond(fakeDateTimeTemplate);
      httpBackend.when('GET', '/data-templates/dimension/geo-hash.json').respond(fakeGeoHashTemplate);
    });
    it('if dimension is default, default template is returned', function() {
      var dimensionTypeJSON = {"type": "default.json"};
      srv.getDimensionTemplateByType().get(dimensionTypeJSON).$promise.then(function(result) {
        expect(JSON.stringify(result)).toEqual(JSON.stringify(fakeDefaultDimensionTemplate));
      });
      rootScope.$digest();
      httpBackend.flush();
    });

    it('if dimension is date time, date time template is returned', function() {
      var dimensionTypeJSON = {"type": "date-time.json"};
      srv.getDimensionTemplateByType().get(dimensionTypeJSON).$promise.then(function(result) {
        expect(JSON.stringify(result)).toEqual(JSON.stringify(fakeDateTimeTemplate));
      });
      rootScope.$digest();
      httpBackend.flush();
    });

    it('if dimension is geo hash, geo hash template is returned', function() {
      var dimensionTypeJSON = {"type": "geo-hash.json"};
      srv.getDimensionTemplateByType().get(dimensionTypeJSON).$promise.then(function(result) {
        expect(JSON.stringify(result)).toEqual(JSON.stringify(fakeGeoHashTemplate));
      });
      rootScope.$digest();
      httpBackend.flush();
    });

  });

  describe("Should return operator template by type", function() {
    beforeEach(function() {
      httpBackend.when('GET', '/data-templates/operator/count.json').respond(fakeCountOperatorTemplate);
      httpBackend.when('GET', '/data-templates/operator/default.json').respond(fakeDefaultOperatorTemplate);
    });
    it('if operator is a count, it returns its specific template', function() {
      var operatorTypeJSON = {"type": "count.json"};
      srv.getOperatorTemplateByType().get(operatorTypeJSON).$promise.then(function(result) {
        expect(JSON.stringify(result)).toEqual(JSON.stringify(fakeCountOperatorTemplate));
      });
      rootScope.$digest();
      httpBackend.flush();
    });

    it('if operator is not a count, it returns the default template', function() {
      var operatorTypeJSON = {"type": "default.json"};
      srv.getOperatorTemplateByType().get(operatorTypeJSON).$promise.then(function(result) {
        expect(JSON.stringify(result)).toEqual(JSON.stringify(fakeDefaultOperatorTemplate));
      });
      rootScope.$digest();
      httpBackend.flush();
    });

  });

  describe("Should return trigger template by type", function() {
    beforeEach(function() {
      httpBackend.when('GET', '/data-templates/trigger/transformation.json').respond(fakeTransformationTriggerTemplate);
      httpBackend.when('GET', '/data-templates/trigger/cube.json').respond(fakeCubeTriggerTemplate);
    });
    it('if is a transformation trigger, it returns its specific template', function() {
      var triggerTypeJSON = {"type": "transformation.json"};
      srv.getTriggerTemplateByType().get(triggerTypeJSON).$promise.then(function(result) {
        expect(JSON.stringify(result)).toEqual(JSON.stringify(fakeTransformationTriggerTemplate));
      });
      rootScope.$digest();
      httpBackend.flush();
    });

    it('if is a cube trigger, it returns the default template', function() {
      var triggerTypeJSON = {"type": "cube.json"};
      srv.getTriggerTemplateByType().get(triggerTypeJSON).$promise.then(function(result) {
        expect(JSON.stringify(result)).toEqual(JSON.stringify(fakeCubeTriggerTemplate));
      });
      rootScope.$digest();
      httpBackend.flush();
    });

  });
});
