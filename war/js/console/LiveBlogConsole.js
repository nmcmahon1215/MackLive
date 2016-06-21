/**
 * This is the javascript for building and operating the console window
 */

LiveBlogConsole = function(containerId) {
	this.containerId = containerId;
    this.container = document.getElementById(this.containerId);
	this.form = null;
	this.$container = $j(this.container);
};

LiveBlogConsole.prototype = {
		render: function() {

			var liveBlogFeedElement = document.getElementById("liveBlogFeed");
			this.liveBlogFeed = new LiveBlogFeed(liveBlogFeedElement);
			this.liveBlogFeed.render();

			this.form = document.getElementById("liveBlogForm");
			this.textArea = document.getElementById("liveBlogInput");

			this.textArea.addEventListener("keypress", function(event){
				if (event.keyCode == 13) {
					this.submitMessage();
					return false;
				}
			}.bind(this));

			this.textArea.addEventListener("keyup", this.validateButton.bind(this));
			this.nameField = document.getElementById("nameInput");
			this.nameField.addEventListener("keyup", this.validateButton.bind(this));
			this.submitButton = document.getElementById("liveBlogSubmitButton");
		},
		initialize: function () {
            this.initialized = true;
            this.validateButton();
			this.liveBlogFeed.initialize();
        },
		submitMessage: function () {
            if (this.textArea.value.trim() == "" || this.nameField.value.trim() == "") {
                return;
            }

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

					this.liveBlogFeed.fetchNewMessages();
				},
				error: function(result) {
					alert("Could not post message!");
				}
			})
        },
    validateButton: function () {
        if (this.textArea.value.trim() == "" || this.nameField.value.trim() == "" || !this.initialized) {
            this.submitButton.disabled = true;
        } else {
            this.submitButton.disabled = false;
        }
    }
};
