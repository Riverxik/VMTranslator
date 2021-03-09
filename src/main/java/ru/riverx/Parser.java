package ru.riverx;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by RiVeRx on 09.03.2021.
 */
public class Parser {
    private List<String> lines;
    public List<String> code;
    private String filename;
    private int count;
    private int size;

    enum Type {
        PUSH,
        POP,
        ADD,
        SUB,
        NEG,
        EQ,
        GT,
        LT,
        AND,
        OR,
        NOT,
        INVALID
    }

    public Parser(List<String> lines, String filename) {
        this.lines = lines;
        this.code = new ArrayList<>();
        this.filename = filename;
        this.count = 0;
        this.size = lines.size();
    }

    public void parse() {
        while (hasNext()) {
            String line = getNext();
            if (line.startsWith("//") || line.equals("")) continue;
            String[] segments = line.split(" ");
            if (segments.length == 0) continue;
            code.add("// " + line);
            switch (getType(segments[0].toLowerCase())) {
                case PUSH: push(segments[1].toLowerCase(), segments[2].toLowerCase()); break;
                case POP: pop(segments[1].toLowerCase(), segments[2].toLowerCase()); break;
                case ADD: add(); break;
                case SUB: sub(); break;
                default:
                case INVALID: break;
            }
        }
    }

    private String getNext() {
        if (hasNext()) return lines.get(count++); return "";
    }

    private boolean hasNext() {
        return count < size;
    }

    private Type getType(String cmd) {
        if (cmd.equals("push")) return Type.PUSH;
        if (cmd.equals("pop")) return Type.POP;
        if (cmd.equals("add")) return Type.ADD;
        if (cmd.equals("sub")) return Type.SUB;
        if (cmd.equals("neg")) return Type.NEG;
        if (cmd.equals("eq")) return Type.EQ;
        if (cmd.equals("gt")) return Type.GT;
        if (cmd.equals("lt")) return Type.LT;
        if (cmd.equals("and")) return Type.AND;
        if (cmd.equals("or")) return Type.OR;
        if (cmd.equals("not")) return Type.NOT;
        return Type.INVALID;
    }

    private void push(String segment, String i) {
        if (segment.equals("constant")) {
            code.add("@"+i);
            code.add("D=A");
            code.add("@SP");
            code.add("AM=M+1");
            code.add("A=A-1");
            code.add("M=D");
            return;
        }
        if (segment.equals("static")) {
            code.add("@"+filename+"."+i);
            code.add("D=M");
            code.add("@SP");
            code.add("AM=M+1");
            code.add("A=A-1");
            code.add("M=D");
            return;
        }
        if (segment.equals("temp")) {
            code.add("@"+(5+Integer.parseInt(i)));
            code.add("D=M");
            code.add("@SP");
            code.add("AM=M+1");
            code.add("A=A-1");
            code.add("M=D");
        }
        if (segment.equals("local")) {
            code.add("@LCL");
        }
        if (segment.equals("argument")) {
            code.add("@ARG");
        }
        if (segment.equals("this") || segment.equals("pointer") && i.equals("0")) {
            code.add("@THIS");
        }
        if (segment.equals("that") || segment.equals("pointer") && i.equals("1")) {
            code.add("@THAT");
        }
        code.add("D=M");
        code.add("@"+i);
        code.add("A=D+A");
        code.add("D=M");
        code.add("@SP");
        code.add("AM=M+1");
        code.add("A=A-1");
        code.add("M=D");
    }

    private void pop(String segment, String i) {
        if (segment.equals("static")) {
            code.add("@SP");
            code.add("AM=M-1");
            code.add("D=M");
            code.add("@"+filename+"."+i);
            code.add("M=D");
            return;
        }
        if (segment.equals("temp")) {
            code.add("@SP");
            code.add("AM=M-1");
            code.add("D=M");
            code.add("@"+(5+Integer.parseInt(i)));
            code.add("M=D");
            return;
        }
        if (segment.equals("local")) {
            code.add("@LCL");
        }
        if (segment.equals("argument")) {
            code.add("@ARG");
        }
        if (segment.equals("this") || segment.equals("pointer") && i.equals("0")) {
            code.add("@THIS");
        }
        if (segment.equals("that") || segment.equals("pointer") && i.equals("1")) {
            code.add("@THAT");
        }
        code.add("D=M");
        code.add("@"+i);
        code.add("D=D+A");
        code.add("@R13");
        code.add("M=D");
        code.add("@SP");
        code.add("AM=M-1");
        code.add("D=M");
        code.add("@R13");
        code.add("A=M");
        code.add("M=D");
    }

    private void add() {
        code.add("@SP");
        code.add("AM=M-1");
        code.add("D=M");
        code.add("A=A-1");
        code.add("M=M+D");
    }

    private void sub() {
        code.add("@SP");
        code.add("AM=M-1");
        code.add("D=M");
        code.add("A=A-1");
        code.add("M=M-D");
    }

    private void neg() {
        code.add("@SP");
        code.add("A=M-1");
        code.add("M=-M");
    }

    private void eq() {
        code.add("@SP");
        code.add("AM=M-1");
        code.add("D=M");
        code.add("A=A-1");
        code.add("");
    }

    private void gt() {

    }

    private void lt() {
        code.add("@SP");
        code.add("AM=M-1");
        code.add("D=M");
    }

    private void and() {

    }

    private void or() {

    }

    private void not() {

    }
}
