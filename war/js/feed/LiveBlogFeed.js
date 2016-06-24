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
		this.team1 = document.getElementById("t1");
		this.team2 = document.getElementById("t2");
		this.logo1 = document.getElementById("t1logo");
		this.logo2 = document.getElementById("t2logo");
		this.score1 = document.getElementById("score1");
		this.score2 = document.getElementById("score2");
		this.shots1 = document.getElementById("shots1");
		this.shots2 = document.getElementById("shots2");
		this.t1pp = document.getElementById("t1pp");
		this.t2pp = document.getElementById("t2pp");
		this.timer = document.getElementById("time");
		this.period = document.getElementById("period");

		this.container.className = "consoleScrollerFeed";
	},
	initialize: function() {
		this.container.innerHTML = "";
		this.fetchAllMessages();
		setInterval(this.fetchNewMessages.bind(this), 5000);
		this.initScoreboard();
	},
	initScoreboard: function () {
		$j.ajax({
			url: location.protocol + '//' + location.host + "/api/game/" + this.blogId,
			success: function (response) {
				this.logo1.src = "/api/teams/image/" + response.team1.key.id;
				this.logo2.src = "/api/teams/image/" + response.team2.key.id;

				this.team1.innerHTML = response.team1.name;
				$j(this.team1).boxfit({multiline: true});
				this.team2.innerHTML = response.team2.name;
				$j(this.team2).boxfit({multiline: true});


				this.score1.innerHTML = response.team1goals;
				this.score2.innerHTML = response.team2goals;

				this.shots1.innerHTML = "Shots: " + response.team1sog;
				this.shots2.innerHTML = "Shots: " + response.team2sog;

				response.team1pp ? $j(this.t1pp).slideDown() : $j(this.t1pp).slideUp();
				response.team2pp ? $j(this.t2pp).slideDown() : $j(this.t2pp).slideUp();

				this.timer.innerHTML = response.time;
				this.period.innerHTML = "Per " + response.period;
				
			}.bind(this),
			error: function () {
				alert("Failed to get scoreboard information.");
			}
		})
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
			url: location.protocol + '//' + location.host + "/api/messages/" + this.blogId,
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
			url: location.protocol + "//" + location.host + "/api/messages/" + this.blogId + "/" + this.lastMessageDate.getTime(),
			success: function(result) {
				var messages = result.messages;
				messages.forEach(function(message){
					this.addMessage(message);
				}.bind(this));
				
				if (result.latestTime){ 
					this.lastMessageDate = new Date(result.latestTime);
				}

				if (messages.length > 0) {
					window.scrollTo(0, document.body.scrollHeight);
				}

			}.bind(this),
			error: function (result, error, desc){
				Console.log("Error fetching new messages");
			},
		})
	},
};
