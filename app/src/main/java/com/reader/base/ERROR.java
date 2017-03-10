package com.reader.base;

import com.uhf.uhf.R;
import com.uhf.uhf.UHFApplication;
import com.ui.base.PreferenceUtil;

/**
 * Regarding UHF return code and description, please refer to Serial_Protocol_User's_Guide_V2.38_en  
 * 
 * @author Administrator
 *
 */
public class ERROR {
	public final static byte SUCCESS = 0x10;
	public final static byte FAIL = 0x11;
	public final static byte MCU_RESET_ERROR = 0x20;
	public final static byte CW_ON_ERROR = 0x21;
	public final static byte ANTENNA_MISSING_ERROR = 0x22;
	public final static byte WRITE_FLASH_ERROR = 0x23;
	public final static byte READ_FLASH_ERROR = 0x24;
	public final static byte SET_OUTPUT_POWER_ERROR = 0x25;
	public final static byte TAG_INVENTORY_ERROR = 0x31;
	public final static byte TAG_READ_ERROR = 0x32;
	public final static byte TAG_WRITE_ERROR = 0x33;
	public final static byte TAG_LOCK_ERROR = 0x34;
	public final static byte TAG_KILL_ERROR = 0x35;
	public final static byte NO_TAG_ERROR = 0x36;
	public final static byte INVENTORY_OK_BUT_ACCESS_FAIL = 0x37;
	public final static byte BUFFER_IS_EMPTY_ERROR = 0x38;
	public final static byte ACCESS_OR_PASSWORD_ERROR = 0x40;
	public final static byte PARAMETER_INVALID = 0x41;
	public final static byte PARAMETER_INVALID_WORDCNT_TOO_LONG = 0x42;
	public final static byte PARAMETER_INVALID_MEMBANK_OUT_OF_RANGE = 0x43;
	public final static byte PARAMETER_INVALID_LOCK_REGION_OUT_OF_RANGE = 0x44;
	public final static byte PARAMETER_INVALID_LOCK_ACTION_OUT_OF_RANGE = 0x45;
	public final static byte PARAMETER_READER_ADDRESS_INVALID = 0x46;
	public final static byte PARAMETER_INVALID_ANTENNA_ID_OUT_OF_RANGE = 0x47;
	public final static byte PARAMETER_INVALID_OUTPUT_POWER_OUT_OF_RANGE = 0x48;
	public final static byte PARAMETER_INVALID_FREQUENCY_REGION_OUT_OF_RANGE = 0x49;
	public final static byte PARAMETER_INVALID_BAUDRATE_OUT_OF_RANGE = 0x4A;
	public final static byte PARAMETER_BEEPER_MODE_OUT_OF_RANGE = 0x4B;
	public final static byte PARAMETER_EPC_MATCH_LEN_TOO_LONG = 0x4C;
	public final static byte PARAMETER_EPC_MATCH_LEN_ERROR = 0x4D;
	public final static byte PARAMETER_INVALID_EPC_MATCH_MODE = 0x4E;
	public final static byte PARAMETER_INVALID_FREQUENCY_RANGE = 0x4F;
	public final static byte FAIL_TO_GET_RN16_FROM_TAG = 0x50;
	public final static byte PARAMETER_INVALID_DRM_MODE = 0x51;
	public final static byte PLL_LOCK_FAIL = 0x52;
	public final static byte RF_CHIP_FAIL_TO_RESPONSE = 0x53;
	public final static byte FAIL_TO_ACHIEVE_DESIRED_OUTPUT_POWER = 0x54;
	public final static byte COPYRIGHT_AUTHENTICATION_FAIL = 0x55;
	public final static byte SPECTRUM_REGULATION_ERROR = 0x56;
	public final static byte OUTPUT_POWER_TOO_LOW = 0x57;

