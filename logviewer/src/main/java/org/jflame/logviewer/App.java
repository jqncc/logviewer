package org.jflame.logviewer;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.jflame.commons.util.StringHelper;

public class App {

    public static void main(String[] args) throws Exception {
        Path classRunDir = Paths.get(App.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        // System.out.println(classRunDir);
        if (classRunDir.toString().endsWith(".jar")) {
            classRunDir = classRunDir.getParent();
            if (classRunDir.endsWith("lib")) {
                classRunDir = classRunDir.getParent();// 打包以jar方式运行在lib目录,返回到上层目录
            }
        }
        String appBase = ".";
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(appBase);
        tomcat.setPort(SysParam.getServerPort());
        String siteHost = SysParam.getServerHost();
        if (StringHelper.isNotEmpty(siteHost)) {
            tomcat.setHostname(appBase);
        }

        Host host = tomcat.getHost();
        host.setAppBase(appBase);
        host.setAutoDeploy(false);

        Connector connector = new Connector();
        connector.setPort(SysParam.getServerPort());
        connector.setURIEncoding(StandardCharsets.UTF_8.name());
        tomcat.setConnector(connector);

        String webappDir = classRunDir.resolve("webapp").toString();

        Context context = tomcat.addWebapp(SysParam.getServerContextPath(), webappDir);
        context.addLifecycleListener(new Tomcat.FixContextListener());
        context.setUseHttpOnly(true);
        context.setRequestCharacterEncoding(StandardCharsets.UTF_8.name());
        context.setResponseCharacterEncoding(StandardCharsets.UTF_8.name());

        // context.setParentClassLoader(App.class.getClassLoader());
        // configureResources(context);
        // tomcat 启动jar扫描设置为跳过所有，避免与框架结合出现 jar file not found exception
        // System.setProperty("tomcat.util.scan.StandardJarScanFilter.jarsToSkip", "\\,*");
        // addServlets(context);
        // addFilters(context);
        // context.getServletContext().addListener(new MySessionListener());

        tomcat.start();
        tomcat.getServer().await();
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                try {
                    tomcat.stop();
                    tomcat.destroy();
                } catch (LifecycleException e) {
                    e.printStackTrace();
                }
                System.out.println("stopped logviewer");
            }
        });
    }

    /* private static void addServlets(Context ctx) {
        Tomcat.addServlet(ctx, "loginServlet", new LoginServlet());
        ctx.addServletMappingDecoded("/login", "loginServlet");
    
        Tomcat.addServlet(ctx, "showServlet", new ShowServlet());
        ctx.addServletMappingDecoded("/show.do", "showServlet");
    
        Wrapper w = Tomcat.addServlet(ctx, "logoutServlet", new LogoutServlet());
        w.addInitParameter("logoutPage", "index.html");
        ctx.addServletMappingDecoded("/logout", "logoutServlet");
    }
    
    private static void addFilters(Context ctx) {
        FilterDef loginFilterDef = new FilterDef();
        loginFilterDef.setFilter(new LoginFilter());
        loginFilterDef.setFilterName("loginFilter");
    
        FilterMap loginFilterMap = new FilterMap();
        loginFilterMap.setFilterName("loginFilter");
        loginFilterMap.addURLPattern("*.do");
    
        ctx.addFilterDef(loginFilterDef);
        ctx.addFilterMap(loginFilterMap);
    }
    */
    /**
     * 自动扫描servlet,filter注解配置
     * 
     * @param context
     */
    /* private static void configureResources(Context context) {
        String WORK_HOME = System.getProperty("user.dir");
        File classesDir = new File(WORK_HOME, "target/classes");
        File jarDir = new File(WORK_HOME, "lib");
        WebResourceRoot resources = new StandardRoot(context);
        System.out.println(jarDir);
        if (classesDir.exists()) {
            resources.addPreResources(
                    new DirResourceSet(resources, "/WEB-INF/classes", classesDir.getAbsolutePath(), "/"));
            // System.out.println("Resources added: [classes]");
        } else if (jarDir.exists()) {
            resources.addJarResources(new DirResourceSet(resources, "/WEB-INF/lib", jarDir.getAbsolutePath(), "/"));
            System.out.println("Resources added: [jar]");
        } else {
            resources.addPreResources(new EmptyResourceSet(resources));
            // System.out.println("Resources added: [empty]");
        }
        context.setResources(resources);
    }*/
}
