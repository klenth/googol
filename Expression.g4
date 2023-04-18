grammar Expression;

goal
    : expr EOF
    ;

expr
    : ID                                                                            #Variable
    | NUMBER                                                                        #Literal
    | '(' expr ')'                                                                  #Parenthetical
    | ID '[' arguments ']'                                                          #FunctionCall
    | <assoc=right> expr op=('^' | '**') expr                                       #Power
    | expr SUPERSCRIPT                                                              #Superscript
    | op=('+' | '-') expr                                                           #Negate
    | expr op=('*' | '/') expr                                                      #Product
    | expr expr2                                                                    #Juxtaposition
    | expr op=('+' | '-') expr                                                      #Sum
    ;

expr2
    : ID                                                                            #Variable2
    | NUMBER                                                                        #Literal2
    | '(' expr ')'                                                                  #Parenthetical2
    | ID '[' arguments ']'                                                          #FunctionCall2
    | <assoc=right> expr op=('^' | '**') expr                                       #Power2
    | expr SUPERSCRIPT                                                              #Superscript2
    | expr op=('*' | '/') expr                                                      #Product2
    ;

arguments
    : (expr (',' expr)*)?
    ;

fragment LETTER
    : [\p{Ll}\p{Lu}\p{Lt}\p{Lm}\p{Lo}]
    ;

fragment DIGIT
    : [0-9]
    ;

ID
    : LETTER (LETTER | DIGIT)*
    ;

NUMBER
    : [0-9]+ ('.' [0-9]*)?
    | [0-9]* '.' [0-9]+
    ;

SUPERSCRIPT
    : '⁻'? [⁰¹²³⁴⁵⁶⁷⁸⁹]+
    ;

WHITESPACE
    : [ \r\n\t]+ -> skip
    ;