	public static String format(byte btErrorCode) {
		String strErrorCode = "";
		switch (btErrorCode) {
		case SUCCESS:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.command_succeeded);
			break;
		case FAIL:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.command_failed);
			break;
		case MCU_RESET_ERROR:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.cup_seset_error);
			break;
		case CW_ON_ERROR:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.cw_on_error);
			break;
		case ANTENNA_MISSING_ERROR:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.antenna_miss_error);
			break;
		case WRITE_FLASH_ERROR:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.write_flash_error);
			break;
		case READ_FLASH_ERROR:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.Read_flash_error);
			break;
		case SET_OUTPUT_POWER_ERROR:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.set_output_power_error);
			break;
		case TAG_INVENTORY_ERROR:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.tag_inventory_error);
			break;
		case TAG_READ_ERROR:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.tag_read_error);
			break;
		case TAG_WRITE_ERROR:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.tag_write_error);
			break;
		case TAG_LOCK_ERROR:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.tag_lock_error);
			break;
		case TAG_KILL_ERROR:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.tag_kill_error);
			break;
		case NO_TAG_ERROR:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.no_tag_error);
			break;
		case INVENTORY_OK_BUT_ACCESS_FAIL:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.inventory_ok_but_access_fail);
			break;
		case BUFFER_IS_EMPTY_ERROR:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.buffer_is_empty_error);
			break;
		case ACCESS_OR_PASSWORD_ERROR:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.access_ro_password_error);
			break;
		case PARAMETER_INVALID:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.parameter_invalid);
			break;
		case PARAMETER_INVALID_WORDCNT_TOO_LONG:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.parameter_invalid_wordcnt_too_long);
			break;
		case PARAMETER_INVALID_MEMBANK_OUT_OF_RANGE:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.parameter_invaild_membank_out_of_range);
			break;
		case PARAMETER_INVALID_LOCK_REGION_OUT_OF_RANGE:
			strErrorCode = UHFApplication
					.getContext()
					.getResources()
					.getString(
							R.string.parameter_invaild_lock_region_out_of_range);
			break;
		case PARAMETER_INVALID_LOCK_ACTION_OUT_OF_RANGE:
			strErrorCode = UHFApplication
					.getContext()
					.getResources()
					.getString(
							R.string.parameter_invaild_lock_action_out_of_range);
			break;
		case PARAMETER_READER_ADDRESS_INVALID:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.parameter_reader_address_invaild);
			break;
		case PARAMETER_INVALID_ANTENNA_ID_OUT_OF_RANGE:
			strErrorCode = UHFApplication
					.getContext()
					.getResources()
					.getString(
							R.string.parameter_invaild_antenna_id_out_of_range);
			break;
		case PARAMETER_INVALID_OUTPUT_POWER_OUT_OF_RANGE:
			strErrorCode = UHFApplication
					.getContext()
					.getResources()
					.getString(
							R.string.parameter_invaild_output_power_out_of_range);
			break;
		case PARAMETER_INVALID_FREQUENCY_REGION_OUT_OF_RANGE:
			strErrorCode = UHFApplication
					.getContext()
					.getResources()
					.getString(
							R.string.parameter_invaild_frequency_region_out_of_range);
			break;
		case PARAMETER_INVALID_BAUDRATE_OUT_OF_RANGE:
			strErrorCode = UHFApplication
					.getContext()
					.getResources()
					.getString(R.string.parameter_invaild_baudrate_out_of_range);
			break;
		case PARAMETER_BEEPER_MODE_OUT_OF_RANGE:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.parameter_beeper_mode_out_of_range);
			break;
		case PARAMETER_EPC_MATCH_LEN_TOO_LONG:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.parameter_epc_match_len_too_long);
			break;
		case PARAMETER_EPC_MATCH_LEN_ERROR:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.parameter_epc_match_len_error);
			break;
		case PARAMETER_INVALID_EPC_MATCH_MODE:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.parameter_invaild_epc_match_mode);
			break;
		case PARAMETER_INVALID_FREQUENCY_RANGE:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.parameter_invaild_frequency_range);
			break;
		case FAIL_TO_GET_RN16_FROM_TAG:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.fail_to_get_rn16_from_tag);
			break;
		case PARAMETER_INVALID_DRM_MODE:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.parameter_invaild_drm_mode);
			break;
		case PLL_LOCK_FAIL:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.pll_lock_fail);
			break;
		case RF_CHIP_FAIL_TO_RESPONSE:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.rf_chip_fail_to_response);
			break;
		case FAIL_TO_ACHIEVE_DESIRED_OUTPUT_POWER:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.fail_to_chieve_desired_output_power);
			break;
		case COPYRIGHT_AUTHENTICATION_FAIL:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.copyright_authentication_fail);
			break;
		case SPECTRUM_REGULATION_ERROR:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.spectrum_regulation_error);
			break;
		case OUTPUT_POWER_TOO_LOW:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.output_power_too_low);
			break;
		default:
			strErrorCode = UHFApplication.getContext().getResources()
					.getString(R.string.unknown_error);
			break;
		}
		return strErrorCode;
	}
}
