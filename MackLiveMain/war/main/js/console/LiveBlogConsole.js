/**
 * This is the javascript for building and operating the console window
 */

LiveBlogConsole = function (containerId) {
    this.containerId = containerId;
    this.container = document.getElementById(this.containerId);
    this.form = null;
    this.$container = $j(this.container);
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
        this.twitterLimit = 140;
        $j(this.twitterSwitch).on("change", this.validateButton.bind(this));
        $j(this.textArea).on("keyup", this.updateCharCount.bind(this));

        $j.ajax({
            url: "/api/twitter/status",
            method: "GET",
            context: this,
            statusCode: {
                401: function () {
                    this.twitterSignIn.classList.remove("hide")
                },
                200: function () {
                    document.getElementById("twitter-switch-wrap").classList.remove("hide")
                }
            }
        })

    },
    initialize: function () {
        this.initialized = true;
        this.validateButton();
        this.liveBlogFeed.src = "/client/liveBlogFeed.html?gameId=" + adminConsole.gameId;
    },
    submitMessage: function () {
        if (this.textArea.value.trim() == "" || this.nameField.value.trim() == "") {
            return;
        }

        var message = {
            author: this.nameField.value,
            text: this.textArea.value,
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
            this.submitButton.disabled = this.twitterSwitch.checked && this.textArea.value.length > this.twitterLimit;
        }

        this.charCount.style.visibility = this.twitterSwitch.checked ? "visible" : "hidden"
    },
    updateCharCount: function () {
        this.charCount.innerHTML = this.twitterLimit - this.textArea.value.length;

        if (this.charCount.innerHTML < 0) {
            this.charCount.style.color = "red"
            this.charCount.style.fontWeight = "bold"
        } else {
            this.charCount.style.color = "";
            this.charCount.style.fontWeight = ""
        }
    }
};
