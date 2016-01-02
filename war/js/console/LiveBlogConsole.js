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
			this.liveBlogFeed = new LiveBlogFeed(liveBlogFeedElement);
			this.liveBlogFeed.render();
			
			this.form = document.createElement("form");
			$j(this.form).on("submit", function(event){
				var callEvent = event || window.event;
				callEvent.preventDefault();
				this.submitMessage();
				return false;
			}.bind(this))
			
			this.textArea = document.createElement("textarea");
			this.textArea.id = "liveBlogInput";
			this.textArea.className = "liveInput fadeBG form-control";
			this.textArea.placeholder = "Enter a post";
			this.textArea.addEventListener("keypress", function(event){
				if (event.keyCode == 13) {
		            this.submitMessage();
		            return false;
		         }
			}.bind(this));
			
			var nameDiv = document.createElement("div");
			nameDiv.className = "input-group";
			nameDiv.style.margin = "10px";
			
			this.nameLabel = document.createElement("span");
			this.nameLabel.id = "nameLabel";
			this.nameLabel.innerHTML = "Posting as: ";
			this.nameLabel.className = "input-group-addon";
			
			this.nameField = document.createElement("input");
			this.nameField.id = "nameInput";
			this.nameField.type = "text";
			this.nameField.className = "form-control";
			this.nameField.placeholder = "(Optional) Your name";
			this.nameField.setAttribute("aria-describedby", "nameLabel");
			
			$j(nameDiv).append(this.nameLabel, this.nameField);
			
			this.submitButton = document.createElement("input");
			this.submitButton.type = "submit";
			this.submitButton.value = "Submit";
			this.submitButton.className = "action floatR btn btn-primary";
			this.submitButton.style.margin = "10px";
			this.submitButton.disabled = true;
			
			$j(this.form).append(nameDiv, this.textArea, this.submitButton)
			
			this.$container.append(header, liveBlogFeedElement, this.form);
		},
		initialize: function () {
			this.submitButton.disabled = false;
			this.liveBlogFeed.initialize();
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