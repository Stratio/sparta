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

    output = output + ' (' + actual + ')';

    return output;
}
