package com.reader.base;

import com.uhf.uhf.R;
import com.uhf.uhf.VehicleApplication;

/**
 * Regarding UHF return code and description, please refer to Serial_Protocol_User's_Guide_V2.38_en  
 * @author Administrator
 *
 */
public class CMD {
	public final static byte RESET = 0x70;
	public final static byte SET_UART_BAUDRATE = 0x71;
	public final static byte GET_FIRMWARE_VERSION = 0x72;
	public final static byte SET_READER_ADDRESS = 0x73;
	public final static byte SET_WORK_ANTENNA = 0x74;
	public final static byte GET_WORK_ANTENNA = 0x75;
	public final static byte SET_OUTPUT_POWER = 0x76;
	public final static byte GET_OUTPUT_POWER = (byte)0x97;
	public final static byte SET_FREQUENCY_REGION = 0x78;
	public final static byte GET_FREQUENCY_REGION = 0x79;
	public final static byte SET_BEEPER_MODE = 0x7A;
	public final static byte GET_READER_TEMPERATURE = 0x7B;
	public final static byte READ_GPIO_VALUE = 0x60;
	public final static byte WRITE_GPIO_VALUE = 0x61;
	public final static byte SET_ANT_CONNECTION_DETECTOR = 0x62;
	public final static byte GET_ANT_CONNECTION_DETECTOR = 0x63;
	public final static byte SET_TEMPORARY_OUTPUT_POWER = 0x66;
	public final static byte SET_READER_IDENTIFIER = 0x67;
	public final static byte GET_READER_IDENTIFIER = 0x68;
	public final static byte SET_RF_LINK_PROFILE = 0x69;
	public final static byte GET_RF_LINK_PROFILE = 0x6A;
	public final static byte GET_RF_PORT_RETURN_LOSS = 0x7E;
	public final static byte INVENTORY = (byte) 0x80;
	public final static byte READ_TAG = (byte) 0x81;
	public final static byte WRITE_TAG = (byte) 0x82;
	public final static byte LOCK_TAG = (byte) 0x83;
	public final static byte KILL_TAG = (byte) 0x84;
	public final static byte SET_ACCESS_EPC_MATCH = (byte) 0x85;
	public final static byte GET_ACCESS_EPC_MATCH = (byte) 0x86;
	public final static byte REAL_TIME_INVENTORY = (byte) 0x89;
	public final static byte FAST_SWITCH_ANT_INVENTORY = (byte) 0x8A;
	public final static byte CUSTOMIZED_SESSION_TARGET_INVENTORY = (byte) 0x8B;
	public final static byte SET_IMPINJ_FAST_TID = (byte) 0x8C;
	public final static byte SET_AND_SAVE_IMPINJ_FAST_TID = (byte) 0x8D;
	public final static byte GET_IMPINJ_FAST_TID = (byte) 0x8E;
	public final static byte ISO18000_6B_INVENTORY = (byte) 0xB0;
	public final static byte ISO18000_6B_READ_TAG = (byte) 0xB1;
	public final static byte ISO18000_6B_WRITE_TAG = (byte) 0xB2;
	public final static byte ISO18000_6B_LOCK_TAG = (byte) 0xB3;
	public final static byte ISO18000_6B_QUERY_LOCK_TAG = (byte) 0xB4;
	public final static byte GET_INVENTORY_BUFFER = (byte) 0x90;
	public final static byte GET_AND_RESET_INVENTORY_BUFFER = (byte) 0x91;
	public final static byte GET_INVENTORY_BUFFER_TAG_COUNT = (byte) 0x92;
	public final static byte RESET_INVENTORY_BUFFER = (byte) 0x93;
    public final static byte OPERATE_TAG_MASK = (byte)0x98;
	
