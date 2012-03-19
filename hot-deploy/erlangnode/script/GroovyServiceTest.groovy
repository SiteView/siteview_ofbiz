import org.ofbiz.service.ServiceUtil;
import org.ofbiz.base.util.Debug;

import com.ericsson.otp.erlang.OtpConnection;
import com.ericsson.otp.erlang.OtpErlangPid;
import com.ericsson.otp.erlang.OtpPeer;
import com.ericsson.otp.erlang.OtpSelf;
import com.ericsson.otp.erlang.OtpErlangList
import com.ericsson.otp.erlang.OtpErlangObject

import com.erlangnode.client.impl.CommunicationFactory;

import svtype.BaseData;
import svtype.SvAtom;
import svtype.SvList;
import svtype.SvTuple;

module = "GroovyserviceTest.groovy"
Debug.logInfo("-=-=-=- TEST GROOVY SERVICE -=-=-=-", module);

result = ServiceUtil.returnSuccess();
if (context.message) {
    message = context.message;
    result.successMessage = "Got message [" + message + "] and finished fine";

    /*OtpSelf self = new OtpSelf("Ecc90myaaggggRandom");
    self.setCookie("3ren");
    OtpPeer other = new OtpPeer("debug@itsm");
    OtpConnection conn = self.connect(other);
    conn.sendRPC("api_monitor", "get_all_monitors", new OtpErlangList());
    OtpErlangObject retobj = conn.receiveRPC();*/

//    BaseData retobj = CommunicationFactory.getEccService().call("api_monitor","get_all_monitors");
    BaseData retobj = CommunicationFactory.getEccService().call("api_machine","get_ntmachine");
    
    Debug.logInfo("----- ErlangRPC is: $retobj -----", "");
    
    result.result = retobj.toString();
    Debug.logInfo("----- Message is: $message -----", "");
} else {
    result.successMessage = "Got no message but finished fine anyway";
    result.result = "[no message received]";
}

return result;
