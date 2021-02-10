var filesJSON = {};
var complementsJSON = {}

updateTypedocJSON = (event) => {
    const idEvent = event.id;
    const isComp = idEvent.includes("comp");
    if (isComp) {
        $("#typedoc-complements-button").attr("disabled", false);
        const pkDemandesFiles = idEvent.replace("comp", "");
        complementsJSON[pkDemandesFiles] = event.value;
    } else {
        $("#typedoc-files-button").attr("disabled", false);
        const pkDemandesFiles = idEvent.replace("file", "");
        filesJSON[pkDemandesFiles] = event.value;
    }
}

$("#typedoc-files-button").click(function (e) {
    execEnregistrerFichiers();
    e.preventDefault();
    return false;
});

$("#typedoc-complements-button").click(function (e) {
    execEnregistrerFichiers();
    e.preventDefault();
    return false;
});

var execEnregistrerFichiers = function() {
    $("#typedoc-complements").prop('value', JSON.stringify(complementsJSON));
    $("#typedoc-files").prop('value', JSON.stringify(filesJSON));
    $("#confirmer-typedoc-button").click();
};