/**
 * Created by Nick on 6/25/16.
 */

Modal = function () {
    this.source = "";
    this.width = localStorage['preferredWidth'] || 100;
    this.height = localStorage['preferredHeight'] || 800;
    this.container = document.getElementById("embedCode")
    $j(this.container).on("click", function () {
        this.focus();
        this.select();
    });

    this.widthBox = document.getElementById("embedWidth");
    this.heightBox = document.getElementById("embedHeight");
    this.widthUnitBox = document.getElementById("embedWidthUnit");
    this.heightUnitBox = document.getElementById("embedHeightUnit");

    this.widthBox.value = this.width;
    this.heightBox.value = this.height;

    this.heightUnit = localStorage['preferredHeightUnit'] || "px";
    this.widthUnit = localStorage['preferredWidthUnit'] || "%";

    this.heightUnitBox.value = this.heightUnit;
    this.widthUnitBox.value = this.widthUnit;

    //Event Handlers
    var self = this;

    var heightChange = function () {
        self.setHeight(this.heightBox.value, this.heightUnitBox.value);
    }.bind(this);

    var widthChange = function () {
        self.setWidth(this.widthBox.value, this.widthUnitBox.value);
    }.bind(this);

    $j(this.widthBox).on("change", widthChange);
    $j(this.widthUnitBox).on("change", widthChange);
    $j(this.heightBox).on("change", heightChange);
    $j(this.heightUnitBox).on("change", heightChange);
}

Modal.prototype = {
    updateModal: function () {
        this.container.value = "<div style='position:relative; width:" + this.width + this.widthUnit + "; " +
            "height:" + this.height + this.heightUnit + "'>" +
            "<iframe style='position: absolute; top: 0; left: 0; height: 100%; width: 100%' src=\"" +
            this.source + "\" frameborder=\"0\">" +
            "</iframe></div>";
    },
    setSource: function (source) {
        this.source = source;
        this.updateModal();
    },
    setHeight: function (height, unit) {
        this.height = height;
        this.heightUnit = unit;
        localStorage['preferredHeight'] = this.height;
        localStorage['preferredHeightUnit'] = this.heightUnit;
        this.updateModal();
    },
    setWidth: function (width, unit) {
        this.width = width;
        this.widthUnit = unit;
        localStorage['preferredWidth'] = this.width;
        localStorage['preferredWidthUnit'] = this.widthUnit;
        this.updateModal();
    }
}
