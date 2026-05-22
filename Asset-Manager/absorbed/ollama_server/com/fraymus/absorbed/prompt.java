package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class prompt {
        "bytes";
        "context";
        "errors";
        "fmt";
        "log/slog";
        "slices";
        "strings";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/llm";
        "github.com/ollama/ollama/model/renderers";
        "github.com/ollama/ollama/template";
        );
        type tokenizeFunc func(context.Context, String) ([]int, error);

    public static void chatPrompt(context.Context ctx, *Model m, tokenizeFunc tokenize, *api.Options opts, []api.Message msgs, []api.Tool tools, *api.ThinkValue think, []llm.ImageData images, error _) {
        var system []api.Message;
        var imageNumTokens = 768;
        var lastMsgIdx = len(msgs) - 1;
        var currMsgIdx = 0;
        if truncate {
        var for i = 0; i <= lastMsgIdx; i++ {
        system = make([]api.Message, 0);
        var for j = range i {
        if msgs[j].Role == "system" {
        system = append(system, msgs[j]);
    }
    }
        var p, err = renderPrompt(m, append(system, msgs[i:]...), tools, think);
        if err != null {
        return "", null, err;
    }
        var s, err = tokenize(ctx, p);
        if err != null {
        return "", null, err;
    }
        var ctxLen = len(s);
        if m.ProjectorPaths != null {
        var for _, msg = range msgs[i:] {
        ctxLen += imageNumTokens * len(msg.Images);
    }
    }
        if ctxLen <= opts.NumCtx {
        currMsgIdx = i;
        break;
    }
        if i == lastMsgIdx {
        currMsgIdx = lastMsgIdx;
        break;
    }
    }
    }
        if currMsgIdx > 0 {
        slog.Debug("truncating input messages which exceed context length", "truncated", len(msgs[currMsgIdx:]));
    }
        var for cnt, msg = range msgs[currMsgIdx:] {
        if slices.Contains(m.Config.ModelFamilies, "mllama") && len(msg.Images) > 1 {
        return "", null, errors.New("this model only supports one image while more than one image requested");
    }
        var prefix String;
        var prompt = msg.Content;
        var for _, i = range msg.Images {
        var imgData = llm.ImageData{
        ID:   len(images),;
        Data: i,;
    }
        images = append(images, imgData);
        if m.Config.Renderer != "" {
        continue;
    }
        var imgTag = fmt.Sprintf("[img-%d]", imgData.ID);
        if !strings.Contains(prompt, "[img]") {
        prefix += imgTag;
        } else {
        prompt = strings.Replace(prompt, "[img]", imgTag, 1);
    }
    }
        msgs[currMsgIdx+cnt].Content = prefix + prompt;
    }
        var p, err = renderPrompt(m, append(system, msgs[currMsgIdx:]...), tools, think);
        if err != null {
        return "", null, err;
    }
        return p, images, null;
    }

    public static void renderPrompt(*Model m, []api.Message msgs, []api.Tool tools) {
        if m.Config.Renderer != "" {
        var rendererName = resolveRendererName(m);
        var rendered, err = renderers.RenderWithRenderer(rendererName, msgs, tools, think);
        if err != null {
        return "", err;
    }
        return rendered, null;
    }
        var b bytes.Buffer;
        var thinkVal = false;
        var thinkLevel = "";
        if think != null {
        thinkVal = think.Bool();
        thinkLevel = think.String();
    }
        var if err = m.Template.Execute(&b, template.Values{Messages: msgs, Tools: tools, Think: thinkVal, ThinkLevel: thinkLevel, IsThinkSet: think != null}); err != null {
        return "", err;
    }
        return b.String(), null;
    }
}
