
package com.jayway.jsonpath.internal.token;

/**
 *
 * @author Hunter Payne
 **/
public class LongToken extends TokenStackElement
{
    public long value;

    public LongToken(long f)
    {
        value = f;
    }

    public TokenType getType()
    {
        return TokenType.LONG_TOKEN;
    }

    public TokenStackElement getValue()
    {
        return null;
    }

    public void setValue(TokenStackElement elem)
    {
        throw new RuntimeException();
    }
}

// End IntToken.java
