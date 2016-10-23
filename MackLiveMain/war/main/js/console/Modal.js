/**
 * Created by Nick on 6/25/16.
 */

Modal = function () {
    this.source = "";
    this.width = localStorage['preferredWidth'] || 400;
    this.height = localStorage['preferredHeight'] || 800;
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
        this.container.value = "<div><iframe src=\"" + this.source +
            "\" height=\"" + this.height +
            "\" width=\"" + this.width +
            "\" frameborder=\"0\" /></div>";
    },
    setSource: function (source) {
        this.source = source;
        this.updateModal();
    },
    setHeight: function (height) {
        this.height = height;
        localStorage['preferredHeight'] = this.height;
        this.updateModal();
    },
    setWidth: function (width) {
        this.width = width;
        localStorage['preferredWidth'] = this.width;
        this.updateModal();
    }
}
