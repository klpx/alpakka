/*
 * Copyright (C) 2016-2017 Lightbend Inc. <http://www.lightbend.com>
 */
package akka.stream.alpakka.ftp;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.filesystem.jimfs.JimfsFactory;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.ClearTextPasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;

abstract class PlainFtpSupportImpl extends FtpSupportImpl {

    static final String DEFAULT_LISTENER = "default";

    protected FtpServerFactory createFtpServerFactory(Integer port) {
        Configuration fsConfig = Configuration.unix().toBuilder().setAttributeViews("basic", "posix").build();
        setFileSystem(Jimfs.newFileSystem(fsConfig));
        JimfsFactory fsf = new JimfsFactory(getFileSystem());
        fsf.setCreateHome(true);

        PropertiesUserManagerFactory pumf = new PropertiesUserManagerFactory();
        pumf.setAdminName("admin");
        pumf.setPasswordEncryptor(new ClearTextPasswordEncryptor());
        pumf.setFile(getUsersFile());
        UserManager userMgr = pumf.createUserManager();

        ListenerFactory factory = new ListenerFactory();
        factory.setPort(port);

        FtpServerFactory serverFactory = new FtpServerFactory();
        serverFactory.setUserManager(userMgr);
        serverFactory.setFileSystem(fsf);
        serverFactory.setConnectionConfig(new ConnectionConfigFactory().createConnectionConfig());
        serverFactory.addListener(DEFAULT_LISTENER, factory.createListener());

        return serverFactory;
    }
}
