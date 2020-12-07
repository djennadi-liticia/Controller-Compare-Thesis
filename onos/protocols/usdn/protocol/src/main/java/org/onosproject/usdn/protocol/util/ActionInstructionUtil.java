package org.onosproject.usdn.protocol.util;

import com.github.usdn.flowtable.*;
import com.github.usdn.packet.USDNnetworkPacket;
import org.onosproject.net.sensorflow.*;

import java.util.ArrayList;
import java.util.List;

import static com.github.usdn.flowtable.FlowTableInterface.*;
import static com.github.usdn.flowtable.ModifyAction.ADD;

public class ActionInstructionUtil {
    public List<AbstractAction> getAction(SensorFlowInstruction instruction) {
        List<AbstractAction> actionSet = new ArrayList<>();
        switch (instruction.getSensorFlowInstructionType()) {
            case DROP:
                DropAction actionDrop = new DropAction();

                actionSet.add(actionDrop);
                break;
            case SET_DST_ADDR:
                SensorFlowSetDstAddrInstruction setDstAddrInstruction =
                        (SensorFlowSetDstAddrInstruction) instruction;

                byte[] addr = setDstAddrInstruction.getDstAddr().getAddr();
                ModifyAction setPktDstAddrHAction = new ModifyAction();
                setPktDstAddrHAction.setLhsLocation(CONST);
                setPktDstAddrHAction.setLhs(0);
                setPktDstAddrHAction.setRhsLocation(CONST);
                setPktDstAddrHAction.setRhs(addr[0]);
                setPktDstAddrHAction.setResLocation(PACKET);
                setPktDstAddrHAction.setRes(USDNnetworkPacket.DST_INDEX);

                ModifyAction setPktDstAddrLAction = new ModifyAction();
                setPktDstAddrLAction.setLhsLocation(CONST);
                setPktDstAddrLAction.setLhs(0);
                setPktDstAddrLAction.setRhsLocation(CONST);
                setPktDstAddrLAction.setRhs(addr[1]);
                setPktDstAddrLAction.setResLocation(PACKET);
                setPktDstAddrLAction.setRes(USDNnetworkPacket.DST_INDEX + 1);

                actionSet.add(setPktDstAddrHAction);
                actionSet.add(setPktDstAddrLAction);
                break;
            case SET_PKT_LEN:
                SensorFlowSetPacketLengthInstruction setPacketLengthInstruction =
                        (SensorFlowSetPacketLengthInstruction) instruction;

                ModifyAction setPktLenAction = new ModifyAction();
                setPktLenAction.setLhsLocation(CONST);
                setPktLenAction.setLhs(setPacketLengthInstruction.length());
                setPktLenAction.setResLocation(PACKET);
                setPktLenAction.setRes(USDNnetworkPacket.LEN_INDEX);

                actionSet.add(setPktLenAction);
                break;
            case SET_STATE_VALUE_CONST:
                SensorFlowSetStateValueConstInstruction setStateValueInstruction =
                        (SensorFlowSetStateValueConstInstruction) instruction;

                ModifyAction setStateValueAction = new ModifyAction();
                setStateValueAction.setLhsLocation(CONST);
                setStateValueAction.setLhs(0);
                setStateValueAction.setRhsLocation(CONST);
                setStateValueAction.setRhs(setStateValueInstruction.value());
                setStateValueAction.setOperator(ADD);
                setStateValueAction.setResLocation(STATUS);
                setStateValueAction.setRes(setStateValueInstruction.beginPosition());

                actionSet.add(setStateValueAction);
                break;
            case ASK_CONTROLLER:
                actionSet.add(new QueryAction());
                break;
            case REMATCH_PACKET:
                actionSet.add(new AcceptAction());
                break;
            case SET_PKT_VAL_CONST:
                SensorFlowSetPacketValueConstInstruction setPacketValueConstInstruction =
                        (SensorFlowSetPacketValueConstInstruction) instruction;

                ModifyAction setPktValueConst = new ModifyAction();
                setPktValueConst.setLhsLocation(CONST);
                setPktValueConst.setLhs(0);
                setPktValueConst.setOperator(ADD);
                setPktValueConst.setRhsLocation(CONST);
                setPktValueConst.setRhs(setPacketValueConstInstruction.value());
                setPktValueConst.setResLocation(PACKET);
                setPktValueConst.setRes(setPacketValueConstInstruction.packetPosition());

                actionSet.add(setPktValueConst);
                break;
            case SET_STATE_VALUE_PACKET:
                SensorFlowSetStateValuePacketInstruction setStateValuePacketInstruction =
                        (SensorFlowSetStateValuePacketInstruction) instruction;

                ModifyAction setStateValuePacketAction = new ModifyAction();
                setStateValuePacketAction.setLhsLocation(CONST);
                setStateValuePacketAction.setLhs(0);
                setStateValuePacketAction.setRhsLocation(PACKET);
                setStateValuePacketAction.setRhs(setStateValuePacketInstruction.packetPosition());
                setStateValuePacketAction.setOperator(ADD);
                setStateValuePacketAction.setResLocation(STATUS);
                setStateValuePacketAction.setRes(setStateValuePacketInstruction.beginPosition());

                actionSet.add(setStateValuePacketAction);
                break;
            case STATE_CONST_OP:
                SensorFlowSetStateValueWithOpConstInstruction setStateValueWithOpConstInstruction =
                        (SensorFlowSetStateValueWithOpConstInstruction) instruction;

                ModifyAction setStateValueOpConstAction = new ModifyAction();
                setStateValueOpConstAction.setLhsLocation(CONST);
                setStateValueOpConstAction.setLhs(setStateValueWithOpConstInstruction.constValue());
                setStateValueOpConstAction.setOperator(
                        translateOperator(setStateValueWithOpConstInstruction.getOperator()));
                setStateValueOpConstAction.setRhsLocation(STATUS);
                setStateValueOpConstAction.setRhs(setStateValueWithOpConstInstruction.stateOperandPos());
                setStateValueOpConstAction.setResLocation(STATUS);
                setStateValueOpConstAction.setRes(setStateValueWithOpConstInstruction.stateResultPos());

                actionSet.add(setStateValueOpConstAction);
                break;
            case STATE_PACKET_OP:
                SensorFlowSetStateValueWithOpPacketInstruction setStateValueWithOpPacketInstruction =
                        (SensorFlowSetStateValueWithOpPacketInstruction) instruction;

                ModifyAction setStateValueOpPacketAction = new ModifyAction();
                setStateValueOpPacketAction.setLhsLocation(STATUS);
                setStateValueOpPacketAction.setLhs(setStateValueWithOpPacketInstruction.stateOperandPos());
                setStateValueOpPacketAction.setOperator(
                        translateOperator(setStateValueWithOpPacketInstruction.getOperator()));
                setStateValueOpPacketAction.setRhsLocation(PACKET);
                setStateValueOpPacketAction.setRhs(setStateValueWithOpPacketInstruction.packetPos());
                setStateValueOpPacketAction.setResLocation(STATUS);
                setStateValueOpPacketAction.setRes(setStateValueWithOpPacketInstruction.stateResultPos());

                actionSet.add(setStateValueOpPacketAction);
                break;
            case STATE_STATE_OP:
                SensorFlowSetStateValueWithOpStateInstruction setStateValueWithOpStateInstruction =
                        (SensorFlowSetStateValueWithOpStateInstruction) instruction;

                ModifyAction setStateValueOpStateAction = new ModifyAction();
                setStateValueOpStateAction.setLhsLocation(STATUS);
                setStateValueOpStateAction.setLhs(setStateValueWithOpStateInstruction.stateOperand1Pos());
                setStateValueOpStateAction.setOperator(
                        translateOperator(setStateValueWithOpStateInstruction.getOperator()));
                setStateValueOpStateAction.setRhsLocation(STATUS);
                setStateValueOpStateAction.setRhs(setStateValueWithOpStateInstruction.stateOperand2Pos());
                setStateValueOpStateAction.setResLocation(STATUS);
                setStateValueOpStateAction.setRes(setStateValueWithOpStateInstruction.stateResultPos());

                actionSet.add(setStateValueOpStateAction);
                break;
            default:
                break;
        }
        return actionSet;
    }

    private byte translateOperator(SensorFlowInstruction.Operator operator) {
        switch (operator) {
            case ADD:
                return ADD;
            case AND:
                return ModifyAction.AND;
            case MULTIPLY:
                return ModifyAction.MUL;
            case OR:
                return ModifyAction.OR;
            case XOR:
                return ModifyAction.XOR;
            default:
                return -1;
        }
    }
}
