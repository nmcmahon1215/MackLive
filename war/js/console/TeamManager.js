/**
 * Creates a console for managing available teams.
 * Include adding, removing, and updating.
 */

TeamManagerConsole = function(containerId) {
	this.containerId = containerId;
	this.container = document.getElementById(containerId);
	this.teamPicker = document.getElementById("teamPicker");
	this.teamLogo = document.getElementById("teamLogo");
	this.teamName = document.getElementById("teamNameInput");
	this.teamAbbr = document.getElementById("teamAbbrInput");
	this.uploadButton = document.getElementById("uploadButton");
	this.saveButton = document.getElementById("saveButton");
	this.deleteButton = document.getElementById("deleteButton");
};

TeamManagerConsole.prototype = {
		/**
		 * Render is slightly different here.
		 * It simply adds necessary event listeners.
		 */
		render: function() {
			$j(this.teamName).keyup(this.validateSave.bind(this));
			$j(this.teamAbbr).keyup(this.validateSave.bind(this));
			$j(this.uploadButton).change(this.previewImage.bind(this));
			$j(this.saveButton).click(this.saveTeam.bind(this));
		},
		/**
		 * Determines if the save button should be enabled and applies it
		 * to the save button
		 */
		validateSave: function() {
			if (this.teamName.value.length > 0 && this.teamAbbr.value.length > 0){
				this.saveButton.disabled = false;
			} else {
				this.saveButton.disabled = true;
			}

		},
		/**
		 * Previews the image for upload
		 */
		previewImage: function() {
			if (this.uploadButton.files && this.uploadButton.files[0]){
				var reader = new FileReader();

				reader.onload = function (e) {
					$j(this.teamLogo).attr('src', e.target.result);
				}.bind(this);

				reader.readAsDataURL(this.uploadButton.files[0]);
			}
		},
		/**
		 * Submits the team to the server to be saved.
		 */
		saveTeam: function() {
			var xhr = new XMLHttpRequest();
			var formData = new FormData();
			
			formData.append("teamName", this.teamName.value.trim());
			formData.append("teamAbbr", this.teamAbbr.value.trim());
			if (this.uploadButton.files && this.uploadButton.files[0]){
				formData.append("teamLogo", this.uploadButton.files[0]);
			}
			
			xhr.addEventListener("load", function(){alert("Upload complete!")});
			xhr.addEventListener("error", function(){alert("Upload Failed!")});
			xhr.addEventListener("abort", function(){alert("Upload Canceled!")});
			
			xhr.open("POST", "/api/teams");
			xhr.send(formData);
		}
};

