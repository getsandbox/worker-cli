Sandbox.define('/yo', function(req, res){
    res.set('custom-header', req.headers['custom-header'])
    res.send({a: 1, b: "yo", c: function(){}, d: /a.*/, e: [{}, {1: "abc"}]})
})

Sandbox.define('/json', 'post', function(req, res){
    res.send(req.body)
})