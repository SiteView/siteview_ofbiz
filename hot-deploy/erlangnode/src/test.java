import svtype.BaseData;

import com.erlangnode.client.impl.CommunicationFactory;


public class test {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		BaseData retobj = CommunicationFactory.getEccService().call("api_monitor","get_all_monitors");
		retobj.toString();
	}

}
