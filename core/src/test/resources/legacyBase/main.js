//handleHttpRequest_initState
if (!state.orgs) {
    state.orgs = {};
    state.orgs.org1 = {id: 'test'}
    state.orgs.org2 = {id: 'blhe'}
};

Sandbox.define("/handleHttpRequest_initState", function(req, res) { res.send('') })

//handleHttpRequest_caseInsensitiveGet
Sandbox.define("/handleHttpRequest_caseInsensitiveGet", function(req, res) { var value = function(request){ return request.get('blahBlah'); }(req); res.send(value) })

//handleHttpRequest_basicDate
Sandbox.define("/handleHttpRequest_basicDate", function(req, res) { res.json(200, JSON.stringify({status: new Date()})); })

//handleHttpRequest_basicFaker
Sandbox.define("/handleHttpRequest_basicFaker", function(req, res) { res.send( 200, faker.address.city() ); })

//handleHttpRequest_fakerRandom
Sandbox.define("/handleHttpRequest_fakerRandom", function(req, res) { res.send( 200, faker.random.number() ); })

//handleHttpRequest_basicMoment
Sandbox.define("/handleHttpRequest_basicMoment", function(req, res) { res.send(moment().format()); })

//handleHttpRequest_runBasicService
Sandbox.define("/handleHttpRequest_runBasicService", function(req, res) { res.send("hello world") })

//handleHttpRequest_runBasicServiceWithNulls
Sandbox.define("/handleHttpRequest_runBasicServiceWithNulls", function(req, res) { var x = { blah: null, msg: 'hello world' }; res.json(x) })

//handleHttpRequest_runBasicPOSTService
Sandbox.define("/handleHttpRequest_runBasicPOSTService", "POST", function(req, res) { res.send("hello world") })

//handleHttpRequest_runBasicHeadersService
Sandbox.define("/handleHttpRequest_runBasicHeadersService", {'SOAPAction':'blah'}, function(req, res) { res.send("hello world") })

//handleHttpRequest_runHeadersWithCacheControlService
Sandbox.define("/handleHttpRequest_runHeadersWithCacheControlService", 'GET', function(req, res) {
    res.set('Cache-Control', 'max-age: 3600');
    res.send("hello world")
})

//handleHttpRequest_runBasicSoapService
Sandbox.soap("/handleHttpRequest_runBasicSoapService", 'blahAction', function(req, res) { res.send("hello world") })

//handleHttpRequest_runBasicSoapOperationNameService
Sandbox.soap("/handleHttpRequest_runBasicSoapOperationNameService", 'blahAction', 'blahOperation', function(req, res) { res.send("hello world") })

//handleHttpRequest_runBasicValidationService
Sandbox.define("/handleHttpRequest_runBasicValidationService", function(req, res){
	req.checkQuery('q','Invalid query param').notEmpty();
	var errors = req.validationErrors();
	if (errors) {
  		res.send(400,req.validationErrorsJson());
  		return;
	}
	res.send("hello world")
})

//handleHttpRequest_runInnerScope
var selfexecuting = function(){
	var x = 'blah'
	Sandbox.define('/handleHttpRequest_runInnerScope', function(req, res){
		res.send(x)
	})
};selfexecuting()

//handleHttpRequest_runBasicUrlEncodedBody
Sandbox.define("/handleHttpRequest_runBasicUrlEncodedBody", function(req, res) { res.send(req.body.attr + '-' + req.body.first) })

//handleHttpRequest_runRootBasicService
Sandbox.define("/", function(req, res) { res.send("hello world") })

//handleHttpRequest_sendWithJsonObject
Sandbox.define("/handleHttpRequest_sendWithJsonObject", function(req, res) { res.send({username: "nick", firstname:"nick"}) })

//handleHttpRequest_sendWithJsonInt
Sandbox.define("/handleHttpRequest_sendWithJsonInt", function(req, res) { res.json({id: Number(1) }) })

//handleHttpRequest_sendWithJsonIntArray
Sandbox.define("/handleHttpRequest_sendWithJsonIntArray", function(req, res) { res.json(200, [{id: Number(1) }]) })

//handleHttpRequest_sendWithJsonArray
Sandbox.define("/handleHttpRequest_sendWithJsonArray", function(req, res) { res.send([{username: "nick"}, {username: "ando"}]) })

//handleHttpRequest_sendWithJsonAssortedObject
Sandbox.define("/handleHttpRequest_sendWithJsonAssortedObject", function(req, res) { res.send([{username:"nick"},1,"a string",["nested","array"]]) })

//handleHttpRequest_jsonWithString
Sandbox.define("/handleHttpRequest_jsonWithString", function(req, res) { res.json("this is not json") })

//handleHttpRequest_jsonWithFunction
Sandbox.define("/handleHttpRequest_jsonWithFunction", function(req, res) { res.json(function() {}) })

//handleHttpRequest_validXmlBody
Sandbox.define("/handleHttpRequest_validXmlBody", function(req, res) { res.type("xml"); res.send(req.xmlDoc.toString()) })

