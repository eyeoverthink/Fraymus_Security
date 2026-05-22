package com.fraymus.absorbed.parsers;

import java.util.*;
import java.io.*;

public class glm47 {

    public static class GLM47Parser {
    }
        func (p *GLM47Parser) Init(tools []api.Tool, lastMessage *api.Message, thinkValue *api.ThinkValue) []api.Tool {
        p.tools = tools;
        p.callIndex = 0;
        if thinkValue == null || thinkValue.Bool() {
        p.state = glm46ParserState_CollectingThinking;
    }
        return tools;
    }
}
