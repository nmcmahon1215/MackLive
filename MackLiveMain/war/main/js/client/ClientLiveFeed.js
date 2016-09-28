/**
 * This is the javascript for building and operating the console window
 */

ClientLiveFeed = function (gameId) {
    this.gameId = gameId;
    this.form = null;
};

ClientLiveFeed.prototype = {
    render: function () {
        this.form = document.getElementById("commentForm");
        this.textArea = document.getElementById("commentInput");

        this.textArea.addEventListener("keypress", function (event) {
            if (event.keyCode == 13) {
                this.submitMessage();
                return false;
            }
        }.bind(this));

        this.textArea.addEventListener("keyup", this.validateButton.bind(this));
        this.nameField = document.getElementById("nameInput");
        this.nameField.addEventListener("keyup", this.validateButton.bind(this));
        this.submitButton = document.getElementById("commentSubmitButton");
        $j(this.submitButton).click(this.submitMessage.bind(this))

        //Remember username on refresh
        $j(this.nameField).change(function () {
            localStorage['MLusername'] = this.nameField.value;
        }.bind(this));

        if (localStorage['MLusername']) {
            this.nameField.value = localStorage['MLusername'];
        }

    },
    initialize: function () {
        this.initialized = true;
        this.validateButton();
    },
    submitMessage: function () {
        if (this.textArea.value.trim() == "" || this.nameField.value.trim() == "") {
            return;
        }

        var message = {
            author: this.nameField.value,
            text: this.textArea.value,
            game: this.gameId,
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

            },
            error: function (result) {
                alert("Could not post message!");
            }
        })
    },
    validateButton: function () {
        if (this.textArea.value.trim() == "" || this.nameField.value.trim() == "" || !this.initialized) {
            this.submitButton.disabled = true;
        } else {
            this.submitButton.disabled = false;
        }
    }
};
