//
//$(document).ready(function () {
//        var json = testJson;
//        var paramsTableApp = "";
//        var stepTable = "";
//        var toggle = "onclick='toggle('step0')";
//        $('#resultHeader').append(json.timestamp + "  " +json.steps[0].timestamp);
//        $('#testNameHeadline').append(json.name);
//        $('#testDescription').append(json.description);
//        for (var j in testJson.parameters){if (testJson.parameters.hasOwnProperty(j)){
//                for (var i in testJson.parameters[j]){if (testJson.parameters[j].hasOwnProperty(i)){
//                    paramsTableApp += "<tr>\n" + "<td>" + i + "</td>\n" + "<td>" + testJson.parameters[j][i] + "</td>\n"+"</tr>\n"
//                    }
//                };
//            }
//        };
//        $('#paramBody').append(paramsTableApp);
////***********************************************************************************************************************************
//        for (var i in testJson.steps){
//            if (testJson.steps.hasOwnProperty(i)){
//                stepTable += "<br>\n" + "<table class='testtbl' >\n" + "<thead>\n" + "<tr>\n" + "<th>" +testJson.steps[i].timestamp + "</th>\n";
//                stepTable += "<th";
//                if(testJson.steps[i].status === "failure"){
//                    stepTable += " class='header fail'";
//                }
//                else if(testJson.steps[i].status === "warning"){
//                    stepTable += " class='header warning'";
//                }
//                stepTable += "><div onclick=" + String.fromCharCode(34) +"toggle('step" + i + "')" + String.fromCharCode(34) + ">";
//                stepTable += testJson.steps[i].description + "</div></th>\n" + "</tr>\n </thead>\n <tbody id='step" + i + "' >\n";
//                for (var j in testJson.steps[i].reportElements){
//                    if (testJson.steps[i].reportElements.hasOwnProperty(j)){
//                        stepTable += "<tr>\n<td>" + testJson.steps[i].reportElements[j].timestamp + "</td>\n";
//
//                        if(testJson.steps[i].reportElements[j].hasOwnProperty("type")){
//                            if(testJson.steps[i].reportElements[j].type === 'IMAGE'){
//                                stepTable += String("<td>\n<a onclick=") + String.fromCharCode(34) +"toggle('sc')" + String.fromCharCode(34) + "href='#'>";
//                                stepTable += testJson.steps[i].reportElements[j].title+"</a>\n";
//                                stepTable += "<div>\n" + new String("<img id='sc' style='display: none;' src='");
//                                stepTable +=  testJson.steps[i].reportElements[j].file + "' alt='Image' height='500' width='500'>";
//                                stepTable += "\n</div>\n</td>\n";};
//                            if(testJson.steps[i].reportElements[j].type === 'VIDEO'){
//                                stepTable += "<td>" + testJson.steps[i].reportElements[j].title + "</td>\n";
//                                //TODO: If it is a video element what do we want to do?
//                                console.log('doing something with video')};
//                            if(testJson.steps[i].reportElements[j].type === 'LOG'){
//                                stepTable += "<td>" + testJson.steps[i].reportElements[j].title + "</td>\n";
//                                //TODO: If it is a log element what do we want to do?
//                                console.log('doing something with log file')};
//                        }
//                        else if(testJson.steps[i].reportElements[j].hasOwnProperty("message")){
//                            stepTable += "<td>" + testJson.steps[i].reportElements[j].title + "</td>\n";
//                            //TODO: If it is some message we want to present to user how do we want to do it?
//                            console.log("doing something with message");
//                        }
//                        else{
//                            stepTable += "<td";
//                            if(testJson.steps[i].reportElements[j].status === "warning"){
//                                stepTable += " class='warning'";
//                            }
//                            else if(testJson.steps[i].reportElements[j].status === "failure"){
//                                stepTable += " class='fail'";
//                            }
//                            stepTable += ">" + testJson.steps[i].reportElements[j].title + "</td>\n";
//                        };
//                        stepTable += "</tr>\n";
//                    }
//                }
//                stepTable+= "</tbody>\n</table>\n<br/>\n";
//            }
//        };
//        $('#stepTables').append(stepTable);
//    }
//)

$(document).ready(function(){
        var json = testJson;
        var paramsTableApp = "";
        var stepTable = "";
        var toggle = "onclick='toggle('step0')";
        $('#resultHeader').append(json.timestamp + "  " +json.steps[0].timestamp);
        $('#testNameHeadline').append(json.name);
        $('#testDescription').append(json.description);
        paramsTableApp = setParamsTable(testJson.parameters);
        $('#paramBody').append(paramsTableApp);
        stepTable = buildStepsTable(testJson.steps);
        $('#stepTables').append(stepTable);

    }
)

function setParamsTable(parameters){
    var paramsTableApp = "";
    for (var j in parameters){
        if (parameters.hasOwnProperty(j)){
            for (var i in parameters[j]){
                if (parameters[j].hasOwnProperty(i)){
                    paramsTableApp += "<tr>\n" + "<td>" + i + "</td>\n" + "<td>" + parameters[j][i] + "</td>\n"+"</tr>\n"
                 }
            };
        }
    };
    return paramsTableApp;
}

function buildStepsTable(steps){
    var stepTable = "";
    for (var i in steps){
        if (steps.hasOwnProperty(i)){
            stepTable += "<br>\n" + "<table class='testtbl' >\n" + "<thead>\n" + "<tr>\n" + "<th>" + steps[i].timestamp + "</th>\n";
            stepTable += "<th";
            if(steps[i].status === "failure"){
                stepTable += " class='header fail'";
            }
            else if(steps[i].status === "warning"){
                stepTable += " class='header warning'";
            }
            stepTable += ">\n<div onclick=" + String.fromCharCode(34) +"toggle('step" + i + "')" + String.fromCharCode(34) + ">";
            stepTable += steps[i].description + "</div></th>\n" + "</tr>\n </thead>\n <tbody id='step" + i + "' >\n";
            stepTable += addSubSteps(steps[i].reportElements);
            stepTable+= "</tbody>\n</table>\n<br/>\n";
        }
    };
    return stepTable;
}

function addSubSteps(elements){
    var stepTable = "";
    for (var j in elements){
        if (elements.hasOwnProperty(j)){
            stepTable += "<tr>\n<td>" + elements.timestamp + "</td>\n";
            if(elements[j].hasOwnProperty("type")){
                switch(elements[j].type)
                {
                    case "IMAGE":
                        stepTable += String("<td>\n<a onclick=") + String.fromCharCode(34) +"toggle('sc')" + String.fromCharCode(34) + "href='#'>";
                        stepTable += elements[j].title+"</a>\n";
                        stepTable += "<div>\n" + new String("<img id='sc' style='display: none;' src='");
                        stepTable +=  elements[j].file + "' alt='Image' height='500' width='500'>";
                        stepTable += "\n</div>\n</td>\n";;
                        break;
                    case "LOG":
                    case "VIDEO":
                        stepTable += "<td>" + elements[j].title + "</td>\n";
                        break;
                }

            }
            else if(elements[j].hasOwnProperty("message")){
                stepTable += "<td>" + elements[j].title + "</td>\n";
                //TODO: If it is some message we want to present to user how do we want to do it?
                console.log("doing something with message");
            }
            else{
                stepTable += "<td";
                if(elements[j].status === "warning"){
                    stepTable += " class='warning'";
                }
                else if(elements[j].status === "failure"){
                    stepTable += " class='fail'";
                }
                stepTable += ">" + elements[j].title + "</td>\n";
            };
            stepTable += "</tr>\n";
        }
    }
    return stepTable;
}

function setStyle(){}
