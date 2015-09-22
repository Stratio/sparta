/*  Autoincrement name
/*  Insert an autoincremenetal number at the end
/*  of a name when duplicating a file */

function autoIncrementName(input) {
    var output = "";
    var actual = 2;
    var pattern = input.match(/\ \(\d+\)$/);

    if (pattern) {
        output = input.substring(0, pattern.index);
        actual = parseInt(pattern[0].substring(2, pattern[0].length-1)) + 1;
    } else {
        output = input;
    }

    output = output + '(' + actual + ')';

    return output;
};

function getFragmentsNames(fragmentsList) {
    var fragmentNames = [];
    for (var i=0; i<fragmentsList.length; i++){
        var lowerCaseName = fragmentsList[i].name.toLowerCase();
        var fragment = {'name': lowerCaseName}
        fragmentNames.push(fragment);
    }
    return fragmentNames;
};

function getPolicyNames(policiesData) {
    var policies = [];

    for (var i=0; i<policiesData.length; i++){
        policies.push(policiesData[i].name);
    }

    return policies;
};