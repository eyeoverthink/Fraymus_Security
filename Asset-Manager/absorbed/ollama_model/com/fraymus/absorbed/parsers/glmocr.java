package com.fraymus.absorbed.parsers;

import java.util.*;
import java.io.*;

public class glmocr {

    public static class GlmOcrParser {
    }
        func (p *GlmOcrParser) HasThinkingSupport() boolean {
        return false;
    }
        func (p *GlmOcrParser) Init(tools []api.Tool, _ *api.Message, _ *api.ThinkValue) []api.Tool {
        p.tools = tools;
        return tools;
    }
}
