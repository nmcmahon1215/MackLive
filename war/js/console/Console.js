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
	},

	setGameName: function (name) {
		this.gameName = name;
		this.titleElement.innerHTML = "Current Game: " + this.gameName;
	},
}