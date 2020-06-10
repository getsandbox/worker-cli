Sandbox.define('/explicit', function(req, res){
    res.render('liquid', 'test', { value: 'hello world'})
})

Sandbox.define('/invalid', function(req, res){
    res.render('junk', 'test', { value: 'hello world'})
})

Sandbox.define('/context', function(req, res){
    res.render('test', { value: 'hello world'})
})

Sandbox.define('/nocontext', function(req, res){
    res.render('test')
})