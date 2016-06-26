/**
 * Creates a feed window for display blog posts and comments
 */
CommentFeed = function (container) {
    this.container = document.getElementById(container);
    this.$container = $j(this.container);
    this.latestMessageDate = new Date(0);
    this.latestGameDate = new Date(0);
};

CommentFeed.prototype = {
    initialize: function () {
        this.container.innerHTML = "";
        this.fetchAllMessages();
        if (this.interval) {
            clearInterval(this.interval);
        }
        this.interval = setInterval(this.fetchNewMessages.bind(this), 5000);
    },
    addMessage: function (message) {
        var panel = document.createElement("div");
        var header = document.createElement("div");
        var content = document.createElement("div");

        panel.className = "panel panel-warning";
        header.className = "panel-heading";
        content.className = "panel-body";

        header.innerHTML = message.author;
        content.innerHTML = message.text;

        panel.appendChild(header);
        panel.appendChild(content);

        this.container.appendChild(panel);
    },
    fetchAllMessages: function () {
        $j.ajax({
            url: location.protocol + '//' + location.host + "/api/messages/pending/" + adminConsole.gameId,
            success: function (result) {
                var messages = result.messages;
                messages.forEach(function (message) {
                    this.addMessage(message);
                }.bind(this));

                if (result.latestTime) {
                    this.lastMessageDate = new Date(result.latestTime);
                }

                $j(this.container).animate({
                    scrollTop: this.container.scrollHeight - this.container.offsetHeight,
                });

            }.bind(this),
            error: function (result, error, desc) {
                console.error("Error: " + desc);
            }

        });
    },
    fetchNewMessages: function () {
        $j.ajax({
            url: location.protocol + "//" + location.host + "/api/messages/pending/" + adminConsole.gameId + "/" + this.lastMessageDate.getTime(),
            success: function (result) {
                var messages = result.messages;
                messages.forEach(function (message) {
                    this.addMessage(message);
                }.bind(this));

                if (result.latestTime) {
                    this.lastMessageDate = new Date(result.latestTime);
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
