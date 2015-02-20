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

sandboxValidator(wrappedRequest, nashornUtils)

_matchedFunction.run(wrappedRequest, _currentResponse)