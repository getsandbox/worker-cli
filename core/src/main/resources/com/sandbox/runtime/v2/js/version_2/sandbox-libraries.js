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

    const libraryVersion = "version_2"
    global.faker = getLibraryObject(libraryVersion, "faker-3.0.1.min.js", "faker");
    global._ = getLibraryObject(libraryVersion, "lodash-4.2.1.min.js", "_");
    global.moment = getLibraryObject(libraryVersion, "moment-2.11.2.min.js", "moment");

    global.validator = evalLibrary(libraryVersion, "validator-4.7.2.min.js", "validator");
    global.amanda = evalLibrary(libraryVersion, "amanda-0.4.8.min.js", "amanda");


}(this));