@ReplaceLine {
    var p = input.trim();
    if(p.startsWith("while")){
        p = p.substring(5);
        return "until not " + p;
    }else{
        return input;
    }
}
@ReplaceLine {
    var p = input.trim();
    if(p.startsWith("each")){
        p = p.substring(4);
        p = p.replace(":", "IN");
        return "FOR " + p;
    }else{
        return input;
    }
}
@ReplaceLine {
    var p = input.trim();
    if(p.startsWith("function")){
        if(p.charAt(p.length-1) == '{'){
            p = p.substring(0, p.length-1);
        }
        var oline = p;
        p = p.substring(p.indexOf('(')+1);
        p = p.substring(0, p.lastIndexOf(')'));
        var bits = p.split(" ");
        var out = [];
        for(var i = 0; i < bits.length; i++){
            var t = bits[i].split(",");
            t.forEach(function(v){
                if(v.length > 0){
                    out.push(v);
                }
            });
        }
        oline = oline.substring(8).trim();
        oline = oline.substring(0,oline.indexOf('('));
        var result = "function " + oline + " {\n";
        out.forEach(function(v){
            result = result + "parameter " + v + ".\n";
        });
        return result;
    }else{
        return input;
    }
}
@ReplaceRegex /([\s\)\w])===?([\s\w])/ $1=$2
@ReplaceRegex /([\s\)])\|\|(\s)/ $1OR$2
@ReplaceRegex /([\s\)])&&(\s)/ $1AND$2
@ReplaceRegex /^(\s*)([_a-z0-9A-Z]+)\s*\+\+\s*;?\s*$/ $1set $2 to $2 + 1.
@ReplaceRegex /^(\s*)([_a-z0-9A-Z]+)\s*\+=\s*(\-?\d+(\.\d+)?)\s*;?\s*$/ $1set $2 to $2 + $3.
@ReplaceRegex /^(\s*)([_a-z0-9A-Z]+)\s*--\s*;?\s*$/ $1set $2 to $2 - 1.
@ReplaceRegex /^(\s*)([_a-z0-9A-Z]+)\s*\-=\s*(\-?\d+(\.\d+)?)\s*;?\s*$/ $1set $2 to $2 - $3.
@ReplaceRegex /^(\s*)([_a-z0-9A-Z]+)\s*\*=\s*(\-?\d+(\.\d+)?)\s*;?\s*$/ $1set $2 to $2 * $3.
@ReplaceRegex /^(\s*)([_a-z0-9A-Z]+)\s*\\=\s*(\-?\d+(\.\d+)?)\s*;?\s*$/ $1set $2 to $2 \ $3.
@ReplaceRegex /^(\s*)([_a-z0-9A-Z]+)\s*&=\s*(\-?\d+(\.\d+)?)\s*;?\s*$/ $1set $2 to $2 and $3.
@ReplaceRegex /^(\s*)([_a-z0-9A-Z]+)\s*%=\s*(\-?\d+(\.\d+)?)\s*;?\s*$/ $1set $2 to $2 % $3.
@ReplaceRegex /^(\s*)([_a-z0-9A-Z]+)\s*\^=\s*(\-?\d+(\.\d+)?)\s*;?\s*$/ $1set $2 to $2 ^ $3.
@ReplaceRegex /^(\s*)([_a-z0-9A-Z]+)\s*\|=\s*(\-?\d+(\.\d+)?)\s*;?\s*$/ $1set $2 to $2 or $3.
@ReplaceRegex /^(\s*)var\s+([_a-z0-9A-Z]+)\s*=\s*(.+?)\s*;?\s*$/ $1local $2 is $3.
@ReplaceRegex /^(\s*)([_a-z0-9A-Z]+)\s*=\s*(.+?)\s*;?\s*$/ $1set $2 to $3.
@ReplaceLine {
    return input.replace(/;$/g, ".");
}
@ReplaceLine {
    return input.replace(/\}$/g, "}.");
}
@ReplaceLine {
    if(input.trim().startsWith("@RAW")){
        return input.trim().substring(4);
    }else{
        return input;
    }
}
var KSP_LIB_VERSION = 1;
