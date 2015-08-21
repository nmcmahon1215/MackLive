/**
 * Creates a feed window for display blog posts and comments
 */
LiveBlogFeed = function(container, blogId) {
	this.blogId = blogId;
	this.container = container;
	this.$container = $j(this.container);
};

LiveBlogFeed.prototype = {
	render: function() {
		
	},
};