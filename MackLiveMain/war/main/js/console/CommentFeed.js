/**
 * Creates a feed window for display blog posts and comments
 */
CommentFeed = function (container) {
    this.container = document.getElementById(container);
    this.latestMessageDate = "0";
    this.initialized = false;
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

        panel.id = "message_" + message.key.id;
        panel.className = "panel panel-warning";
        header.className = "panel-heading";

        header.innerHTML = message.author + "<span class=\"panel-right\">" + new Date(+message.time).toLocaleTimeString() + "</span>";
        content.innerHTML = message.text;

        var approveButton = document.createElement("input");
        approveButton.type = "button";
        approveButton.value = "Approve"
        approveButton.className = "floatR btn btn-success btn-sm commentButton bottomLeft";

        var deleteButton = document.createElement("input");
        deleteButton.type = "button";
        deleteButton.value = "Delete";
        deleteButton.className = "floatR btn btn-danger btn-sm commentButton bottomRight";

        panel.appendChild(header);
        panel.appendChild(content);

        var modalPanel = panel.cloneNode(true);

        content.style.paddingBottom = '30px';
        content.appendChild(deleteButton);
        content.appendChild(approveButton);

        $j(deleteButton).on("click", function () {
            $j("#confirmModalContent").html("");
            $j("#confirmModalContent").append(modalPanel);
            $j("#confirmModalYes").one("click", function () {
                this.deleteMessage(message.key.id);
            }.bind(this));
            $j("#confirmModal").modal();
        }.bind(this));

        $j(approveButton).on("click", function () {
            this.approveMessage(message.key.id);
        }.bind(this));

        panel.style.display = "none";
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
    deleteMessage: function (messageId) {
        $j.ajax({
            url: location.protocol + '//' + location.host + "/api/messages/delete/" + messageId,
            context: this,
            success: function () {
                $j("#message_" + messageId).slideUp(700, function () {
                    $j(this).remove();
                });
            },
            error: function () {
                console.error("Could not delete message " + messageId);
            }
        });
    },
    approveMessage: function (messageId) {
        $j.ajax({
            url: location.protocol + '//' + location.host + "/api/messages/approve/" + messageId,
            context: this,
            success: function () {
                $j("#message_" + messageId).slideUp(700, function () {
                    $j(this).remove();
                });
            },
            error: function () {
                console.error("Could not approve message " + messageId);
            }
        });
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
                    this.latestMessageDate = result.latestTime;
                }
                this.initialized = true;
            }.bind(this),
            error: function (result, error, desc) {
                console.error("Error: " + desc);
            }

        });
    },
    fetchNewMessages: function () {
        $j.ajax({
            url: location.protocol + "//" + location.host + "/api/messages/pending/" + adminConsole.gameId + "/" + this.latestMessageDate,
            success: function (result) {
                var messages = result.messages;
                messages.forEach(function (message) {
                    this.addMessage(message);
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
