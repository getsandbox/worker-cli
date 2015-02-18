var wrappedRequest = {}
_.each(_currentRequest._getAccessibleProperties(), function(property){
    if(typeof _currentRequest[property] == 'function'){
        wrappedRequest[property] = function(){
            return _currentRequest[property](Array.prototype.slice.call(arguments))
        }
    }else{
        wrappedRequest[property] = _currentRequest[property]
    }
})

sandboxValidator(wrappedRequest)

//extra methods added to support validation
wrappedRequest.checkJsonSchema = function checkJsonSchema(filename){
    // Initialize a JSON Schema validator.
    if(typeof amanda != "function") throw new Error("Failed to load JSON Schema validator")
    var jsonSchemaValidator = amanda('json')

    // load request schema from cache
    var schemaStr = nashornUtils.readFile("schemas/" + filename + ".json")
    if(typeof schemaStr != "string") throw new Error("Failed to load json schema")

    //if we have everything then validate
    jsonSchemaValidator.validate(wrappedRequest.body, schemaStr, { singleError: false }, function(error) {
        if(!error) return;
        error = JSON.parse(error)

        if (wrappedRequest._validationErrors === undefined) {
            wrappedRequest._validationErrors = [];
        }

        for (var x=0; x < error.length; x++){
            var validateError = {
                param: error[x].property,
                msg: error[x].message
            }
            wrappedRequest._validationErrors.push(validateError)
        }
    });

}
_matchedFunction.run(wrappedRequest, _currentResponse)