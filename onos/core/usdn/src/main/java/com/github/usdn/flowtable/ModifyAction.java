package com.github.usdn.flowtable;

import com.github.usdn.packet.USDNnetworkPacket;

import static com.github.usdn.flowtable.AbstractAction.Action.SDN_FT_ACTION_MODIFY;
import static com.github.usdn.flowtable.FlowtableStructure.getOperandFromString;
import static com.github.usdn.util.Utils.*;

public final class ModifyAction extends AbstractAction{


    public static final byte ADD = 0,
            AND = 5,
            DIV = 3,
            MOD = 4,
            MUL = 2,
            OR = 6,
            SUB = 1,
            XOR = 7;

    private static final byte LEFT_BIT = 1,
            LEFT_INDEX_H = 3,
            LEFT_INDEX_L = 4,
            LEFT_LEN = 2,
            OP_BIT = 3,
            OP_INDEX = 0, OP_LEN = 3,
            RES_BIT = 0,
            RES_INDEX_H = 1,
            RES_INDEX_L = 2,
            RES_LEN = 1,
            RIGHT_BIT = 6,
            RIGHT_INDEX_H = 5,
            RIGHT_INDEX_L = 6,
            RIGHT_LEN = LEFT_LEN;
    private static final int FULL_SET = 6,
            HALF_SET = 4,
            RES = 1,
            LHS = 3,
            RHS = 5,
            OP = 4;
    private static final byte SIZE = 7;

    public ModifyAction() {
        super(SDN_FT_ACTION_MODIFY, SIZE);
    }
    public ModifyAction(final byte[] value) {
        super(value);
    }

    public ModifyAction(final String val) {
        super(SDN_FT_ACTION_MODIFY, SIZE);
        String[] operands = val.split(" ");
        if (operands.length == FULL_SET) {
            String res = operands[RES];
            String lhs = operands[LHS];
            String rhs = operands[RHS];

            int[] tmpRes = getResFromString(res);
            int[] tmpLhs = getOperandFromString(lhs);
            int[] tmpRhs = getOperandFromString(rhs);

            setResLocation(tmpRes[0]);
            setRes(tmpRes[1]);

            setLhsLocation(tmpLhs[0]);
            setLhs(tmpLhs[1]);

            setOperator(getOperatorFromString(operands[OP]));

            setRhsLocation(tmpRhs[0]);
            setRhs(tmpRhs[1]);

        } else if (operands.length == HALF_SET) {

            String res = operands[RES];
            String lhs = operands[LHS];

            int[] tmpRes = getResFromString(res);
            int[] tmpLhs = getOperandFromString(lhs);

            setResLocation(tmpRes[0]);
            setRes(tmpRes[1]);

            setLhsLocation(tmpLhs[0]);
            setLhs(tmpLhs[1]);

            setRhsLocation(NULL);
            setRhs(0);
        }

    }

    public int getLhs() {
        return mergeBytes(getValue(LEFT_INDEX_H), getValue(LEFT_INDEX_L));
    }
    public int getLhsLocation() {
        return getBitRange(getValue(OP_INDEX), LEFT_BIT, LEFT_LEN);
    }
    public String getLhsToString() {
        switch (getLhsLocation()) {
            case NULL:
                return "";
            case CONST:
                return String.valueOf(getLhs());
            case PACKET:
                return "P." + USDNnetworkPacket.getNetworkPacketByteName(getLhs());
            case STATUS:
                return "R." + getLhs();
            default:
                return "";
        }
    }

    public int getOperator() {
        return getBitRange(getValue(OP_INDEX), OP_BIT, OP_LEN);
    }
    public int getOperatorFromString(final String val) {
        switch (val.trim()) {
            case ("+"):
                return ADD;
            case ("-"):
                return SUB;
            case ("*"):
                return MUL;
            case ("/"):
                return DIV;
            case ("%"):
                return MOD;
            case ("&"):
                return AND;
            case ("|"):
                return OR;
            case ("^"):
                return XOR;
            default:
                throw new IllegalArgumentException();
        }
    }
    public String getOperatorToString() {
        switch (getOperator()) {
            case (ADD):
                return " + ";
            case (SUB):
                return " - ";
            case (MUL):
                return " * ";
            case (DIV):
                return " / ";
            case (MOD):
                return " % ";
            case (AND):
                return " & ";
            case (OR):
                return " | ";
            case (XOR):
                return " ^ ";
            default:
                return "";
        }
    }
    public int getRes() {
        return mergeBytes(getValue(RES_INDEX_H), getValue(RES_INDEX_L));
    }

