/**
 * This is the javascript for building and operating the console window
 */

LiveBlogConsole = function(containerId) {
	this.containerId = containerId;
	this.container = document.getElementById(containerId);
	this.$container = $j(this.container);
};

LiveBlogConsole.prototype = {
		render: function() {
			var header = document.createElement("h2");
			header.innerHTML = "Live Feed";
			
			var liveBlogFeedElement = document.createElement("div");
			liveBlogFeedElement.id = 'liveBlogFeed';
			liveBlogFeedElement.className = 'consoleScrollerFeed';
			this.liveBlogFeed = new LiveBlogFeed(liveBlogFeedElement);
			
			this.textArea = document.createElement("textarea");
			this.textArea.id = "liveBlogInput";
			this.textArea.className = "liveInput";
			this.textArea.placeholder = "Enter a post";
			
			this.submitButton = document.createElement("input");
			this.submitButton.type = "button";
			this.submitButton.value = "Submit";
			this.submitButton.className = "action floatR";
			this.submitButton.style.margin = "10px";
			
			this.$container.append(header, liveBlogFeedElement, this.textArea, this.submitButton);
		}
};