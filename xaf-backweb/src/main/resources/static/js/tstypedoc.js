var filesJSON = {};
var complementsJSON = {}

updateTypedocJSON = (event) => {
    const idEvent = event.id;
    const isComp = idEvent.includes("comp");
    if (isComp) {
        const pkDemandesFiles = idEvent.replace("comp", "");
        complementsJSON[pkDemandesFiles] = event.value;
    } else {
        const pkDemandesFiles = idEvent.replace("file", "");
        filesJSON[pkDemandesFiles] = event.value;
    }
    console.log(complementsJSON);
    console.log(filesJSON);
}

$("#typedoc-button").click(function (e) {
    console.log(complementsJSON);
    console.log(filesJSON);
    $("#typedoc-complements").prop('value', JSON.stringify(complementsJSON));
    $("#typedoc-files").prop('value', JSON.stringify(filesJSON));
    $("#confirmer-typedoc-button").click();
    e.preventDefault();
    return false;
});