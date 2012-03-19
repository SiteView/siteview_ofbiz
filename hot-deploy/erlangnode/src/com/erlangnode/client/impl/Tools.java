package com.erlangnode.client.impl;

import java.util.LinkedList;
import java.util.List;

import svtype.BaseData;
import svtype.SvAtom;
import svtype.SvBinary;
import svtype.SvBitstr;
import svtype.SvBoolean;
import svtype.SvByte;
import svtype.SvChar;
import svtype.SvDouble;
import svtype.SvFloat;
import svtype.SvInt;
import svtype.SvList;
import svtype.SvLong;
import svtype.SvPid;
import svtype.SvPort;
import svtype.SvRef;
import svtype.SvShort;
import svtype.SvString;
import svtype.SvTuple;
import svtype.SvUInt;
import svtype.SvUShort;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangBitstr;
import com.ericsson.otp.erlang.OtpErlangBoolean;
import com.ericsson.otp.erlang.OtpErlangByte;
import com.ericsson.otp.erlang.OtpErlangChar;
import com.ericsson.otp.erlang.OtpErlangDouble;
import com.ericsson.otp.erlang.OtpErlangFloat;
import com.ericsson.otp.erlang.OtpErlangInt;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangLong;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangPid;
import com.ericsson.otp.erlang.OtpErlangPort;
import com.ericsson.otp.erlang.OtpErlangRef;
import com.ericsson.otp.erlang.OtpErlangShort;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.ericsson.otp.erlang.OtpErlangUInt;
import com.ericsson.otp.erlang.OtpErlangUShort;

public class Tools {
	public static BaseData convert(OtpErlangObject elobject) throws Exception{
		if (elobject instanceof OtpErlangAtom){
			OtpErlangAtom val = (OtpErlangAtom) elobject;
			return new SvAtom(val.atomValue());
		}
		else if (elobject instanceof OtpErlangBinary){
			OtpErlangBinary val = (OtpErlangBinary) elobject;
			return new SvBinary(val.binaryValue());
		}
		else if (elobject instanceof OtpErlangBitstr){
			OtpErlangBinary val = (OtpErlangBinary) elobject;
			List<Integer> values = new LinkedList<Integer>();
			for(byte b : val.binaryValue()){
				values.add(new Integer(b));
			}
			return new SvBitstr(values);
		}
		else if (elobject instanceof OtpErlangBoolean){
			OtpErlangBoolean val = (OtpErlangBoolean) elobject;
			return new SvBoolean(val.booleanValue());
		}
		else if (elobject instanceof OtpErlangByte){
			OtpErlangByte val = (OtpErlangByte) elobject;
			return new SvByte(val.byteValue());
		}
		else if (elobject instanceof OtpErlangChar){
			OtpErlangChar val = (OtpErlangChar) elobject;
			return new SvChar(val.charValue());
		}
		else if (elobject instanceof OtpErlangDouble){
			OtpErlangDouble val = (OtpErlangDouble) elobject;
			return new SvDouble(val.doubleValue());
		}
		else if (elobject instanceof OtpErlangFloat){
			OtpErlangFloat val = (OtpErlangFloat) elobject;
			return new SvFloat(val.floatValue());
		}
		else if (elobject instanceof OtpErlangInt){
			OtpErlangInt val = (OtpErlangInt) elobject;
			return new SvInt(val.intValue());
		}
		else if (elobject instanceof OtpErlangLong){
			OtpErlangLong val = (OtpErlangLong) elobject;
			return new SvLong(val.longValue());
		}
		else if (elobject instanceof OtpErlangPid){
			OtpErlangPid val = (OtpErlangPid) elobject;
			return new SvPid(val.node(),val.id(),val.serial(),val.creation());
		}
		else if (elobject instanceof OtpErlangPort){
			OtpErlangPort val = (OtpErlangPort) elobject;
			return new SvPort(val.node(),val.id(),val.creation());
		}
		else if (elobject instanceof OtpErlangRef){
			OtpErlangRef val = (OtpErlangRef) elobject;
			return new SvRef(val.node(),val.ids(),val.creation());
		}
		else if (elobject instanceof OtpErlangShort){
			OtpErlangShort val = (OtpErlangShort) elobject;
			return new SvShort(val.shortValue());
		}
		else if (elobject instanceof OtpErlangString){
			OtpErlangString val = (OtpErlangString) elobject;
			return new SvString(new String(val.stringValue().getBytes("ISO-8859-1"),"UTF-8"));
		}
		else if (elobject instanceof OtpErlangUInt){
			OtpErlangUInt val = (OtpErlangUInt) elobject;
			return new SvUInt(val.uIntValue());
		}
		else if (elobject instanceof OtpErlangUShort){
			OtpErlangUShort val = (OtpErlangUShort) elobject;
			return new SvUShort(val.uShortValue());
		}
		else if (elobject instanceof OtpErlangList){
			OtpErlangList list = (OtpErlangList) elobject;
			List<BaseData> blist = new LinkedList<BaseData>();
			for(OtpErlangObject val : list.elements()){
				blist.add(convert(val));
			}
			return new SvList(blist.toArray(new BaseData[blist.size()]));
		}
		else if (elobject instanceof OtpErlangTuple){
			OtpErlangTuple list = (OtpErlangTuple) elobject;
			List<BaseData> blist = new LinkedList<BaseData>();
			for(OtpErlangObject val : list.elements()){
				blist.add(convert(val));
			}
			return new SvTuple(blist.toArray(new BaseData[blist.size()]));
		}
		return null;
	}
}