//handleHttpRequest_validXpath
Sandbox.define("/handleHttpRequest_validXpath", function(req, res) { res.type("xml"); res.send(req.xmlDoc.get("//*[name()='user']").toString()) })

//handleHttpRequest_xmlFindLoop
Sandbox.define("/handleHttpRequest_xmlFindLoop", function(req, res) { 
	var result  = ""
	_.each(req.xmlDoc.find("//getQuantities"), function(item, index){
   		result += item.get('inventoryItemName').text()
	})
	res.send(result) 
})

//handleHttpRequest_invalidXpath
Sandbox.define("/handleHttpRequest_invalidXpath", function(req, res) { res.send(req.xmlDoc.get("//*[name()='APCN']")) })

//handleHttpRequest_invalidXpath2
Sandbox.define("/handleHttpRequest_invalidXpath2", function(req, res) { 
	res.type("xml"); 
	var blah = req.xmlDoc.get("//*[name()='APCN']").substring(1); 
	res.send(blah) 
})

//handleHttpRequest_invalidXmlbody
Sandbox.define("/handleHttpRequest_invalidXmlbody", function(req, res) { 
	if (req.body.isInvalid) { res.send('isInvalid') } else { res.send('valid') } 
})

//handleHttpRequest_xmlNestedXpath

Sandbox.define('/handleHttpRequest_xmlNestedXpath', 'POST', function(req, res){
	function pad(a,b){return(1e15+a+"").slice(-b)};

    var report = {};
    report.flight_num = pad(req.xmlDoc.get("//*[local-name()='FlightNumber']").text(), 4);
    report.departure_port = req.xmlDoc.get("//*[local-name()='DeparturePort']").text();
    report.flight_datetime = moment(req.xmlDoc.get("//*[local-name()='DepartureDate']").text(), 'DD/MM/YYYYThh:mm');
    report.flight_date = report.flight_datetime.format('DDMMYY'); 
    report.flight_time = report.flight_datetime.format('HHmm');
    report.report_type = 'FTB';
    report.time_stamp = moment().zone('+10:00').format('DDMMYYYYHHmmss');
    report.ssss = 'FLOG';
    report.event_type = 'FT';
    
    report.title = report.event_type + report.flight_num + report.flight_date + report.departure_port + 
                   report.time_stamp + report.flight_time + report.ssss + report.event_type + '.txt';
    
    report.passengers = [];
    
    var nodelist = req.xmlDoc.find("//*[local-name()='Passenger']");  
    
    for (var x=0; x < nodelist.getLength(); x++) {
          var node = nodelist.item(x);
          var passenger = {};
          passenger.title = node.getNodeName();
          passenger.seq = node.get("//*[local-name()='BoardingSequence']").text();
          report.passengers.push(passenger);
    }
    
    return res.json(report);
});

//handleHttpRequest_simpleRender
Sandbox.define("/handleHttpRequest_simpleRender", function(req, res) { res.render('a',req.body) } )

//handleHttpRequest_includeRender
Sandbox.define("/handleHttpRequest_includeRender", function(req, res) { res.render('a',req.body) } )

//handleHttpRequest_exceededRender
Sandbox.define("/handleHttpRequest_exceededRender", function(req, res) { 
	var args = {}; 
	for(var x=0; x<10000;x++) { 
		args[x]='sdl,fjksldjkdfsjkldsflkjsfdjlksfdjlksdfjlksdfjlksdfjlksldfjkjlkfsdsdflsdfjlksdfjlkdsfljksdfjlfkdssdflkjsdflksjdflksjdflkjsflwiejfopqijfqlskdjqlsidjqwldjqwd' 
	}; 
	res.render('a',{ 'hash': args}) 
})

//handleHttpRequest_invalidIncludeRender
Sandbox.define("/handleHttpRequest_invalidIncludeRender", function(req, res) { res.render('loop',req.body) } )

//handleHttpRequest_endlessLoopIncludeRender
Sandbox.define("/handleHttpRequest_endlessLoopIncludeRender", function(req, res) { res.render('loop',req.body) } )

//handleHttpRequest_loopRender
Sandbox.define("/handleHttpRequest_loopRender", function(req, res) { res.render('a',{trackingNumbers:['a','b','c']}) } )

//handleHttpRequest_deepLoopRender
Sandbox.define("/handleHttpRequest_deepLoopRender", function(req, res) { res.render('a',{trackingNumbers:['a','b',{c:['1','2','3']}]}) } )

//handleHttpRequest_validJsonObjectbody
Sandbox.define("/handleHttpRequest_validJsonObjectbody", function(req, res) { res.send(req.body) })

//handleHttpRequest_testIsJsonBody
Sandbox.define("/handleHttpRequest_testIsJsonBody", function(req, res) { if(req.is('json')) { res.send('json') } else { res.send('not')} })

//handleHttpRequest_validJsonArraybody
Sandbox.define("/handleHttpRequest_validJsonArraybody", function(req, res) { res.send(req.body) })

