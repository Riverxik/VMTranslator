package ru.riverx;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by RiVeRx on 09.03.2021.
 */
public class Parser {
    private final List<String> lines;
    private final String filename;
    private final int size;
    public List<String> code;
    private int count;

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
                case PUSH:  push(segments[1].toLowerCase(), segments[2].toLowerCase()); break;
                case POP:   pop(segments[1].toLowerCase(), segments[2].toLowerCase()); break;
                case ADD:   add(); break;
                case SUB:   sub(); break;
                case NEG:   neg(); break;
                case EQ:    eq(); break;
                case GT:    gt(); break;
                case LT:    lt(); break;
                case AND:   and(); break;
                case OR:    or(); break;
                case NOT:   not(); break;
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
        if (segment.equals("constant")) { pushConst(i); return; }
        if (segment.equals("static"))   { pushStatic(i); return; }
        if (segment.equals("temp"))     { pushTemp(i); return; }
        if (segment.equals("pointer"))  { pushPointer(i); return; }
        if (segment.equals("local"))    { code.add("@LCL"); }
        if (segment.equals("argument")) { code.add("@ARG"); }
        if (segment.equals("this"))     { code.add("@THIS"); }
        if (segment.equals("that"))     { code.add("@THAT"); }
        code.add("D=M");
        code.add("@"+i);
        code.add("A=D+A");
        code.add("D=M");
        code.add("@SP");
        code.add("AM=M+1");
        code.add("A=A-1");
        code.add("M=D");
    }

    private void pushConst(String i) {
        code.add("@"+i);
        code.add("D=A");
        code.add("@SP");
        code.add("AM=M+1");
        code.add("A=A-1");
        code.add("M=D");
    }

    private void pushStatic(String i) {
        code.add("@"+filename+"."+i);
        code.add("D=M");
        code.add("@SP");
        code.add("AM=M+1");
        code.add("A=A-1");
        code.add("M=D");
    }

    private void pushTemp(String i) {
        code.add("@"+(5+Integer.parseInt(i)));
        code.add("D=M");
        code.add("@SP");
        code.add("AM=M+1");
        code.add("A=A-1");
        code.add("M=D");
    }

    private void pushPointer(String i) {
        if (i.equals("0")) code.add("@THIS");
        if (i.equals("1")) code.add("@THAT");
        code.add("D=M");
        code.add("@SP");
        code.add("AM=M+1");
        code.add("A=A-1");
        code.add("M=D");
    }

    private void pop(String segment, String i) {
        if (segment.equals("static"))   { popStatic(i); return; }
        if (segment.equals("temp"))     { popTemp(i); return; }
        if (segment.equals("pointer"))  { popPointer(i); return; }
        if (segment.equals("local"))    { code.add("@LCL"); }
        if (segment.equals("argument")) { code.add("@ARG"); }
        if (segment.equals("this"))     { code.add("@THIS"); }
        if (segment.equals("that"))     { code.add("@THAT"); }
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

    private void popStatic(String i) {
        code.add("@SP");
        code.add("AM=M-1");
        code.add("D=M");
        code.add("@"+filename+"."+i);
        code.add("M=D");
    }

    private void popTemp(String i) {
        code.add("@SP");
        code.add("AM=M-1");
        code.add("D=M");
        code.add("@"+(5+Integer.parseInt(i)));
        code.add("M=D");
    }

    private void popPointer(String i) {
        code.add("@SP");
        code.add("AM=M-1");
        code.add("D=M");
        if (i.equals("0")) code.add("@THIS");
        if (i.equals("1")) code.add("@THAT");
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

    private void logic() {
        code.add("@SP");
        code.add("AM=M-1");
        code.add("D=M");        // y
        code.add("A=A-1");
        code.add("D=M-D");      // D - value (x-y), A - address.
        code.add("@IF"+count);
    }

    private void logicEnd() {
        code.add("@SP");
        code.add("A=M-1");
        code.add("M=0");        // false.
        code.add("@IFEND"+count);
        code.add("0;JMP");
        code.add("(IF"+count+")");
        code.add("@SP");
        code.add("A=M-1");
        code.add("M=-1");       // true.
        code.add("(IFEND"+count+")");
    }

    private void eq() {
        logic();
        code.add("D;JEQ");
        logicEnd();
    }

    private void gt() {
        logic();
        code.add("D;JGT");
        logicEnd();
    }

    private void lt() {
        logic();
        code.add("D;JLT");
        logicEnd();
    }

    private void and() {
        code.add("@SP");
        code.add("AM=M-1");
        code.add("D=M");
        code.add("A=A-1");
        code.add("M=M&D");
    }

    private void or() {
        code.add("@SP");
        code.add("AM=M-1");
        code.add("D=M");
        code.add("A=A-1");
        code.add("M=M|D");
    }

    private void not() {
        code.add("@SP");
        code.add("A=M-1");
        code.add("M=!M");
    }
}
