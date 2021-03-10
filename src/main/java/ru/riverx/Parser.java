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
        LABEL,
        GOTO_LABEL,
        IF_GOTO_LABEL,
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
                case LABEL: label(segments[1].toLowerCase());
                case GOTO_LABEL: gotoLabel(segments[1].toLowerCase());
                case IF_GOTO_LABEL: ifGotoLabel(segments[1].toLowerCase());
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
        if (cmd.equals("push"))     return Type.PUSH;
        if (cmd.equals("pop"))      return Type.POP;
        if (cmd.equals("add"))      return Type.ADD;
        if (cmd.equals("sub"))      return Type.SUB;
        if (cmd.equals("neg"))      return Type.NEG;
        if (cmd.equals("eq"))       return Type.EQ;
        if (cmd.equals("gt"))       return Type.GT;
        if (cmd.equals("lt"))       return Type.LT;
        if (cmd.equals("and"))      return Type.AND;
        if (cmd.equals("or"))       return Type.OR;
        if (cmd.equals("not"))      return Type.NOT;
        if (cmd.equals("label"))    return Type.LABEL;
        if (cmd.equals("goto"))     return Type.GOTO_LABEL;
        if (cmd.equals("if-goto"))  return Type.IF_GOTO_LABEL;
        return Type.INVALID;
    }

    /**
     * Pushes value into global stack from @segment one with @i shift.
     * @segment can be constant, static, temp, pointer, local, argument, this, that.
     *
     * @param segment from which of the 'stacks' value will be pushed into global stack.
     * @param i local pointer for 'stacks'
     * */
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

    /**
     * Pushes given constant to the global stack.
     *
     * @param i constant to be pushed in global stack.
     * */
    private void pushConst(String i) {
        code.add("@"+i);
        code.add("D=A");
        code.add("@SP");
        code.add("AM=M+1");
        code.add("A=A-1");
        code.add("M=D");
    }

    /**
     * Pushes value from the static pointer into global stack.
     *
     * @param i shift for the static field.
     */
    private void pushStatic(String i) {
        code.add("@"+filename+"."+i);
        code.add("D=M");
        code.add("@SP");
        code.add("AM=M+1");
        code.add("A=A-1");
        code.add("M=D");
    }

    /**
     * Pushes temporary value into global stack.
     *
     * @param i shift for 'temp stack'.
     */
    private void pushTemp(String i) {
        code.add("@"+(5+Integer.parseInt(i)));
        code.add("D=M");
        code.add("@SP");
        code.add("AM=M+1");
        code.add("A=A-1");
        code.add("M=D");
    }

    /**
     * Pushes address from @this or @that pointers into the global stack.
     *
     * @param i can be 0 for this or 1 for that pointer.
     */
    private void pushPointer(String i) {
        if (i.equals("0")) code.add("@THIS");
        if (i.equals("1")) code.add("@THAT");
        code.add("D=M");
        code.add("@SP");
        code.add("AM=M+1");
        code.add("A=A-1");
        code.add("M=D");
    }

    /**
     * Pushes given @address to the stack.
     *
     * @param address address which will be pushed to the global stack.
     * */
    private void pushAddress(String address) {
        code.add("@"+address);
        code.add("D=A");
        code.add("@SP");
        code.add("AM=M+1");
        code.add("A=A-1");
        code.add("M=D");
    }

    /**
     * Pops the value from the global stack to the @segment 'stack' with @i shift.
     * @segment can be: static, temp, pointer, local, argument, this, that.
     *
     * @param segment name of the segment-stack for putting the value to.
     * @param i shift for the segment-stack.
     */
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

    private void label(String labelName) {
        code.add("(" + labelName + ")");
    }

    private void gotoLabel(String labelName) {
        code.add("@"+labelName);
        code.add("0;JMP");
    }

    private void ifGotoLabel(String labelName) {
        pop("local", "0");
        code.add("@LCL");
        code.add("D=M");
        code.add("@"+labelName);
        code.add("D+1;JEQ");        // True is -1, so (-1+1=0) == 0, if true then jump.
    }

    private void call(String funcName, String nArgs) {
        pushAddress("return"+count);    // Using the label declared below.
        pushAddress("LCL");             // Saves LCL of the caller.
        pushAddress("ARG");             // Saves ARG of the caller.
        pushAddress("THIS");            // Saves THIS of the caller.
        pushAddress("THAT");            // Saves THAT of the caller.
        repositionARG(nArgs);           // ARG = SP - 5 - nArgs.
        repositionLCL();                // LCL = SP.
        gotoLabel(funcName);            // Transfers control to the called function.
        label("return"+count); // Declares a label for the return-address.
    }

    private void repositionARG(String nVars) {
        code.add("@SP");
        code.add("D=A");        // Current value of address.
        code.add("@ARG");
        code.add("D=D-A");      // D is number of addresses between ARG and SP.
    }

    private void repositionLCL() {
        code.add("@SP");
        code.add("D=A");
        code.add("@LCL");
        code.add("M=D");
    }

    private void mFunction(String funcName, String nVars) {
        label(funcName);
        // Repeat nVars times:
        pushConst("0");

    }

    // This method is probably broken. I will continue work on it.
    private void mReturn() {
        pushConst("LCL");   // endFrame.
        popTemp("0");       // temp 0 is LCL.
        pushTemp("0");
        pushConst("5");
        sub();                 // Now at the bottom of the stack is address of the returnAddress.
        popTemp("1");       // temp 1 is returnAddress.
        pop("argument", "0");
        code.add("@ARG");
        code.add("D=A+1");
        code.add("@SP");
        code.add("M=D");                    // SP = ARG + 1
        push("temp", "0");
        pushConst("1");
        sub();
        pop("pointer", "1");    // THAT
        push("temp", "0");
        pushConst("2");
        sub();
        pop("pointer", "0");    // THIS.
        push("temp", "0");
        pushConst("3");
        sub();
        pop("argument", "0");   // ARG
        push("temp", "0");
        pushConst("4");
        sub();
        pop("local", "0");
        // goto return Address.
    }
}
