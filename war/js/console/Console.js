/**
 * This JS controls functions for the Console as a whole
 */

Console = function() {
	this.gameName = null;
	this.titleElement = document.getElementById("gameTitle");
	this.gamePicker = document.getElementById("gamePicker");
	this.loadButton = document.getElementById("loadButton");
	this.createButton = document.getElementById("createButton");
	this.initialize();
}

Console.prototype = {
	initialize: function() {
		this.titleElement.innerHTML = "<div style='background:red;'>No game selected.</div>";
		this.loadRecentGames();
		
		$j(this.createButton).click(this.createGame.bind(this));
	},

	setGameName: function (name) {
		$j(this.titleElement).slideUp(500, function(){
			this.gameName = name;
			this.titleElement.innerHTML = "Current Game: " + this.gameName;
			$j(this.titleElement).slideDown();
		}.bind(this));
		if (this.gameId){
			if (!this.idMessage){
				this.idMessage = document.createElement("div");
				this.idMessage.style.position = "absolute";
				this.idMessage.style.right = "0px";
				this.idMessage.style.marginTop = "5px";
				this.loadButton.parentElement.appendChild(this.idMessage);
			}
			this.idMessage.innerHTML = "Game id: " + this.gameId;
		}
	},
	loadRecentGames: function() {
		$j.ajax({
			url: location.protocol + '//' + location.host + "/api/game/recent/5",
			context: this,
			success: function(response) {
				var games = response.split(",");
				this.gamePicker.innerHTML = "<option>Select A Game...</option>";
				games.forEach(function (game){
					if (game != ""){
						this.gamePicker.innerHTML += "<option>" + game + "</option>";
					}
				});
			},
			failure: function(response) {
				alert("Could not fetch recent games.");
			}
		})
	},
	createGame: function () {
		var today = new Date();
		var team1select = document.getElementById("team1select");
		var team2select = document.getElementById("team2select");
		
		var team1 = team1select.children[team1select.selectedIndex];
		var team2 = team2select.children[team2select.selectedIndex];
		
		$j.ajax({
			url:location.protocol + '//' + location.host + "/api/game",
			method: "POST",
			contentType: "text/plain",
			dataType: "json",
			data: team1.id + "," + team2.id,
			context: this,
			timeout: 2000,
			success: function(response){
				this.gameId = response.id;
				this.setGameName(response.Name);
			},
			error: function (response, errorType, errorThrown) {
				alert("Failed to create new game.");
			},
		})
	}
}