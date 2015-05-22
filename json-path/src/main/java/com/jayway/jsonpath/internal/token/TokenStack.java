
package com.jayway.jsonpath.internal.token;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.EvaluationCallback;
import com.jayway.jsonpath.internal.Path;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 *
 * @author Hunter Payne
 **/
public class TokenStack
{
    protected static Logger log = Logger.getLogger("com.jayway.jsonpath");

    protected Configuration conf;
    protected Stack<TokenStackElement> elements;
    protected List<Path> paths;
    protected Map<TokenStackElement, Path> matchedPaths;

    private TokenStackElement curr;

    public TokenStack(Configuration conf)
    {
        this.conf = conf;
        paths = new ArrayList<Path>();
        matchedPaths = new HashMap<TokenStackElement, Path>();
        elements = new Stack<TokenStackElement>();
    }

    public Stack<TokenStackElement> getStack()
    {
        return elements;
    }

    /**
     * registers a path for which to fire results
     */
    public void registerPath(Path path)
    {
        paths.add(path);
    }

    // reads tokens and goes to first leaf
    public void read(JsonParser parser, EvaluationCallback callback)
    {
        assert(callback != null);
        try {
            //boolean lookingForRow = true;
            //TokenStackElement rowToken = null;
            boolean needsPathCheck = false;
            while (parser.nextToken() != null) {
                boolean saveMatch = false;
                switch (parser.getCurrentToken()) {
                case START_ARRAY:
                {
                    if (curr != null) {
                        TokenStackElement newElem = new ArrayToken();
                        curr.setValue(newElem);
                        curr = newElem;
                    } else {
                        curr = new ArrayToken();
                    }
                    saveMatch = true;
                    needsPathCheck = true;
                    elements.push(curr);
                    break;
                }
                case END_ARRAY:
                {
                    Path match = matchedPaths.remove(curr);
                    if (match != null) {
                        callback.resultFoundExit(match);
                    }
                    elements.pop();
                    if (elements.empty()) curr = null;
                    else curr = elements.peek();
                    saveMatch = true;
                    needsPathCheck = true;

                    break;
                }
                case VALUE_EMBEDDED_OBJECT:
                case START_OBJECT:
                {
                    if (curr != null) {
                        TokenStackElement newElem = new ObjectToken();
                        curr.setValue(newElem);
                        curr = newElem;
                    } else {
                        curr = new ObjectToken();
                    }
                    //if (!elements.empty())
                    saveMatch = true;
                    needsPathCheck = true;
                    elements.push(curr);
                    break;
                }
                case END_OBJECT:
                {
                    Path match = matchedPaths.remove(curr);
                    if (match != null) {
                        callback.resultFoundExit(match);
                    }
                    elements.pop();
                    if (elements.empty()) curr = null;
                    else curr = elements.peek();
                    saveMatch = true;
                    needsPathCheck = true;
                    break;
                }
                case FIELD_NAME:
                {
                    assert(curr instanceof ObjectToken);
                    ((ObjectToken)curr).key = parser.getText();
                    break;
                }
                case VALUE_FALSE:
                {
                    StringToken newToken = new StringToken("FALSE");
                    curr.setValue(newToken);
                    needsPathCheck = true;
                    break;
                }
                case VALUE_TRUE:
                {
                    StringToken newToken = new StringToken("TRUE");
                    curr.setValue(newToken);
                    needsPathCheck = true;
                    break;
                }
                case VALUE_NUMBER_FLOAT:
                {
                    FloatToken newToken =
                        new FloatToken((float)parser.getValueAsDouble());
                    curr.setValue(newToken);
                    needsPathCheck = true;
                    break;
                }
                case VALUE_NUMBER_INT:
                {
                    IntToken newToken =
                        new IntToken(parser.getValueAsInt());
                    curr.setValue(newToken);
                    needsPathCheck = true;
                    break;
                }
                case VALUE_STRING:
                {
                    StringToken newToken =
                        new StringToken(parser.getText());
                    curr.setValue(newToken);
                    needsPathCheck = true;
                    break;
                }
                case VALUE_NULL:
                {
                    curr.setValue(null);
                    needsPathCheck = true;
                    break;
                }
                default:
                    assert false;
                }
                // now check the paths for matches
                if (needsPathCheck) {
                    for (Path path : paths) {
                        if (path.checkForMatch(this)) {
                            if (saveMatch) {
                                Path oldMatch = matchedPaths.get(curr);
                                if (oldMatch != null) {
                                    callback.resultFoundExit(oldMatch);
                                }
                                matchedPaths.put(curr, path);
                                break;
                            }
                            callback.resultFound(path);
                        }
                    }
                    needsPathCheck = false;
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
            log.log(Level.INFO, e.getMessage(), e);
        }
        log.fine("finished read");
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Token Stack depth=");
        sb.append(elements.size());
        for (TokenStackElement elem : elements) {
            sb.append(" ");
            sb.append(elem.toString());
        }
        sb.append(" ");
        sb.append(elements.peek().getValue());
        return sb.toString();
    }

    public Configuration configuration()
    {
        return conf;
    }
}

// End TokenStack.java

