/**
 * Creates the elements for controlling the scoreboard from the console
 */

ScoreBoardConsole = function(containerId) {
	this.containerId = containerId;
	this.container = document.getElementById(containerId);
	this.$container = $j(this.container);
};

ScoreBoardConsole.prototype = {
	render: function() {
		var teamManageLink = document.createElement("a");
		teamManageLink.innerHTML = "Manage Teams";
		teamManageLink.href = "#";
		teamManageLink.className = "topRight";
		teamManageLink.onclick = function() {
			window.open('teamManager.html', 'newwindow', 'width=800px, height=350px');
		}
		
		var table = document.createElement("table");
		var tableBody = document.createElement("tbody");
		table.appendChild(tableBody);
		table.style.fontSize = "12px";
		table.style.width = "95%";
		table.style.tableLayout = "fixed";
		table.style.margin = "auto";
		
		this.team1Selector = document.createElement("select");
		this.team1Selector.id = "team1select";
		this.team1Selector.className = "teamSelect";
		this.initializeTeams(this.team1Selector);
		
		this.team2Selector = document.createElement("select");
		this.team2Selector.id = "team2select";
		this.team2Selector.className = "teamSelect";
		this.initializeTeams(this.team2Selector);
		
		this._addRowToTable(tableBody, [this.team1Selector], [this.team2Selector], "Team");
		
		this.team1ScoreBox = document.createElement("input");
		this.team1ScoreBox.type = "Number";
		this.team1ScoreBox.min = 0;
		this.team1ScoreBox.value = 0
		this.team1ScoreBox.className = "scoreBox";
		
		this.team2ScoreBox = document.createElement("input");
		this.team2ScoreBox.type = "number";
		this.team2ScoreBox.min = 0;
		this.team2ScoreBox.value = 0
		this.team2ScoreBox.className = "scoreBox";
		
		this._addRowToTable(tableBody, [this.team1ScoreBox], [this.team2ScoreBox], "Score");
		
		this.team1ShotsBox = document.createElement("input");
		this.team1ShotsBox.type = "number";
		this.team1ShotsBox.min = 0;
		this.team1ShotsBox.value = 0
		this.team1ShotsBox.className = "shotsBox";
		
		this.team2ShotsBox = document.createElement("input");
		this.team2ShotsBox.type = "number";
		this.team2ShotsBox.min = 0;
		this.team2ShotsBox.value = 0
		this.team2ShotsBox.className = "shotsBox";
		
		this._addRowToTable(tableBody, [this.team1ShotsBox], [this.team2ShotsBox], "SOG");
		
		this.team1PenaltyBox = document.createElement("input");
		this.team1PenaltyBox.type = "checkbox";
		this.team2PenaltyBox = document.createElement("input");
		this.team2PenaltyBox.type = "checkbox";
		this._addRowToTable(tableBody, [this.team1PenaltyBox], [this.team2PenaltyBox], "Penalty?");
		
		this.timeBox = document.createElement("input");
		this.timeBox.style.width = "100px";
		this._addRowToTable(tableBody, [this.timeBox], [], "Time");
		
		this.periodBox = document.createElement("input");
		this.periodBox.type = "number";
		this.periodBox.min = 0;
		this.periodBox.max = 5;
		this.periodBox.value = 1;
		this.periodBox.className = "shotsBox";
		this._addRowToTable(tableBody, [this.periodBox], [], "Period");
		
		this.$container.append(teamManageLink, table);
	},
	_addRowToTable: function(tableBody, leftItems, rightItems, label){
		var row = document.createElement("tr");
		if (label){
			var labelData = document.createElement("td");
			labelData.innerHTML = label;
			labelData.style.fontWeight = "bold";
			row.appendChild(labelData);
		}
		var dataLeft = document.createElement("td");
		if (leftItems && leftItems.length > 0){
			leftItems.forEach(function(item) {
				dataLeft.appendChild(item);
			});
			if (!rightItems || rightItems.length == 0){
				dataLeft.setAttribute("colspan", "2");
			}
			row.appendChild(dataLeft);
		}

		var dataRight = document.createElement("td");
		if (rightItems && rightItems.length > 0){
			rightItems.forEach(function(item){
				dataRight.appendChild(item);
			});
			if (!leftItems || leftItems.length == 0){
				dataRight.setAttribute("colspan", "2");
			}
			row.appendChild(dataRight);
		}
		
		tableBody.appendChild(row);
		return row;
	},
	initializeTeams: function(comboBox){
		$j.ajax({
			url: location.protocol + '//' + location.host + "/api/teams",
			context: this,
			dataType: "json",
			success: function(response){
				var teams = response.teams;
				teams.forEach(function(team){
					if (team != ""){
						var element = document.createElement("option");
						element.setAttribute("id", team.id);
						element.innerHTML = team.Name;
						comboBox.appendChild(element);
					}
				});
			},
			error: function (response) {
				alert("Could not fetch teams!");
			}
			
		})
	},
	updateInfo: function (game) {
		var t1id = this.getIdFromKey(game.Team1);
		var t2id = this.getIdFromKey(game.Team2);
		var t1found = false;
		var t2found = false;
		
		for (var i = 0; i < this.team1Selector.children.length; i++){
			if (this.team1Selector.children[i].id == t1id){
				this.team1Selector.selectedIndex = i;
				t1found = true;
				break;
			}
		}
		
		for (var i = 0; i < this.team2Selector.children.length; i++){
			if (this.team2Selector.children[i].id == t2id){
				this.team2Selector.selectedIndex = i;
				t2found = true;
				break;
			}
		}
		
		if (!t1found || !t2found) {
			alert("Error loading game: " + game.Name);
		}
		
		this.team1ScoreBox.value = game.T1Score;
		this.team2ScoreBox.value = game.T2Score;
		this.team1ShotsBox.value = game.T1SOG;
		this.team2ShotsBox.value = game.T2SOG;
		this.team1PenaltyBox.checked = game.T1Penalty == "true";
		this.team2PenaltyBox.checked = game.T2Penalty == "true";
		this.timeBox.value = game.Time;
		this.periodBox.value = game.Period;
		
	},
	getIdFromKey: function (key){
		return +key.substring(key.indexOf("(") + 1, key.indexOf(")"));
	},
}