	public static String format(byte btCmd)
    {
		String strCmd = "";
        switch (btCmd)
        {
            case RESET:
            	strCmd = VehicleApplication.getContext().getResources().getString(R.string.reset);
                break;
            case SET_UART_BAUDRATE:
            	strCmd = VehicleApplication.getContext().getResources().getString(R.string.set_uart_baudrate);
                break;
            case GET_FIRMWARE_VERSION:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.get_firmware_version);
                break;
            case SET_READER_ADDRESS:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.set_reader_add);
                break;
            case SET_WORK_ANTENNA:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.set_work_ant);
                break;
            case GET_WORK_ANTENNA:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.get_work_ant);
                break;
            case SET_OUTPUT_POWER:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.set_output_power);
                break;
            case GET_OUTPUT_POWER:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.get_output_power);
                break;
            case SET_FREQUENCY_REGION:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.set_freq_reg);
                break;
            case GET_FREQUENCY_REGION:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.get_freq_reg);
                break;
            case SET_BEEPER_MODE:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.set_beeper_mode);
                break;
            case GET_READER_TEMPERATURE:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.get_rader_temp);
                break;
            case READ_GPIO_VALUE:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.read_gpio_value);
                break;
            case WRITE_GPIO_VALUE:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.write_gpio_value);
                break;
            case SET_ANT_CONNECTION_DETECTOR:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.set_ant_conn);
                break;
            case GET_ANT_CONNECTION_DETECTOR:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.get_ant_conn);
                break;
            case SET_TEMPORARY_OUTPUT_POWER:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.set_temp_output_power);
                break;
            case SET_READER_IDENTIFIER:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.set_reader_identi);
                break;
            case GET_READER_IDENTIFIER:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.get_reader_identi);
                break;
            case SET_RF_LINK_PROFILE:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.set_rf_link_pro);
                break;
            case GET_RF_LINK_PROFILE:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.get_rf_link_pro);
                break;
            case GET_RF_PORT_RETURN_LOSS:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.get_rf_port);
                break;
            case INVENTORY:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.inventory);
                break;
            case READ_TAG:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.read_tag_c);
                break;
            case WRITE_TAG:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.write_tag_c);
                break;
            case LOCK_TAG:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.lock_tag_c);
                break;
            case KILL_TAG:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.kill_tag_c);
                break;
            case SET_ACCESS_EPC_MATCH:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.set_access_epc_match);
                break;
            case GET_ACCESS_EPC_MATCH:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.get_access_epc_match);
                break;
            case REAL_TIME_INVENTORY:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.real_time_inventory);
                break;
            case FAST_SWITCH_ANT_INVENTORY:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.fast_switch_ant_inv);
                break;
            case CUSTOMIZED_SESSION_TARGET_INVENTORY:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.customized_session);
                break;
            case SET_IMPINJ_FAST_TID:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.set_impinj);
                break;
            case SET_AND_SAVE_IMPINJ_FAST_TID:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.set_and_save_impinj);
                break;
            case GET_IMPINJ_FAST_TID:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.get_imping);
                break;
            case ISO18000_6B_INVENTORY:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.iso_B_inv);
                break;
            case ISO18000_6B_READ_TAG:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.iso_B_read);
                break;
            case ISO18000_6B_WRITE_TAG:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.iso_b_write_tag);
                break;
            case ISO18000_6B_LOCK_TAG:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.iso_b_lock_tag);
                break;
            case ISO18000_6B_QUERY_LOCK_TAG:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.iso_b_query_lock_tag);
                break;
            case GET_INVENTORY_BUFFER:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.get_inventory_buff);
                break;
            case GET_AND_RESET_INVENTORY_BUFFER:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.get_ant_reset_inv);
                break;
            case GET_INVENTORY_BUFFER_TAG_COUNT:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.get_inventory_buffer_tag_count);
                break;
            case RESET_INVENTORY_BUFFER:
                strCmd = VehicleApplication.getContext().getResources().getString(R.string.reset_inventory_buff);
                break;
            default:
            	strCmd = VehicleApplication.getContext().getResources().getString(R.string.unknown_operate);
                break;
        }
        return strCmd;
    }
}
