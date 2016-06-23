/**
 * Creates the elements for controlling the scoreboard from the console
 */

ScoreBoardConsole = function(containerId) {
	this.containerId = containerId;
	this.container = document.getElementById(containerId);
	this.$container = $j(this.container);
	this.teams = null;
};

ScoreBoardConsole.prototype = {
	render: function() {
		var teamManageLink = document.getElementById("teamManageLink");
		teamManageLink.onclick = function() {
			window.open('teamManager.jsp', 'newwindow', 'width=650px, height=350px');
		};

		this.team1Selector = document.getElementById("team1select");
		$j(this.team1Selector).on("change", this.updateTeam);

		this.team2Selector = document.getElementById("team2select");
		$j(this.team2Selector).on("change", this.updateTeam);
		
		this.initializeTeams();

		this.team1ScoreBox = document.getElementById("team1scorebox");
		this.team2ScoreBox = document.getElementById("team2scorebox");
		this.team1ShotsBox = document.getElementById("team1shotsbox");
		this.team2ShotsBox = document.getElementById("team2shotsbox");
		this.team1PowerPlay = document.getElementById("team1pp");
		this.team2PowerPlay = document.getElementById("team2pp");
		this.timeBox = document.getElementById("timeControl");
		this.periodBox = document.getElementById("periodControl");
	},
	initializeTeams: function(){
		$j.ajax({
			url: location.protocol + '//' + location.host + "/api/teams",
			context: this,
			dataType: "json",
			success: function(response){
				
				this.teams = response;
				
				$j(this.team1Selector).on("change", function() {
					var t1Index = this.team1Selector.selectedIndex;
					var t2Index = this.team2Selector.selectedIndex;
					
					this.team2Selector.innerHTML = "";
					
					for (var i = 0; i < this.teams.length; i++){
						if (i != this.team1Selector.selectedIndex){
							var element = document.createElement("option");
							element.setAttribute("id", this.teams[i].key.id);
							element.innerHTML = this.teams[i].name;
							this.team2Selector.appendChild(element);
						}
					}
					var index;
					
					if (t1Index < t2Index){
						index = t2Index - 1;
					} else {
						index = Math.max(t2Index, 0);
					}
					
					this.team2Selector.selectedIndex = index;
					
				}.bind(this));
				
				response.forEach(function(team){
					if (team != ""){
						var element = document.createElement("option");
						element.setAttribute("id", team.key.id);
						element.innerHTML = team.name;
						this.team1Selector.appendChild(element);
					}
				}.bind(this));
				
				$j(this.team1Selector).trigger("change");
			},
			error: function (response) {
				alert("Could not fetch teams!");
			}
			
		})
	},
	updateInfo: function (game) {
		var t1id = game.team1.key.id;
		var t2id = game.team2.key.id;
		var t1found = false;
		var t2found = false;
		
		for (var i = 0; i < this.team1Selector.children.length; i++){
			if (this.team1Selector.children[i].id == t1id){
				this.team1Selector.selectedIndex = i;
				t1found = true;
				break;
			}
		}

		$j(this.team1Selector).trigger("change");
		
		for (var i = 0; i < this.team2Selector.children.length; i++){
			if (this.team2Selector.children[i].id == t2id){
				this.team2Selector.selectedIndex = i;
				t2found = true;
				break;
			}
		}
		
		if (!t1found || !t2found) {
			alert("Error loading game: " + game.name);
		}
		
		this.team1ScoreBox.value = game.team1goals;
		this.team2ScoreBox.value = game.team2goals;
		this.team1ShotsBox.value = game.team1sog;
		this.team2ShotsBox.value = game.team2sog;
		this.team1PowerPlay.checked = game.team1penalty;
		this.team2PowerPlay.checked = game.team2penalty;
		this.timeBox.value = game.time;
		this.periodBox.value = game.period;
		
	},
	updateTeam: function(){
		if (!adminConsole || !adminConsole.gameId){
			return;
		}
		
		var teamNum;
		if (this.id == "team1select"){
			teamNum = 1;
		} else if (this.id == "team2select"){
			teamNum = 2;
		} else {
			return;
		}
		
		var team = this.children[this.selectedIndex];

		$j.ajax({
			url : location.protocol + '//' + location.host + "/api/game/" + adminConsole.gameId + "/" + teamNum,
			method : "POST",
			contentType : "text/plain",
			dataType : "json",
			data : team.id,
			context : this,
			timeout : 2000,
			success : function(response) {
				adminConsole.gameId = response.key.id;
				adminConsole.setGameName(response.name);
			},
			error : function(response, errorType, errorThrown) {
				alert("Failed to update Game.");
			},
		})
	}
};
