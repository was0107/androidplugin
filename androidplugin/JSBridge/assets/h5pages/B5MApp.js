 (function(){
  
  var callbacksCount=1;
  var callbacks={};
  
  window.B5MApp = {
    send_message: function(method, args, callback) {
        args = args || {};
  		var hascallback = callback && typeof callback == 'function';
  		var callbackId = hascallback ? callbacksCount++ : 0;
  		if (hascallback) {callbacks[callbackId] = callback;};

  		args['callbackId'] = callbackId;
  		args = (typeof args === 'object') ? JSON.stringify(args) : args + ''; 

  		//create iframe to load js 
  		var ifr = document.createElement('iframe');
  		ifr.style.display = 'none';
  		document.body.appendChild(ifr);
  		ifr.contentWindow.location.href = 'js://_?method=' + method + '&args=' + encodeURIComponent(args) + '&callbackId=' + callbackId;
  		setTimeout(function(){
  			ifr.parentNode.removeChild(ifr);
  		},0);
  		return callbackId;
    },

    callback: function(callbackId,retValue) {
    	try {
    		var callback = callbacks[callbackId];
    		if (!callback) return;
    		callback.apply(null, [retValue]);
    		delete callbacks[callbackId];
    	} catch(e) { alert(e); }
    },

    ga: function(category, action, label, value, extra) {
    	B5MApp.send_message("ga",{
    		category: category,
    		action: action,
    		label: label || '',
    		value: value || 0,
    		extra: extra || {}
    		},function(){}
    	);
    },

    ajax: function(opts) {
    	var url = opts.url;
    	var data = opts.data || {};
    	var success = opts.success || function(){};
    	var error = opts.error || function(){};

    	//create params
    	var params = [];
    	for(var p in data) {
    		if (data.hasOwnProperty(p)) {
    			params.push(p + '=' + encodeURIComponent(data[p]));
    		};
    	}
    	if (params.length > 0) {
    		url += url.indexOf('?') == -1 ? '?' : '&';
    		url += params.join('&');
    	};

    	opts.url = url;

    	var callbackId = B5MApp.send_message('ajax', opts, function(json) {
    		var errMsg = '';
    		if (0 == json.code) {
    			var response = null;
    			try {
    				response = JSON.parse(json.response);
    				success(response);
    			} catch(e) {
    				error(-2, 'parse json error ' + e.messaga);
    				console.log(json);
    			}
    		} else {
    			error(json.code, json.messaga);
    		}
    	});

    	return {cancel: function() {
    		delete callbacks[callbackId];
    	}};

    },

    action: {
    	get: function(callback) {
    		B5MApp.send_message('actionGetQuery', {}, function(query) {
    			if (!$.isFunction(callback)) return;
    			callback(query);
    		});
    	},

    	open: function(page, query, modal, animated) {
            modal = !!modal;
            if (animated === undefined) { animated = true ;}
    		B5MApp.send_message('actionOpen', {
    			page:page,
    			query:query,
    			modal:modal,
    			animated:animated,
    		}, function(){});
    	},

    	back: function(animated) {
    		if (animated === undefined) { animated = true};
    		B5MApp.send_message("actionBack", {animated:animated});
    	},

    	dismiss: function(animated) {
    		if (animated === undefined) { animated = true};
    		B5MApp.send_message("actionDismiss", {animated:animated});
    	},
    },

    getEnv: function() {
        B5MApp.send_message('getEnv',{}, function(env) {
            window.B5MAppEnv = window.B5MAppEnv || {
                parseQuery: function() {
                    if (B5MAppEnv.query == '${query}') {
                        B5MAppEnv.query = null;
                        return;
                    };
                    var params = {};
                    B5MAppEnv.query.split('&').forEach(function(item) {
                        var kv = item.split('=');
                        var k = kv[0];
                        var v = kv.length > 1 ? kv[1] :'';
                        params[k]=v;
                    });
                    B5MAppEnv.query = params;
                },
            };

            $.extend(window.B5MAppEnv, env);
            window.B5MAppEnv.parseQuery();
            callback();
        });

    },

    // startRefresh: function() {
    //     //override this method pls;
    // }

    stopRefresh: function() {
         B5MApp.send_message('stopRefresh',{},function(){});
    },

    setTitle: function(title) {
         B5MApp.send_message('setTitle',{title:title});
    },
  
  	genUUID: function(bdid) {
        bdip = bdip || B5MApp.bdid;
        var requid = CryptoJS.MD5(bdid + (new Date.getTime()) + Math.Random());
        return requid.toString();
  	},

  	takePhoto: function(callback) {
        B5MApp.send_message('imagePicker',{}, function(result) {
            $.isFunction(callback) && callback(result && result.image);
        });
  	},
  
  }

})();