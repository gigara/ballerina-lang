package ballerina.lang.math;

import ballerina.doc;


@doc:Description { value:"Returns Euler's number, that is 'e' raised to the power of exponent"}
@doc:Param { value:"val: exponent value to raise" }
@doc:Return { value:"float: exp value" }
native function exp (float val) (float);

@doc:Description { value:"Returns the value of the 'a' raised to the power of 'b'"}
@doc:Param { value:"a: the base value" }
@doc:Param { value:"b: the exponent value" }
@doc:Return { value:"float: result value" }
native function pow (float a, float b) (float);
