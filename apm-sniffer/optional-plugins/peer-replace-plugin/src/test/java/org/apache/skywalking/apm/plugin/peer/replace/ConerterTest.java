package org.apache.skywalking.apm.plugin.peer.replace;

import org.apache.skywalking.apm.agent.core.context.PeerService;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author Ma Qian(maqian258@gmail.com)
 * @date 2019-08-11 13:28
 * @version V1.0
 */
public class ConerterTest {

    @Test
    public void testConverter() throws Throwable {
        PeerService service = new PeerReplaceService();

        System.setProperty("skywalking.peer.replace_patterns", "s/api\\.([^.]*)\\.my\\.com\\/([^\\/]*).*/\\2.\\1/");
        service.boot();
        Assert.assertEquals("users.test", service.replaceRemotePeer("/users/1/posts", "api.test.my.com"));
        Assert.assertEquals("users.test", service.replaceRemotePeer("/users", "api.test.my.com"));
        Assert.assertEquals("api.test.my.com", service.replaceRemotePeer("", "api.test.my.com"));
        Assert.assertEquals("api.test.com", service.replaceRemotePeer("/users", "api.test.com"));


        System.setProperty("skywalking.peer.replace_patterns", "s#api\\.([^.]*)\\.my\\.com/([^/]*).*#\\2.\\1#");
        service.boot();
        Assert.assertEquals("users.test", service.replaceRemotePeer("/users/1/posts", "api.test.my.com"));
        Assert.assertEquals("users.test", service.replaceRemotePeer("/users", "api.test.my.com"));
        Assert.assertEquals("api.test.my.com", service.replaceRemotePeer("", "api.test.my.com"));
        Assert.assertEquals("api.test.com", service.replaceRemotePeer("/users", "api.test.com"));
    }
}