//handleHttpRequest_checkJsonSchema
Sandbox.define("/handleHttpRequest_checkJsonSchema", function(req, res) { 
	req.checkJsonSchema('request'); 
	if(req.validationErrors()) { 
		res.send(req.validationErrorsJson()); 
	} else { 
		res.send('OK') 
	} 
})

//handleHttpRequest_amandaTest
Sandbox.define("/handleHttpRequest_amandaTest", function(req, res) { 
	var schema = {
	    "$schema": "http://json-schema.org/draft-04/schema#",
	    "title": "Product",
	    "description": "A product from Acmes catalog",
	    "type": "object",
	    "properties": {
	            "id": {
	                "description": "The unique identifier for a product",
	                "type": "integer",
	            },
	            "name": {
	                "description": "Name of the product",
	                "type": "string",
	                "required": true
	            },
	    }
	}
    
    // Validate the data against the schema.
    var validator = amanda('json')
    validator.validate(req.body, schema, { singleError: false }, function(error) {
      if (error !== undefined) {
           console.log(error);
          return res.send(400, 'errors: ' + JSON.stringify(error))
      }  
      
      console.log('errors: ' + error); 
      return res.send(200, 'ok')
    }, console.log)
})

//handleHttpRequest_simpleMomentTest
Sandbox.define("/handleHttpRequest_simpleMomentTest", function(req, res) { res.send(moment().format()) })

//handleHttpRequest_simpleConsoleTest
Sandbox.define("/handleHttpRequest_simpleConsoleTest", function(req, res) { console.log('console log'); res.send('response') })

//handleHttpRequest_validJsonArrayInLodash
Sandbox.define("/handleHttpRequest_validJsonArrayInLodash", function(req, res) { _.each(req.body, function(item) {}); res.send(req.body) })

//handleHttpRequest_mapLodash
Sandbox.define("/handleHttpRequest_mapLodash", function(req, res) { 
	res.send(JSON.stringify(_.map([1,2,3], function(item){
    	return item+1;
	}))) 
})

//handleHttpRequest_stringifyInternalMap
Sandbox.define("/handleHttpRequest_stringifyInternalMap", function(req, res) { res.send(JSON.stringify({a:1, b: null})) })

//handleHttpRequest_mapPathParams
Sandbox.define('/handleHttpRequest_mapPathParams/{blah}', function(req, res) { if (Object.keys(req.params).length){ res.send('1'); } else { res.send('2'); } })

//handleHttpRequest_mapLodashObjectEach
Sandbox.define("/handleHttpRequest_mapLodashObjectEach", function(req, res) { 
	var orgs = [{
	    id: 1234,
	    name: 'username'
	}]

	var matchedOrg = _.find(orgs, function(existingOrg) {
	    return existingOrg.id == '1234'
	})
	
	res.json([matchedOrg]) 
})

//handleHttpRequest_validJsonAssortedArraybody
Sandbox.define("/handleHttpRequest_validJsonAssortedArraybody", function(req, res) { res.send(req.body) })

//handleHttpRequest_setStatus
Sandbox.define("/handleHttpRequest_setStatus", function(req, res) { res.send(400, 'test') })

//handleHttpRequest_setStatusSeparately
Sandbox.define("/handleHttpRequest_setStatusSeparately", function(req, res) { res.status(400); res.send('test') })

//handleHttpRequest_getMissingHeader
Sandbox.define("/handleHttpRequest_getMissingHeader", function(req, res) { res.send(req.get('notthere') == undefined ? 'missing' : 'exists') })

//handleHttpRequest_sendUndefined
Sandbox.define("/handleHttpRequest_sendUndefined", function(req, res) { res.send([undefined]) })

//handleHttpRequest_underscoreRemoveWeirdness
Sandbox.define("/handleHttpRequest_underscoreRemoveWeirdness", function(req, res) { var smth = [{id:'1'}]; _.remove(smth,function(obj) { return true }); res.send(smth) })

//handleHttpRequest_noResBodyException
Sandbox.define("/handleHttpRequest_noResBodyException", function(req, res) { console.log('meep') })

//handleHttpRequest_ReferenceErrorException
Sandbox.define("/handleHttpRequest_ReferenceErrorException", function(req, res) { console.log('meep'); var blah = {shipmentId: shipmentIds }} )

//handleHttpRequest_TypeErrorException
Sandbox.define("/handleHttpRequest_TypeErrorException", function(req, res) { console.log('meep'); var blah = 1234; blah.substring(2) })

//handleHttpRequest_RangeErrorException
Sandbox.define("/handleHttpRequest_RangeErrorException", function(req, res) { var list = new Array(-1) })

//handleHttpRequest_URIErrorException
Sandbox.define("/handleHttpRequest_URIErrorException", function(req, res) { decodeURIComponent('%'); res.send('') })

//handleHttpRequest_noMatchingTemplateException
Sandbox.define("/handleHttpRequest_noMatchingTemplateException", function(req, res) { res.render('blah') })

//handleHttpRequest_requireJSONFile
Sandbox.define("/handleHttpRequest_requireJSONFile", function(req, res) { res.json(require('./child.json')); })






