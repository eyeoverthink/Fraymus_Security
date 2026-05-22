package com.fraymus.absorbed.models.qwen3_5_moe;

import java.util.*;
import java.io.*;

public class qwen3_5_moe {
        "github.com/ollama/ollama/x/mlxrunner/model/base";
        "github.com/ollama/ollama/x/models/qwen3_5";
        );

    public static void init() {
        base.Register("Qwen3_5MoeForConditionalGeneration", qwen3_5.NewModel);
        base.Register("Qwen3_5MoeForCausalLM", qwen3_5.NewModel);
        base.Register("Qwen3NextMoeForConditionalGeneration", qwen3_5.NewModel);
        base.Register("Qwen3NextMoeForCausalLM", qwen3_5.NewModel);
    }
}