    public int[] getResFromString(final String val) {
        int[] tmp = new int[2];
        String[] strVal = val.split("\\.");
        switch (strVal[0]) {
            case "P":
                tmp[0] = PACKET;
                break;
            case "R":
                tmp[0] = STATUS;
                break;
            default:
                throw new IllegalArgumentException();
        }

        if (tmp[0] == PACKET) {
            tmp[1] = USDNnetworkPacket.getNetworkPacketByteFromName(strVal[1]);
        } else {
            tmp[1] = Integer.parseInt(strVal[1]);
        }
        return tmp;
    }
    public int getResLocation() {
        return getBitRange(getValue(OP_INDEX), RES_BIT, RES_LEN) + 2;
    }

    public String getResToString() {
        switch (getResLocation()) {
            case PACKET:
                return SDN_FT_ACTION_MODIFY.name() + " P."
                        + USDNnetworkPacket.getNetworkPacketByteName(getRes())
                        + " = ";
            case STATUS:
                return SDN_FT_ACTION_MODIFY.name() + " R." + getRes() + " = ";
            default:
                return "";
        }
    }
    public int getRhs() {
        return mergeBytes(getValue(RIGHT_INDEX_H), getValue(RIGHT_INDEX_L));
    }
    public int getRhsLocation() {
        return getBitRange(getValue(OP_INDEX), RIGHT_BIT, RIGHT_LEN);
    }
    public String getRhsToString() {
        switch (getRhsLocation()) {
            case NULL:
                return "";
            case CONST:
                return String.valueOf(getRhs());
            case PACKET:
                return "P." + USDNnetworkPacket.getNetworkPacketByteName(getRhs());
            case STATUS:
                return "R." + getRhs();
            default:
                return "";
        }
    }
    public ModifyAction setLhs(final int val) {
        setValue(LEFT_INDEX_L, (byte) val);
        setValue(LEFT_INDEX_H, (byte) val >>> Byte.SIZE);
        return this;
    }
    public ModifyAction setLhsLocation(final int value) {
        setValue(OP_INDEX, (byte) setBitRange(getValue(OP_INDEX),
                LEFT_BIT, LEFT_LEN, value));
        return this;
    }
    public ModifyAction setOperator(final int value) {
        setValue(OP_INDEX, (byte) setBitRange(getValue(OP_INDEX),
                OP_BIT, OP_LEN, value));
        return this;
    }
    public ModifyAction setRes(final int val) {
        setValue(RES_INDEX_L, (byte) val);
        setValue(RES_INDEX_H, (byte) val >>> Byte.SIZE);
        return this;
    }
    public ModifyAction setResLocation(final int value) {
        setValue(OP_INDEX, (byte) setBitRange(getValue(OP_INDEX),
                RES_BIT, RES_LEN, value));
        return this;
    }
    public ModifyAction setRhs(final int val) {
        setValue(RIGHT_INDEX_L, (byte) val);
        setValue(RIGHT_INDEX_H, (byte) val >>> Byte.SIZE);
        return this;
    }

    public ModifyAction setRhsLocation(final int value) {
        setValue(OP_INDEX, (byte) setBitRange(getValue(OP_INDEX),
                RIGHT_BIT, RIGHT_LEN, value));
        return this;
    }

    @Override
    public String toString() {
        String f = getResToString();
        String l = getLhsToString();
        String r = getRhsToString();
        String o = getOperatorToString();

        if (!l.isEmpty() && !r.isEmpty()) {
            return f + l + o + r;
        } else if (r.isEmpty()) {
            return f + l;
        } else {
            return f + r;
        }
    }





}
