/**
 * Creates a feed window for display blog posts and comments
 */
LiveBlogFeed = function(container, blogId) {
	this.blogId = blogId;
	this.container = container;
	this.$container = $j(this.container);
	this.latestMessageDate = new Date(0);
};

LiveBlogFeed.prototype = {
	render: function() {
		this.container.className = "consoleScrollerFeed";
	},
	initialize: function() {
		this.container.innerHTML = "";
		this.fetchAllMessages();
		setInterval(function(){
			this.fetchNewMessages();
		}.bind(this), 5000);
	},
	addMessage: function (message) {
		var panel = document.createElement("div");
		var header = document.createElement("div");
		var content = document.createElement("div");
		
		panel.className = "panel panel-info";
		header.className = "panel-heading";
		content.className = "panel-body";
		
		header.innerHTML = message.author;
		content.innerHTML = message.text;
		
		panel.appendChild(header);
		panel.appendChild(content);
		
		this.container.appendChild(panel);
	},
	fetchAllMessages: function() {
		$j.ajax({
			url: location.protocol + '//' + location.host + "/api/messages/" + adminConsole.gameId,
			success: function(result) {
				var messages = result.messages;
				messages.forEach(function(message){
					this.addMessage(message);
				}.bind(this));
				
				if (result.latestTime){
					this.lastMessageDate = new Date(result.latestTime);
				}
				
			}.bind(this),
			error: function (result, error, desc){
				alert("Error: " + desc);
			}
				
		});
	},
	fetchNewMessages: function() {
		$j.ajax({
			url: location.protocol + "//" + location.host + "/api/messages/" + adminConsole.gameId + "/" + this.lastMessageDate.getTime(),
			success: function(result) {
				var messages = result.messages;
				messages.forEach(function(message){
					this.addMessage(message);
				}.bind(this));
				
				if (result.latestTime){ 
					this.lastMessageDate = new Date(result.latestTime);
				}
				
			}.bind(this),
			error: function (result, error, desc){
				alert("Error: " + desc)
			},
		})
	},
};
