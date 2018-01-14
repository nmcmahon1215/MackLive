/**
 * This is the javascript for building and operating the console window
 */

LiveBlogConsole = function (containerId) {
    this.containerId = containerId;
    this.container = document.getElementById(this.containerId);
    this.form = null;
    this.$container = $j(this.container);
    this.isValidTwitter = false
};

LiveBlogConsole.prototype = {
    render: function () {

        this.liveBlogFeed = document.getElementById("liveBlogFeed");
        this.liveBlogFeed.src = "";
        this.form = document.getElementById("liveBlogForm");
        this.textArea = document.getElementById("liveBlogInput");

        this.textArea.addEventListener("keypress", function (event) {
            if (event.keyCode == 13) {
                this.submitMessage();
                return false;
            }
        }.bind(this));

        this.textArea.addEventListener("keyup", this.validateButton.bind(this));
        this.nameField = document.getElementById("nameInput");
        this.nameField.addEventListener("keyup", this.validateButton.bind(this));
        this.submitButton = document.getElementById("liveBlogSubmitButton");
        $j(this.submitButton).click(this.submitMessage.bind(this))

        $j(this.nameField).change(function () {
            localStorage['MLusername'] = this.nameField.value;
        }.bind(this));

        if (localStorage['MLusername']) {
            this.nameField.value = localStorage['MLusername'];
        }

        this.twitterSwitch = document.getElementById("twitter-switch");
        this.charCount = document.getElementById("char-count");
        this.twitterSignIn = document.getElementById("twitter-sign-in");
        this.twitterLink = document.getElementById("twitter-options-link");
        this.twitterLinkBox = document.getElementById("twitter-link");
        this.twitterFollowInput = document.getElementById("twitter-follow-input")
        this.saveLinkButton = document.getElementById("save-link-button");
        this.twitterLimit = 140;
        $j(this.twitterSwitch).on("change", this.validateButton.bind(this));
        $j(this.textArea).on("keyup", this.updateCharCount.bind(this));
        $j(this.saveLinkButton).click(this.updateGameOptions.bind(this));

        $j.ajax({
            url: "/api/twitter/status",
            method: "GET",
            context: this,
            statusCode: {
                401: function () {
                    this.twitterSignIn.classList.remove("hide")

                },
                200: function () {
                    this.isValidTwitter = true
                    if (this.initialized) {
                        this.twitterLink.classList.remove("hide")
                    }
                }
            }
        })

        $j("#twitter-options-link").on("click", function () {
            $j("#twitterModal").modal();
        })

    },
    initialize: function (gameData) {
        this.initialized = true;
        this.validateButton();
        this.liveBlogFeed.src = "/client/liveBlogFeed.html?gameId=" + adminConsole.gameId;

        //Show twitter options if we
        if (this.isValidTwitter) {
            this.twitterLink.classList.remove("hide")
        }

        if (gameData.link) {
            if (gameData.link.trim().length > 0) {
                this.updateCounterLimit()
            }
            this.twitterLinkBox.value = gameData.link.trim();
        }
        if (gameData.twitterAccounts && gameData.twitterAccounts.length > 0) {
            this.twitterFollowInput.innerHTML = gameData.twitterAccounts.join(',')
            this.startTwitterStream()
        }
    },
    startTwitterStream: function () {
        $j.ajax({
            url: "/api/twitter/stream/" + adminConsole.gameId,
            method: "GET",
            context: this,
            success: function () {
                console.log("Successfully started twitter streaming for:" + this.twitterAccounts);
            },
            error: function () {
                console.log("Failed to start twitter streaming for:" + this.twitterAccounts);
            }
        })
    },
    submitMessage: function () {
        if (this.textArea.value.trim() == "" || this.nameField.value.trim() == "") {
            return;
        }

        var message = {
            author: this.nameField.value,
            text: this.textArea.value.trim(),
            game: adminConsole.gameId,
            twitter: this.twitterSwitch.checked,
        };

        $j.ajax({
            url: "/api/messages",
            method: "POST",
            context: this,
            contentType: "application/json",
            data: JSON.stringify(message),
            success: function (result) {
                this.textArea.value = "";
                this.textArea.classList.remove("fadeBG");

                setTimeout(function () {
                    this.textArea.classList.add("fadeBG");
                }.bind(this), 100);

                this.liveBlogFeed.contentWindow.liveFeed.fetchNewMessages();
            },
            error: function (jqXHR, textStatus, errorThrown) {
                if (jqXHR.status == 401) {
                    //Indicates a twitter problem. Reload to refresh the status of twitter
                    window.location.reload();
                } else {
                    alert(textStatus + ": " + errorThrown);
                }
            }
        })
    },
    validateButton: function () {
        if (this.textArea.value.trim() == "" || this.nameField.value.trim() == "" || !this.initialized) {
            this.submitButton.disabled = true;
        } else {
            this.submitButton.disabled = this.twitterSwitch.checked && this.textArea.value.trim().length > this.twitterLimit;
        }

        this.charCount.style.visibility = this.twitterSwitch.checked ? "visible" : "hidden"
    },
    updateCharCount: function () {
        this.charCount.innerHTML = this.twitterLimit - this.textArea.value.trim().length;

        if (this.charCount.innerHTML < 0) {
            this.charCount.style.color = "red"
            this.charCount.style.fontWeight = "bold"
        } else {
            this.charCount.style.color = "";
            this.charCount.style.fontWeight = ""
        }
    },
    updateGameOptions: function () {
        var payload = {
            link: this.twitterLinkBox.value.trim(),
            twitterAccounts: this.twitterFollowInput.value.trim()
        };

        $j.ajax({
            url: "/api/game/" + adminConsole.gameId + "/options",
            method: "POST",
            context: this,
            contentType: "application/json",
            data: JSON.stringify(payload),
            success: function (result) {
                this.textArea.value = "";
                this.textArea.classList.remove("fadeBG");

                setTimeout(function () {
                    this.textArea.classList.add("fadeBG");
                }.bind(this), 100);

                this.liveBlogFeed.contentWindow.liveFeed.fetchNewMessages();
            },
        })

        if (this.twitterLinkBox.value.trim().length > 0) {
            this.updateCounterLimit();
        } else {
            this.twitterLimit = 140;
            this.updateCharCount();
        }
    },
    updateCounterLimit: function () {
        $j.ajax({
            url: "/api/twitter/limit",
            method: "GET",
            context: this,
            success: function (result) {
                this.twitterLimit = result.limit;
                this.updateCharCount();
            },
        });
    },
};
