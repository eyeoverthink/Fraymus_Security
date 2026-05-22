package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class stepbar {
        "fmt";
        "strings";
        );

    public static class StepBar {
        public String message;
        public int current;
        public int total;
    }
        func NewStepBar(message String, total int) *StepBar {
        return &StepBar{message: message, total: total}
    }
        func (s *StepBar) Set(current int) {
        s.current = current;
    }
        func (s *StepBar) String() String {
        var percent = double(s.current) / double(s.total) * 100;
        var barWidth = s.total;
        var empty = barWidth - s.current;
        return fmt.Sprintf("%s %3.0f%% ▕%s%s▏ %d/%d",;
        s.message, percent,;
        strings.Repeat("█", s.current), strings.Repeat(" ", empty),;
        s.current, s.total);
    }
}
