import java.io.IOException;

import org.ofbiz.entity.Delegator;

import com.ericsson.otp.erlang.*;

public class ofbiznode {
	public static void main(String[] args) throws Exception

	{

		OtpNode myNode = new OtpNode("ofbiz", "3ren");

		OtpMbox myMbox = myNode.createMbox("admin");

		OtpErlangObject myObject;

		OtpErlangTuple myMsg;

		OtpErlangPid from;

		OtpErlangString command;

		Integer counter = 0;

		OtpErlangAtom myAtom = new OtpErlangAtom("ok");

		while (counter >= 0)
			try

			{
				System.out.println("Server:" + myNode.node() + " Cookie:"
						+ myNode.cookie() + " Counter:" + counter);

				OtpMsg msg = myMbox.receiveMsg();

				myMsg = (OtpErlangTuple) msg.getMsg();

				from = msg.getSenderPid();
				Delegator delegator = dctx.getDelegator();
				delegator.create(primaryKey);
				command = (OtpErlangString) myMsg.elementAt(1);

				// here you may want to check the value of command

				OtpErlangObject[] reply = new OtpErlangObject[2];

				reply[0] = myAtom;

				reply[1] = new OtpErlangInt(counter);

				OtpErlangTuple myTuple = new OtpErlangTuple(reply);

				myMbox.send(from, myTuple);

				counter++;

			} catch (OtpErlangExit e)

			{

				break;

			}

	}
}
