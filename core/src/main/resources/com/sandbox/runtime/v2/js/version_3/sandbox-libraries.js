(function(global){

    //load wrapper common libraries so we can lazy load them
    const evalLibrary = function(libraryVersion, libraryFileName, libraryObjectName) {
        var scriptSource = __service.readLibrary(libraryVersion, libraryFileName);
        var evalResult = eval(scriptSource);
        return this[libraryObjectName];
    }

    const resolveLazyTarget = function(target, libraryVersion, libraryFileName, libraryObjectName){
        evalLibrary(libraryVersion, libraryFileName, libraryObjectName);
        target = this[libraryObjectName];
        return target;
    }

    const lazyLoadedLibrary = (resolvedTarget, libraryVersion, libraryFileName, libraryObjectName) => ({
        proxy: new Proxy(
            //this is defined as a function, rather than a straight obj so lazy obj can be called direct as a function like '_(objs).smth()'
            function(){},
            {
                get: function(target, property) {
                    if(resolvedTarget == undefined) {
                        target = resolveLazyTarget(target, libraryVersion, libraryFileName, libraryObjectName);
                        resolvedTarget = target;
                    }
                    return resolvedTarget[property];
                },
                apply: function(target, thisArg, argumentsList) {
                    if(resolvedTarget == undefined) {
                        target = resolveLazyTarget(target, libraryVersion, libraryFileName, libraryObjectName);
                        resolvedTarget = target;
                    }
                    return resolvedTarget(argumentsList);
                },
                construct: function(target, args) {
                    if(resolvedTarget == undefined) {
                        target = resolveLazyTarget(target, libraryVersion, libraryFileName, libraryObjectName);
                        resolvedTarget = target;
                    }
                    return new resolvedTarget(...args);
                }
            }
        )
    });

    const getLibraryObject = function(libraryVersion, libraryFileName, libraryObjectName) {
        var resolvedTarget = undefined
        const { proxy } = lazyLoadedLibrary(resolvedTarget, libraryVersion, libraryFileName, libraryObjectName);
        return proxy
    }

    const libraryVersion = 'version_3'
    global.faker = getLibraryObject(libraryVersion, "faker-4.1.0.min.js", "faker");
    global._ = getLibraryObject(libraryVersion, "lodash-4.17.11.min.js", "_");
    global.moment = getLibraryObject(libraryVersion, "moment-2.24.0.min.js", "moment");

    global.validator = evalLibrary(libraryVersion, "validator-10.11.0.min.js", "validator");
    global.Ajv = evalLibrary(libraryVersion, "ajv-6.10.0.min.js", "Ajv");

}(this));