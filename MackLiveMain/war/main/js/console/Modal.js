/**
 * Created by Nick on 6/25/16.
 */

Modal = function () {
    this.source = "";
    this.width = 400;
    this.height = 800;
    this.container = document.getElementById("embedCode")
    $j(this.container).on("click", function () {
        this.focus();
        this.select();
    });

    this.widthBox = document.getElementById("embedWidth");
    this.heightBox = document.getElementById("embedHeight");

    this.widthBox.value = this.width;
    this.heightBox.value = this.height;

    //Event Handlers
    var self = this;

    $j(this.widthBox).on("change", function () {
        self.setWidth(this.value)
    });

    $j(this.heightBox).on("change", function () {
        self.setHeight(this.value)
    });

}

Modal.prototype = {
    updateModal: function () {
        this.container.value = "<iframe src=\"" + this.source +
            "\" height=\"" + this.height +
            "\" width=\"" + this.width +
            "\" frameborder=\"0\" />";
    },
    setSource: function (source) {
        this.source = source;
        this.updateModal();
    },
    setHeight: function (height) {
        this.height = height;
        this.updateModal();
    },
    setWidth: function (width) {
        this.width = width;
        this.updateModal();
    }
}