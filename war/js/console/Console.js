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
		this.setGameName("N/A");
		this.loadRecentGames();
		
		$j(this.createButton).click(this.createGame.bind(this));
	},

	setGameName: function (name) {
		this.gameName = name;
		this.titleElement.innerHTML = "Current Game: " + this.gameName;
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
		var team1 = document.getElementById("team1select").value;
		var team2 = document.getElementById("team2select").value;
		
		$j.ajax({
			url:location.protocol + '//' + location.host + "/api/game",
			method: "POST",
			contentType: "text/plain",
			dataType: "application/json",
			data: team1 + "," + team2,
			context: this,
			success: function(response){
				this.gameId = response.id;
				this.setGameName(response.name);
			},
			failure: function (response) {
				alert("Failed to create new game.");
			},
		})
	}
}