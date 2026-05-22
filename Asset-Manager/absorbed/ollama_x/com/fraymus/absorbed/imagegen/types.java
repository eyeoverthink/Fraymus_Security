package com.fraymus.absorbed.imagegen;

import java.util.*;
import java.io.*;

public class types {

    public static class Request {
        public String Prompt;
        public *RequestOptions Options;
        public int32 Width;
        public int32 Height;
        public int Steps;
        public long Seed;
        public [][]byte Images;
    }

    public static class RequestOptions {
        public int NumPredict;
        public double Temperature;
        public double TopP;
        public int TopK;
        public []String Stop;
    }

    public static class Response {
        public String Content;
        public String Image;
        public boolean Done;
        public int DoneReason;
        public String StopReason;
        public int Step;
        public int Total;
        public int PromptEvalCount;
        public int PromptEvalDuration;
        public int EvalCount;
        public int EvalDuration;
    }

    public static class HealthResponse {
        public String Status;
        public float32 Progress;
    }
        type ModelMode int;
        const (;
        ModeLLM ModelMode = iota;
        ModeImageGen;
        );
        func (m ModelMode) String() String {
        switch m {
        case ModeLLM:;
        return "llm";
        case ModeImageGen:;
        return "imagegen";
        default:;
        return "unknown";
    }
    }
}
