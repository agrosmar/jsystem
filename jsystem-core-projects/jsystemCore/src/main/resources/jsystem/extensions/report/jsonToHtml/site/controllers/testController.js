/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var idIndex = 0;

function setFixedProperties(element) {
    $(element).find("#name").text(test.name);
    $(element).find("#timestamp").text(test.timestamp);
    $(element).find("#description").text(test.description);

}

function addPropertiesToTbl(properties, table) {
    for (var key in properties) {
        var tr = $('<tr>');
        tr.append($('<td>').text(key));
        tr.append($('<td>').text(properties[key]));
        $(table).append(tr);
    }
}

function setCustomProperties(element) {
    addPropertiesToTbl(test.properties, $(element).find("#propTbl > tbody"));
}

function setParameters(element) {
    addPropertiesToTbl(test.parameters, $(element).find("#paramTbl > tbody"));

}

function createDetailsTable() {
    var table = $("<table>");
    $(table).addClass("detailsTbl").append("<tbody>");
    return table;

}

function addToggleElement(table, toggle, toggled, startAsOpened) {
    var id = "toggled_" + idIndex++;
    $(toggle).click(function() {
        doToggle(id);
    });
    $(toggle).addClass("toggle");
    $(toggled).attr("id", id).find("td").attr("colspan", "2");
    if (startAsOpened) {
        toggled.show();
    } else {
        toggled.hide();
    }
    $(table).append(toggle);
    $(table).append(toggled);



}

function isPropertyExist(element, property) {
    return (element.hasOwnProperty(property) && element[property] !== null && element[property] !== "");
}

function addStatusAsClass(elementToAppend, elementWithStatus) {
    if (isPropertyExist(elementWithStatus, "status")) {
        elementToAppend.addClass(elementWithStatus.status);
    }
}

function setRegularElement(table, element) {
    var tr = $("<tr>");
    tr.append($('<td>').text(element.time));
    if (isPropertyExist(element, "message")) {
        tr.append($('<td>').text(element.title));
        addStatusAsClass(tr, element);
        var messageTr = $("<tr>");
        messageTr.append($('<td>').text(element.message));
        addToggleElement(table, tr, messageTr, false);

    } else {
        tr.append($('<td>').text(element.title));
        addStatusAsClass(tr, element);
        $(table).append(tr);
    }
}

function setStepElement(table, element) {
    var tr = $("<tr>");
    tr.append($('<td>').text(element.time));
    tr.append($('<td>').text(element.title));
    tr.addClass("step");
    addStatusAsClass(tr, element);
    $(table).append(tr);

}

function setImageElement(table,element){
    var tr = $("<tr>");
    tr.append($('<td>').text(element.time));
    var img = $("<img>").attr("src",element.message).addClass("example-image").attr("alt",element.title);
    var a = $("<a>").attr("href",element.message).attr("data-lightbox","image-1").attr("title",element.title);
    a.append(img);
    var td = $("<td>");
    td.append(a);
    tr.append(td);
    $(table).append(tr);
}

function setLinkElement(table, element) {
    var tr = $("<tr>");
    tr.append($('<td>').text(element.time));
    if (isPropertyExist(element, "message")) {
        tr.append($('<td>').append($('<a>').text(element.title).attr("href", element.message)));
    } else {
        tr.append($('<td>').text(element.title));
    }
    $(table).append(tr);
}

function setReportElements(table, reportElements) {
    $(reportElements).each(function() {
        switch (this.type) {
            case "startLevel":
                setRegularElement(table, this);
                //TODO: Implement
                break;
            case "stopLevel":
                //TODO: Implement
                break;
            case "lnk":
                setLinkElement(table, this);
                break;
            case "step":
                setStepElement(table, this);
                break;
            case "img":
                setImageElement(table,this);
                break;

            default:
                setRegularElement(table, this);
                break;
        }

    });
}

function testController(element) {
    setFixedProperties(element);
    setCustomProperties(element);
    setParameters(element);
    setReportElements($(element).find(".detailsTbl:first"), test.reportElements);

}

function doToggle(id) {
    $("#" + id).toggle(200);

}

