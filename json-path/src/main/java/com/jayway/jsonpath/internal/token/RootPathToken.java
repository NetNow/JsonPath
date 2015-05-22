/*
 * Copyright 2011 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jayway.jsonpath.internal.token;

import com.jayway.jsonpath.internal.PathRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class RootPathToken extends PathToken {

    private static final Logger logger = LoggerFactory.getLogger(RootPathToken.class);

    private PathToken tail;
    private int tokenCount;

    public RootPathToken() {
        this.tail = this;
        this.tokenCount = 1;
    }

    @Override
    public int getTokenCount() {
        return tokenCount;
    }

    public RootPathToken append(PathToken next) {
        this.tail = tail.appendTailToken(next);
        this.tokenCount++;
        return this;
    }

    @Override
    public void evaluate(String currentPath, PathRef pathRef, Object model, EvaluationContextImpl ctx) {
        if (isLeaf()) {
            PathRef op = ctx.forUpdate() ?  pathRef : PathRef.NO_OP;
            ctx.addResult("$", op, model);
        } else {
            next().evaluate("$", pathRef, model, ctx);
        }
    }

    @Override
    public boolean checkForMatch(TokenStack stack, int idx)
    {
        assert(0 == idx);
        if (0 == stack.getStack().size()) {
            return isLeaf();
        }
        return !isLeaf() && next().checkForMatch(stack, 0);
    }

    @Override
    public String getPathFragment() {
        return "$";
    }

    @Override
    boolean isTokenDefinite() {
        return true;
    }
}
