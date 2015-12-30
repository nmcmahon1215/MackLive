/**
 * This is the javascript for building and operating the console window
 */

LiveBlogConsole = function(containerId) {
	this.containerId = containerId;
	this.container = document.getElementById(containerId);
	this.form = null;
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
			
			this.form = document.createElement("form");
			$j(this.form).on("submit", function(event){
				var callEvent = event || window.event;
				callEvent.preventDefault();
				this.submitMessage();
				return false;
			}.bind(this))
			
			this.textArea = document.createElement("textarea");
			this.textArea.id = "liveBlogInput";
			this.textArea.className = "liveInput fadeBG";
			this.textArea.placeholder = "Enter a post";
			
			this.nameLabel = document.createElement("label");
			this.nameLabel.id = "nameLabel";
			this.nameLabel.setAttribute("for", "nameInput");
			this.nameLabel.innerHTML = "Posting as: ";
			
			this.nameField = document.createElement("input");
			this.nameField.id = "nameInput";
			this.nameField.type = "text";
			this.nameField.placeholder = "(Optional) Your name";
			
			this.submitButton = document.createElement("input");
			this.submitButton.type = "submit";
			this.submitButton.value = "Submit";
			this.submitButton.className = "action floatR";
			this.submitButton.style.margin = "10px";
			this.submitButton.disabled = true;
			
			$j(this.form).append(this.nameLabel, this.nameField, this.textArea, this.submitButton)
			
			this.$container.append(header, liveBlogFeedElement, this.form);
		},
		initialize: function () {
			this.submitButton.disabled = false;
		},
		submitMessage: function () {
			var message = {
				author: this.nameField.value,
				text: this.textArea.value,
				game: adminConsole.gameId,
			};
			
			$j.ajax({
				url: "/api/messages",
				method: "POST",
				context: this,
				contentType: "application/json",
				data: JSON.stringify(message),
				success: function(result) {
					this.textArea.value = "";
					this.textArea.classList.remove("fadeBG");
					
					setTimeout(function(){
						this.textArea.classList.add("fadeBG");
					}.bind(this), 100);
				},
				error: function(result) {
					alert("Could not post message!");
				}
			})
		}
};