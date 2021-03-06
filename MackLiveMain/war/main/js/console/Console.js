/**
 * This JS controls functions for the Console as a whole
 */

AdminConsole = function () {
    this.gameName = null;
    this.titleElement = document.getElementById("gameTitle");
    this.gamePicker = document.getElementById("gamePicker");
    this.loadButton = document.getElementById("loadButton");
    this.createButton = document.getElementById("createButton");
    this.initialize();
    this.modal = new Modal();
};

AdminConsole.prototype = {
    initialize: function () {
        this.titleElement.innerHTML = "No game selected.";
        this.titleElement.style.backgroundColor = "red";
        this.loadButton.disabled = true;
        this.loadRecentGames();

        $j(this.gamePicker).on("change", function (event) {
            if (this.gamePicker.selectedIndex != 0) {
                this.loadButton.disabled = false;
            } else {
                this.loadButton.disabled = true;
            }
        }.bind(this));

        $j(this.createButton).click(this.createGame.bind(this));
        $j(this.loadButton).click(this.loadGame.bind(this));
    },

    setGameName: function (name) {
        $j(this.titleElement).slideUp(500, function () {
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
                this.idMessage.style.textDecoration = "underline";
                this.idMessage.style.cursor = "pointer";
                this.loadButton.parentElement.appendChild(this.idMessage);
            }
            this.idMessage.innerHTML = "Game id: " + this.gameId + " <i class='fa fa-code' aria-hidden='true'></i>";
            $j(this.idMessage).click(function () {
                $j('#embedModal').modal();
            });
        }
    },
    loadRecentGames: function () {
        $j.ajax({
            url: location.protocol + '//' + location.host
            + "/api/game/recent/5",
            dataType: "json",
            context: this,
            success: function (response) {
                var games = response;
                this.gamePicker.innerHTML = "<option>Select A Game...</option>";
                if (!games) {
                    return;
                }
                games.forEach(function (game) {
                    if (game != "") {
                        this.gamePicker.innerHTML += "<option id="
                            + game.key.id + ">" + game.name
                            + "</option>";
                    }
                });
            },
            error: function (response, errorType, errorMessage) {
                alert("Could not fetch recent games.");
            }
        })
    },
    loadGame: function () {
        var game = this.gamePicker.children[this.gamePicker.selectedIndex];
        this.loadGameById(game.id)
    },
    loadGameById: function (id) {
        $j.ajax({
            url: location.protocol + '//' + location.host + "/api/game/" + id,
            method: "GET",
            dataType: "json",
            context: this,
            success: function (response) {
                this.initializeGame(response.name, response.key.id, response);
            },
            error: function (response, errorType, errorStuff) {
                alert("Error loading game!");
            },
        });
    },
    createGame: function () {
        var today = new Date();
        var team1select = document.getElementById("team1select");
        var team2select = document.getElementById("team2select");

        var team1 = team1select.children[team1select.selectedIndex];
        var team2 = team2select.children[team2select.selectedIndex];

        $j.ajax({
            url: location.protocol + '//' + location.host + "/api/game",
            method: "POST",
            contentType: "text/plain",
            dataType: "json",
            data: team1.id + "," + team2.id,
            context: this,
            timeout: 2000,
            success: function (response) {
                this.initializeGame(response.name, response.key.id, response);
            },
            error: function (response, errorType, errorThrown) {
                alert("Failed to create new game.");
            },
        })
    },
    initializeGame: function (name, id, gameData) {
        this.gameId = id;
        this.setGameName(name);
        scoreBoard.loadTeams(gameData);
        scoreBoard.loadScoreInfo(gameData);
        liveConsole.initialize(gameData);
        this.modal.setSource(location.protocol + "//" + location.host + "/client/clientApp.html?gameId=" + id);
        commentFeed.initialize();
        localStorage['gameid'] = this.gameId;
    },
};
