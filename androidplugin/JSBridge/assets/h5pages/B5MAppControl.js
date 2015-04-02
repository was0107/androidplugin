function B5MAppControl(domNode) {
	this.domNode = domNode;
	this.init();
};

$.extend(B5MAppControl.prototype, {
	init: function() {
		this._touchStartTime = 0;
		this._touch = null;

		this._longPressDelay = 1.5;
		this._longPressTimer = null;
		this._longPressFired = false;

		$(this.domNode).addClass('B5MAppControl');
		this.bindEvent();
	},

	bindEvent: function() {
		$(this.domNode).on('touchstart',$.proxy(this,'onTouchStart'))
					.on('touchmove',$.proxy(this, 'onTouchMove'))
					.on('touchend', $.proxy(this, 'onTouchEnd'));
	},

	onTouchStart: function(evt) {
		this._touchStartTime = new Date().getTime();
		this._longPressFired = false;
		this._clearLongPressTimer();
		this._longPressTimer = setTimeout($.proxy(this, '_fireLongPress'), this._longPressDelay * 1000);
		this._startScrollTop = $(window).scrollTop();
		this._startScrollLeft = $(window).scrollLeft();
		this._scrolled = false;

		$(this.domNode).addClass('B5MAppControl-highlight');

	},

	onTouchMove: function(evt) {
		this._clearLongPressTimer();
		var touch = evt.touches[0];
		if (touch) {
			this._touch = touch;
		};

		this._scrolled = true;
		$(this.domNode).removeClass('B5MAppControl-highlight');
	},

	onTouchEnd: function(evt) {
		this._clearLongPressTimer();
		if (this._longPressFired) return;
		var touchInside = !this._touch || this._inRect({x: this._touch.pageX,y: this._touch.pageY},
			$(this.domNode).offset());
		$(this.domNode).removeClass('B5MAppControl-highlight');
		this._touch = null;
		if (!this._scrolled) {
			touchInside ? this.onClick() : this.onTouchUpOutside();
		};
	},

	onClick: function() {

	},

	onTouchUpOutside: function() {

	},

	onLongPress: function() {

	},

	dispatchEvent: function(type, canBubble, cancelable) {
		// @see https://developer.mozilla.org/en-US/docs/Web/API/event.initMouseEvent
		var evt = document.createEvent('MouseEvents');
		evt.initMouseEvent.apply(evt, Array.protype.slice.call(arguments));
		this.domNode.dispatchEvent(evt);
	},

	_fireLongPress: function() {
		$(this.domNode).removeClass('B5MAppControl-highlight');
		this._longPressFired = true;
		this.onLongPress();
	},

	_clearLongPressTimer: function() {
		if (this._longPressTimer != null) {
			clearTimeout(this._longPressTimer);
			this._longPressTimer = null;
		};
	},

	_inRect: function(point, rect) {
		return rect.left <= point.x && point.x <= rect.left + rect.width 
				&& rect.top <= point.y && point.y <= rect.top + rect.height;
	},

	emptyFn: function() {

	},

});


//////////////////////////
///loading page 
function B5MLoading() {}
$.extend(B5MLoading.prototype, {
	domNode: null,
	_init: function() {
		var div = document.createElement('div');
		$(div).addClass('main_loading');

		var maskerDiv = document.createElement('div');
		$(maskerDiv).addClass('main_loading_masker');

		var activitorDiv = document.createElement('div');
		$(activitorDiv).addClass('main_loading_activitor');

		this.domNode = div;

		$(this.domNode).on('touchstart', function(evt) {
			evt.preventDefault();
		})
	},

	layoutSubviews: function() {
		var masker = $(this.domNode).children('.main_loading_masker');
		var activitor = masker.children('.main_loading_activitor');
		masker.css('margin-top',parseInt(($this.domNode).height() - masker.height)/2 + 'px');
	},

	show: function() {
		if (this.domNode == null) {
			this._init();
		};
		document.body.appendChild(this.domNode);
		this.layoutSubviews();
	},

	hide: function() {
		this.domNode.parent.removeChild(this.domNode);
	}
});







