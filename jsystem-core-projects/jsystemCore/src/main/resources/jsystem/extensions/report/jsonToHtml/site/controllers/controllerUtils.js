/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function getTestColors(){
    var colors = new Object();
    colors.success = "#88ee88";
    colors.failure = "#ff8888";
    colors.error = "#4682B4";
    colors.warning = "#ffff77";
    return colors;
}

function collectTestsFromScenario(children,tests){
    $(children).each(function(){
       switch (this.type) {
           case "test":
               tests.push(this);
               break;
           case "scenario":
               collectTestsFromScenario(this.children,tests);
               break;
       }
    });
}

function collectAllTests(){
    var tests = new Array();
    $(execution.machines).each(function(){
        var machineName = this.name;
        $(this.children).each(function() {
            var suiteName = this.name;
            var suiteTests = new Array();
            collectTestsFromScenario(this.children,suiteTests);
            $(suiteTests).each(function() {
                this.machineName = machineName;
                this.suiteName = suiteName;
            });
            tests = tests.concat(suiteTests);
        });
        
    });
    return tests;
    
}
