(function(global){
    var templateRenderFunctions = {}
    var templateLibraries = {
        'liquid': function(libraryKey, __service) {
            //reuse existing engine if already configured, otherwise construct..
            if (templateRenderFunctions[libraryKey] != undefined) return templateRenderFunctions[libraryKey]

            const liquidRender = function(templateName, context, sandboxRequest) {
                var templateLocals = { res: context, data: context, req: sandboxRequest }
                return __service.renderLiquid(templateName, templateLocals)
            }
            templateRenderFunctions[libraryKey] = liquidRender
            return liquidRender
        }
    }

    const sandboxTemplates = function(wrappedResponse, __service) {
        wrappedResponse.renderWithRequest = function() {
            var templateLibrary = 'liquid'
            var templateContext = {}
            var templateName = undefined
            var sandboxRequest = arguments[0]

            if(arguments.length == 2) {
                templateName = arguments[1]
            } else if(arguments.length == 3) {
                templateName = arguments[1]
                templateContext = arguments[2]
            } else if(arguments.length == 4) {
                templateLibrary = arguments[1]
                templateName = arguments[2]
                templateContext = arguments[3]
            }

            const getRenderFunction = templateLibraries[templateLibrary]
            if (getRenderFunction == undefined) {
                throw new Error("Unsupported template library: " + templateLibrary)
            }
            const renderFunction = getRenderFunction(templateLibrary, __service)
            wrappedResponse.send(renderFunction(templateName, templateContext, sandboxRequest))
        }
    }
    global.sandboxTemplates = sandboxTemplates

}(this));