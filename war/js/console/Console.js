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
	initialize : function() {
		this.titleElement.innerHTML = "No game selected.";
		this.titleElement.style.backgroundColor = "red";
		this.loadRecentGames();

		$j(this.createButton).click(this.createGame.bind(this));
		$j(this.loadButton).click(this.loadGame.bind(this));
	},

	setGameName : function(name) {
		$j(this.titleElement).slideUp(500, function() {
			this.gameName = name;
			this.titleElement.style.backgroundColor = "";
			this.titleElement.style.border = "1px solid white";
			this.titleElement.style.borderRadius = '5px';
			this.titleElement.innerHTML = this.gameName;
			$j(this.titleElement).slideDown();
		}.bind(this));
		if (this.gameId) {
			if (!this.idMessage) {
				this.idMessage = document.createElement("div");
				this.idMessage.style.position = "absolute";
				this.idMessage.style.right = "0px";
				this.idMessage.style.marginTop = "5px";
				this.loadButton.parentElement.appendChild(this.idMessage);
			}
			this.idMessage.innerHTML = "Game id: " + this.gameId;
		}
	},
	loadRecentGames : function() {
		$j
				.ajax({
					url : location.protocol + '//' + location.host
							+ "/api/game/recent/5",
					dataType : "json",
					context : this,
					success : function(response) {
						var games = response;
						this.gamePicker.innerHTML = "<option>Select A Game...</option>";
						if (!games) {
							return;
						}
						games.forEach(function(game) {
							if (game != "") {
								this.gamePicker.innerHTML += "<option id="
										+ game.key.id + ">" + game.name
										+ "</option>";
							}
						});
					},
					error : function(response, errorType, errorMessage) {
						alert("Could not fetch recent games.");
					}
				})
	},
	loadGame : function() {
		var game = this.gamePicker.children[this.gamePicker.selectedIndex];
		
		$j.ajax({
			url : location.protocol + '//' + location.host + "/api/game/"
					+ game.id,
			method : "GET",
			dataType : "json",
			context : this,
			success : function(response) {
				scoreBoard.updateInfo(response);
				liveConsole.initialize();
				this.gameId = game.id;
				this.setGameName(game.value);
			},
			error : function(response, errorType, errorStuff) {
				alert("Error loading game!");
			},
		});
	},
	createGame : function() {
		var today = new Date();
		var team1select = document.getElementById("team1select");
		var team2select = document.getElementById("team2select");

		var team1 = team1select.children[team1select.selectedIndex];
		var team2 = team2select.children[team2select.selectedIndex];

		$j.ajax({
			url : location.protocol + '//' + location.host + "/api/game",
			method : "POST",
			contentType : "text/plain",
			dataType : "json",
			data : team1.id + "," + team2.id,
			context : this,
			timeout : 2000,
			success : function(response) {
				this.gameId = response.key.id;
				this.setGameName(response.name);
			},
			error : function(response, errorType, errorThrown) {
				alert("Failed to create new game.");
			},
		})
	}
}