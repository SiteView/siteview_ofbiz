import com.dragonflow.test.TestErlang;
import org.ofbiz.base.util.Debug;

module = "GroovyserviceTest1.groovy"
Debug.logInfo("-=-=-=------------ TEST GROOVY SERVICE New  ==> Start", module);
return TestErlang.Ping(dctx,context);