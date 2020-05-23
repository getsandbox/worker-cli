Sandbox.define('/test', function(req, res){
    req.checkJsonSchema('request');
    if(req.validationErrors()) {
        res.send(req.validationErrorsJson());
    } else {
        res.send('OK')
    }
})