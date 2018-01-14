/**
 * Creates a feed window for display blog posts and comments
 */
LiveBlogFeed = function (container, blogId) {
    this.blogId = blogId;
    this.container = container;
    this.$container = $j(this.container);
    this.latestMessageDate = "0";
    this.latestGameDate = "0";
    this.initialized = false;
};

LiveBlogFeed.prototype = {
    render: function () {
        this.team1 = document.getElementById("t1");
        this.team2 = document.getElementById("t2");
        this.logo1 = document.getElementById("t1logo");
        this.logo2 = document.getElementById("t2logo");
        this.score1 = document.getElementById("score1");
        this.score2 = document.getElementById("score2");
        this.shots1 = document.getElementById("shots1");
        this.shots2 = document.getElementById("shots2");
        this.t1pp = document.getElementById("t1pp");
        this.t2pp = document.getElementById("t2pp");
        this.timer = document.getElementById("time");
        this.period = document.getElementById("period");

        this.container.className = "consoleScrollerFeed";
    },
    initialize: function () {
        this.container.innerHTML = "";
        this.fetchAllMessages();
        setInterval(this.fetchNewMessages.bind(this), 5000);
        setInterval(this.refreshScore.bind(this), 10000);
        this.initScoreboard();
    },
    initScoreboard: function () {
        $j.ajax({
            url: location.protocol + '//' + location.host + "/api/game/" + this.blogId,
            success: function (response) {
                this.logo1.src = "/api/teams/image/" + response.team1.key.id;
                this.logo2.src = "/api/teams/image/" + response.team2.key.id;

                this.team1.innerHTML = response.team1.name;
                $j(this.team1).boxfit({multiline: true});
                this.team2.innerHTML = response.team2.name;
                $j(this.team2).boxfit({multiline: true});


                this.updateScore(response);

            }.bind(this),
            error: function () {
                console.error("Failed to get scoreboard information.");
            }
        })
    },
    refreshScore: function () {
        $j.ajax({
            url: location.protocol + '//' + location.host + "/api/game/" + this.blogId + "/" + this.latestGameDate,
            context: this,
            success: this.updateScore,
            error: function () {
                console.error("Failed to refresh scoreboard information.");
            }
        })
    },
    updateScore: function (gameObject) {
        if (gameObject && !$j.isEmptyObject(gameObject)) {
            this.score1.innerHTML = gameObject.team1goals;
            this.score2.innerHTML = gameObject.team2goals;

            this.shots1.innerHTML = "Shots: " + gameObject.team1sog;
            this.shots2.innerHTML = "Shots: " + gameObject.team2sog;

            gameObject.team1pp ? $j(this.t1pp).slideDown() : $j(this.t1pp).slideUp();
            gameObject.team2pp ? $j(this.t2pp).slideDown() : $j(this.t2pp).slideUp();

            this.timer.innerHTML = gameObject.time;
            this.period.innerHTML = "Per " + gameObject.period;

            this.latestGameDate = gameObject.lastUpdated;
        }
    },
    addMessage: function (message) {
        if (message.hidden) {
            return;
        }

        var panel = document.createElement("div");
        var header = document.createElement("div");
        var content = document.createElement("div");

        if (message.isUserComment) {
            panel.className = "panel panel-warning";
        } else if (message.tweetId) {
            panel.className = 'panel panel-info'
        } else {
            panel.className = "panel panel-primary";
        }

        header.className = "panel-heading";
        content.className = "panel-body";

        var author = message.author;
        if (message.tweetId) {
            author = "<i class=\"fa fa-twitter twitter-icon\" aria-hidden=\"true\"></i>" + '@' + message.author;
            panel.id = message.tweetId;
        }

        header.innerHTML = author + "<span class=\"panel-right\">" + new Date(+message.time).toLocaleTimeString() + "</span>";
        content.innerHTML = message.text;

        panel.style.display = "none";
        if (message.tweetId) {
            header.onclick = function () {
                window.open("https://twitter.com/" + message.author + "/status/" + message.tweetId, "'_blank");
            }
            header.classList.add("div-link")
        }
        panel.appendChild(header);
        panel.appendChild(content);
        this.container.appendChild(panel);

        if (this.initialized) {
            $j(panel).slideDown(700, function () {
                $j(this.container).animate({
                    scrollTop: this.container.scrollHeight - this.container.offsetHeight,
                });
            }.bind(this));
        } else {
            panel.style.display = "";
        }

    },
    fetchAllMessages: function () {
        $j.ajax({
            url: location.protocol + '//' + location.host + "/api/messages/" + this.blogId,
            success: function (result) {
                var messages = result.messages;
                var lastTweets = {};

                messages.forEach(function (message) {
                    this.processMessage(message, lastTweets);
                }.bind(this));

                this.twitterData = lastTweets;

                if (result.latestTime) {
                    this.latestMessageDate = result.latestTime;
                }

                $j(this.container).animate({
                    scrollTop: this.container.scrollHeight - this.container.offsetHeight,
                });
                this.initialized = true;
            }.bind(this),
            error: function (result, error, desc) {
                console.error("Error: " + desc);
            }

        });
    },
    processMessage: function (message, twitterData) {
        this.addMessage(message);
        if (message.tweetId && twitterData) {
            twitterData[message.author] = message.tweetId;
        }
    },
    fetchNewMessages: function () {
        $j.ajax({
            url: location.protocol + "//" + location.host + "/api/messages/" + this.blogId + "/" + this.latestMessageDate,
            success: function (result) {
                var messages = result.messages;
                messages.forEach(function (message) {
                    this.processMessage(message);
                }.bind(this));

                if (result.latestTime) {
                    this.latestMessageDate = result.latestTime;
                }

                if (messages.length > 0) {
                    $j(this.container).animate({
                        scrollTop: this.container.scrollHeight - this.container.offsetHeight,
                    });
                }

            }.bind(this),
            error: function (result, error, desc) {
                console.error("Error fetching new messages");
            },
        })
    },
};
