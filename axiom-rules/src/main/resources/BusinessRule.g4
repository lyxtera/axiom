grammar BusinessRule;

// Parser Rules
businessRule
    : expression 'then' outcome EOF
    ;

expression
    : subExpression
    ;

subExpression
    : subExpression AND subExpression             # andExpression
    | subExpression OR subExpression              # orExpression
    | NOT subExpression                           # notExpression
    | '(' subExpression ')'                       # groupingExpression
    | comparison                                  # comparisonExpression
    ;

comparison
    : businessCheck comparisonOperator literal    # comparisonOperation
    | businessCheck                               # businessBooleanExpression
    ;

businessCheck
    : IDENTIFIER '(' arguments? ')'               # businessCheckExpression
    ;

arguments
    : literal (',' literal)*
    ;

comparisonOperator
    : '=' | '<' | '>'
    ;

literal
    : NUMBER                                      # numberLiteral
    | STRING                                      # stringLiteral
    ;

outcome
    : businessAction (';' businessAction)*                        # businessActionList
    ;

businessAction
    : IDENTIFIER '(' arguments? ')'                  # businessActionCall
    ;

// Lexer Rules
NOT     : 'not' ;
AND     : 'and' ;
OR      : 'or' ;
IDENTIFIER
        : [a-zA-Z_][a-zA-Z_0-9]*
        ;
NUMBER  : [0-9]+ ('.' [0-9]+)? ;
STRING  : '"' (~["\\] | '\\' .)* '"' ;
WS      : [ \t\n\r]+ -> skip ;
ILLEGAL_CHAR
        : . ;
